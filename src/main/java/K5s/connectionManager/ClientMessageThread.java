package K5s.connectionManager;

import K5s.ChatServer;
import K5s.ClientManager;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static K5s.protocol.ServerToClientProtocol.*;

public class ClientMessageThread implements Runnable{
    private final Socket socket;
    private BufferedReader in;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private DataOutputStream out ;
    private ClientManager manager;
    private JSONParser parser = new JSONParser();
    private ChatClient client;
    private ChatServer server;

    /**
     *
     * @param socket the client socket maintaining the client connection
     * @throws IOException on socket failure
     */
    public ClientMessageThread(Socket socket, ClientManager manager,ChatServer server) throws IOException {
        this.manager = manager;
        this.socket = socket;
        this.out=new DataOutputStream(socket.getOutputStream());
        this.client = null;
        this.server = server;
    }
    public ChatClient getClient(){
        return client;
    }
    public void setClient(ChatClient client){
        this.client=client;
    }
    /**
     * create a new BufferReader to read TCP inputStream to the socket specified
     * wait on the BufferReader for new messages ,parse them and pass them to MessageReceive method
     * catch IoException and follow Quit protocol
     */
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            JSONObject message;
            while (this.running.get()){
                message = (JSONObject) parser.parse(in.readLine());
                System.out.println("Receiving: " + message);
                this.MessageReceive(message);
            }

            this.in.close();
            this.socket.close();
        }  catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());

            boolean isOwner = manager.chatClientQuit(this.client);
            try {
                send(quitOwnerReply(client.getChatClientID(),client.getRoom().getRoomId()));
                if (isOwner){
                    manager.ownerDeleteRoom(client);
                }
            } catch (IOException ex) {
                System.out.println("Communication Error: " + ex.getMessage());
            } catch (NullPointerException ex){

                e.printStackTrace();
            }

            running.set(false);
        } catch (ParseException e) {
            System.out.println("Message Error: " + e.getMessage());

            boolean isOwner = manager.chatClientQuit(this.client);
            try {
                send(quitOwnerReply(client.getChatClientID(),client.getRoom().getRoomId()));
                if (isOwner){
                    manager.ownerDeleteRoom(client);
                }
            } catch (IOException ex) {
                System.out.println("Communication Error: " + ex.getMessage());
            }

            running.set(false);
        }

    }

    /**
     *
     * @param message JSONObject receive message from the user
     * @throws IOException on socket failure
     */
    public void MessageReceive(JSONObject message) throws IOException {
        String type = (String) message.get("type");
        String identity;
        switch (type) {
            case "newidentity":
                /**
                 * for newidentity message
                 * check the server object for availability  of the identity
                 * if the requested identity s available create new User object ,
                 * add the user to the server user list and set user to the current messageReceiveThread
                 * then create reply message using  getNewIdentityReply and send it to the user
                 * if identity is already taken then close the socket
                 */
                identity = (String) message.get("identity");

                if(server.getElectionInProgress()){
                    System.out.println("Identity declined since election in progress");
                    send(getNewIdentityReply(identity,false));
                    this.in.close();
                    socket.close();
                } else {
                    if (manager.newIdentity(identity,this) ) {
                        System.out.println("User waiting for leader approval or got approved");
//                            clientManager has replied already
                    }else{
                        System.out.println("leader declined.");
                        send(getNewIdentityReply(identity,false));
                        this.in.close();
                        socket.close();
                    }
                }

                break;
            case "message":
                /**
                 * for incoming messages
                 * get the current user bind to the thread and there room , then if the room is not the main room
                 * ( ie: he/she is not in the main room )  broadcast the received message by
                 * calling the broadcastMessage function from respective room
                 * here the identity key is check because type "message" is used to send the broadcast message with the identity
                 * if identity key is available then it is a broadcast message send by the Room
                 * else it is a message received from the client
                 */
                String m = (String) message.get("content");
                if (message.containsKey("identity")) {
                    send(message);
                }
                else{
                    manager.sendMessage(m,this.client);
                }

                break;
            case "list":
                /**
                 * get existing roomIds by calling getRoomIds method of current server
                 * TODO :this method need to be updated to return all the rooms in the entire system
                 */
                ArrayList<String> roomIds = manager.listRoomIds();
                send(getListReply(roomIds));
                break;
            case "who":
                /**
                 * replies with the current users in the same room as the user
                 */
                JSONObject reply = manager.listRoomDetails(this.client);
                send(reply);
                break;
            case "createroom":
                /**
                 * when createroom message is received the system will check
                 * whether the roomid is already taken by calling thisServer isRoomAvailableToCreate method
                 * if available and the user is not owner of any other room create new room and add to the server and gossip
                 * and send success  message to user and move him/her to the room and
                 * broadcast roomChange message to the respective rooms
                 * Else send fail message to the user
                 * TODO : inform other servers the creation of new room
                 */
                String cid = (String) message.get("roomid");
                if(server.getElectionInProgress()){
                    send(getCreateRoomReply(cid,false));
                } else {
                    ChatRoom room = manager.createRoom(this.client, cid);
                    if ((this.client != null) && (room != null)){
                        send(getCreateRoomReply(cid,true));
                        manager.sendRoomCreateBroadcast(client, room);
                    }
                    else{
                        send(getCreateRoomReply(cid,false));
                    }
                }

                break;

            case "joinroom":
                /**
                 * if the requested user already in a room and he is the owner of that room send a failure message
                 * else check if the current server has this Room
                 * if it exists call user joinroom methode to move him to that room.
                 * else check TODO: whether other servers has that room ,
                 * if so then TODO : send a route message
                 * else send a fail message
                 */
                String jrid = (String) message.get("roomid");
                boolean success = manager.joinRoom(this.client,jrid);
                if(!success){
                    send(getRoomChangeBroadcast(this.client.getChatClientID(),jrid,jrid));
                }
                break;
            case "movejoin":
                String joinRoomid = (String) message.get("roomid");
                String formerRoomid = (String) message.get("former");
                String clientIdentity = (String) message.get("identity");

                manager.moveJoinRoom(clientIdentity, joinRoomid, this, formerRoomid);
//                if(!isServerChange){
//                    send(getMoveJoinReply(clientIdentity,true,this.server.getServerId()));
//                }

                break;
            case "deleteroom":
                /**
                 * TODO : inform other servers of room deletion
                 */
                String rid = (String) message.get("roomid");
                boolean isApproved = manager.clientDeleteRoom(this.client,rid);
                if (isApproved){
                    send(getDeleteRoomRequest(rid,true));
                }
                else{
                    send(getDeleteRoomRequest(rid,false));
                }
                break;
            case "roomchange":
                /**
                 * broadcasting room change messages
                 */
                send(message);
                break;
            case "quit":
                /**
                 * TODO : inform other servers of room deletion is user is room owner
                 */
                boolean isOwner = manager.chatClientQuit(this.client);
                try {
                    send(quitOwnerReply(client.getChatClientID(),client.getRoom().getRoomId()));
                    if (isOwner){
                        manager.ownerDeleteRoom(client);
                    }
                } catch (IOException ex) {
                    System.out.println("Communication Error: " + ex.getMessage());
                }
            default:
                System.out.println(message + "not configured");
        }
    }

    /**
     *
     * @param obj JSONObject to be written to the OutputStream of the socket
     * @throws IOException on socket failure
     */
    public void send(JSONObject obj) throws IOException {
        System.out.println("Reply :" + obj );
        out.write((obj.toString() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        if (client==null){
            this.running.set(false);
        }
    }

}
