package K5s;

import K5s.connectionManager.ClientMessageThread;
//import K5s.storage.ChatClient;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static K5s.protocol.ServerToClientMessages.*;

public class ClientManager {

    private static ArrayList<ChatClient> chatClients;
    private static RoomManager roomManager;
    private static Map<String ,ClientMessageThread> identitySubscribers;

    public ClientManager(RoomManager manager){
        chatClients = new ArrayList<>();
        this.roomManager = manager;

    }

    public synchronized boolean newIdentity(String identity, ClientMessageThread clientMessageThread){

        if (isAvailableIdentity(identity ,clientMessageThread)) {
            System.out.println("User waiting for approval.");
            return true;
//            ChatClient user = new ChatClient(identity, clientMessageThread);
//            chatClients.add(user);
//            user.setRoom(roomManager.getMainHall());
//            roomManager.addToMainHall(user);
//            return user;
        } else {
            System.out.println(identity + " already in use.");
            return false;
        }
    }
    public static void replyIdentityRequest(String identity,boolean approved){
        if(identitySubscribers.containsKey(identity)){
            System.out.println("User has been approved.");
            ClientMessageThread clientMessageThread=identitySubscribers.get(identity);
            ChatClient user = new ChatClient(identity,clientMessageThread );
            chatClients.add(user);
            user.setRoom(roomManager.getMainHall());
            roomManager.addToMainHall(user);
            clientMessageThread.setClient(user);
            sendMainhallBroadcast(user);
            identitySubscribers.remove(identity);
        }else{
            //server detected timeout and reply to the client that identity not available to ensure availability
//            TODO:inform leader to delete the approved identity
        }

    }
    public synchronized boolean isAvailableIdentity(String identity,ClientMessageThread clientMessageThread) {
        for (ChatClient u : chatClients) {
            if (u.getChatClientID().equalsIgnoreCase(identity)) {
                return false;
            }
        }

        if(ServerManager.isAvailableIdentity(identity)){
            identitySubscribers.put(identity,clientMessageThread);
            return true;
        }else {
            return false;
        }
    }

    public static void sendMainhallBroadcast(ChatClient client){
        JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), "", "MainHall-s1");
        roomManager.broadcastMessageToMembers(roomManager.getMainHall(),message);
    }

    public ArrayList<String> listRoomIds(){
        ArrayList<String> roomIds = roomManager.getRoomIds();

        // TODO : get list of other rooms in system by referring other server details through server manager
        return roomIds;
    }

    public JSONObject listRoomDetails(ChatClient client){
        ChatRoom room = client.getRoom();
        JSONObject message = getWhoReply(room.getRoomId(),room.getUserIds(), room.getOwner().getChatClientID());
        return message;
    }

    public synchronized ChatRoom createRoom(ChatClient client, String roomId){
        ChatRoom former = client.getRoom();

        if(former.getOwner() == client){
            return null;
        }
        boolean isAvailable = roomManager.isRoomIdAvailableToCreate(roomId);
        if (isAvailable){
            ChatRoom current = roomManager.createRoom(roomId, client);
            return current;
        }
        return null;
    }

    public void sendRoomCreateBroadcast(ChatClient client, ChatRoom room){
        ChatRoom former = client.getRoom();
//        System.out.println(former.getRoomId());
//        System.out.println(room.getRoomId());
        JSONObject message =getRoomChangeBroadcast(client.getChatClientID(),former.getRoomId(),room.getRoomId());
        client.setRoom(room);
        roomManager.broadcastMessageToMembers(former,message);
        roomManager.removeUserFromChatRoom(client);

    }

    public synchronized boolean clientDeleteRoom(ChatClient client, String roomId){
        ChatClient owner = roomManager.findOwnerOfRoom(roomId);
        if(owner != client){
            return false;
        }
        else if(owner == null){
            return false;
        }
        ownerDeleteRoom(client);
        return true;
    }

    public synchronized boolean joinRoom(ChatClient client, String roomId){
        ChatRoom formerRoom = client.getRoom();

        if(roomManager.findIfOwner(client)){
            return false;
        }
        ChatRoom joinRoom = roomManager.findRoomExists(roomId);
        if (joinRoom != null){
            client.setRoom(joinRoom);
            formerRoom.removeMember(client);
            joinRoom.addMember(client);
            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(),formerRoom.getRoomId(),joinRoom.getRoomId());
            roomManager.broadcastMessageToMembers(formerRoom,message);
            roomManager.broadcastMessageToMembers(joinRoom,message);
            return true;
        }
        return false;
    }

    public synchronized void moveJoinRoom(String identity, String joinRoomId, ClientMessageThread recieveThread,
                                          String formerRoomId){
        ChatRoom room = roomManager.findRoomExists(joinRoomId);
        if(room != null){
            if(newIdentity(identity, recieveThread)){

            }
        }
        else{
//            ChatRoom mainHall = roomManager.getMainHall();
//            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(),formerRoomId,mainHall.getRoomId());
//            roomManager.broadcastMessageToMembers(mainHall,message);
//        TODO:once move join received check the identity availability and if the identity is available then check whether room is available then if both available move
            //TODO : verify with the requirements how to proceed if identity not available.
        }
    }

    public void sendMessage(String content, ChatClient user){
        JSONObject message = getMessageBroadcast(content, user.getChatClientID());
        roomManager.broadcastMessageToMembers(user.getRoom(), message);
    }

    public synchronized boolean chatClientQuit(ChatClient client){
        chatClients.remove(client);
        boolean isOwner = roomManager.removeUserFromChatRoom(client);
        return isOwner;
    }

    public synchronized void ownerDeleteRoom(ChatClient client){
        roomManager.deleteRoom(client.getRoom());
    }


}
