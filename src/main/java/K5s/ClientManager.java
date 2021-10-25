package K5s;

import K5s.connectionManager.ClientMessageThread;
//import K5s.storage.ChatClient;
import K5s.connectionManager.ServerMessageThread;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static K5s.protocol.ServerToClientProtocol.*;

public class ClientManager {

    private static ArrayList<ChatClient> chatClients;
    public static RoomManager roomManager;
    private static Map<String ,ClientMessageThread> identitySubscribers;

    public ClientManager(RoomManager manager){
        chatClients = new ArrayList<>();
        this.roomManager = manager;
        identitySubscribers=new HashMap<>();
    }

    public synchronized boolean newIdentity(String identity, ClientMessageThread clientMessageThread){
        if(!roomManager.isValidId(identity)){
            return false;
        }
        switch (isAvailableIdentity(identity ,clientMessageThread)) {
            case "WAITING":
                System.out.println("User waiting for approval.");
                return true;
            case "FALSE":
                System.out.println(identity + " already in use.");
                return false;
            case "TRUE":
                replyIdentityRequest(identity,true);
                return true;
            default:
                System.out.println("Invalid case");
                return false;
        }
    }
    public synchronized boolean newRoom(String roomId, ChatClient client){

        if(!roomManager.isValidId(roomId)){
            return false;
        }

        switch (roomManager.isAvailableRoomName(roomId ,client)) {
            case "WAITING":
                System.out.println("User waiting for approval.");
                return true;
            case "FALSE":
                System.out.println(roomId + " already in use.");
                return false;
            case "TRUE":
                replyNewRoomRequest(roomId,true);
                return true;
            default:
                System.out.println("Invalid case");
                return false;
        }
    }
    public static void replyNewRoomRequest(String roomid,boolean approved){
        Map<String, ChatClient> sub = RoomManager.createRoomSubscribers;
        if(sub.containsKey(roomid)){
            ChatClient user = sub.get(roomid);
            ClientMessageThread clientMessageThread = user.getMessageThread();
            if (approved) {
                System.out.println("Room has been approved.");
                ChatRoom fr =user.getRoom();
                ChatRoom r= roomManager.createRoom(roomid,user);
                user.setRoom(r);
                sub.remove(roomid);
                try {
                    clientMessageThread.send(getCreateRoomReply(roomid,true));
                    sendRoomChangeBroadcast(user,fr,r);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                boolean b =fr.removeMember(user);
                if(ChatServer.isLeader()){
                    ServerManager.gossipState();
                }
            }else {
                System.out.println("User request declined.");
                sub.remove(roomid);
                try {
                    clientMessageThread.send(getCreateRoomReply(roomid,false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            //server detected timeout and reply to the client that identity not available to ensure availability
//            TODO:inform leader to delete the approved identity
        }
    }
    public static void replyIdentityRequest(String identity,boolean approved){
        if(identitySubscribers.containsKey(identity)){
            if (approved) {
                System.out.println("User has been approved.");
                ClientMessageThread clientMessageThread = identitySubscribers.get(identity);
                ChatClient user = new ChatClient(identity, clientMessageThread);
                chatClients.add(user);
                user.setRoom(roomManager.getMainHall());
                roomManager.addToMainHall(user);
                clientMessageThread.setClient(user);
                sendMainhallBroadcast(user);
                identitySubscribers.remove(identity);
                if(ChatServer.isLeader()){
                    ServerManager.gossipState();
                }
            }else {
                System.out.println("User request declined.");
                ClientMessageThread clientMessageThread = identitySubscribers.get(identity);
                identitySubscribers.remove(identity);
                try {
                    clientMessageThread.send(getNewIdentityReply(identity,false));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }else{
            //server detected timeout and reply to the client that identity not available to ensure availability
//            TODO:inform leader to delete the approved identity
        }

    }
    public synchronized String isAvailableIdentity(String identity,ClientMessageThread clientMessageThread) {
        for (ChatClient u : chatClients) {
            if (u.getChatClientID().equalsIgnoreCase(identity)) {
                return "FALSE";
            }
        }
        switch (ServerManager.isAvailableIdentity(identity)) {
            case "WAITING":
                System.out.println("WAITING");
                identitySubscribers.put(identity, clientMessageThread);
                return "WAITING";
            case "FALSE":
                System.out.println("FALSE");
                return "FALSE";
            case "TRUE":
                System.out.println("TRUE");
                identitySubscribers.put(identity, clientMessageThread);
                return "TRUE";
            default:
                System.out.println("Invalid case");
                return "FALSE";
        }
    }

    public static void sendMainhallBroadcast(ChatClient client){
        JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), "", roomManager.getMainHall().getRoomId());
        roomManager.broadcastMessageToMembers(roomManager.getMainHall(),message);
    }

    public ArrayList<String> listGlobalRoomIds(){
        ArrayList<String> roomIds = roomManager.getRoomIds();
        ArrayList<String> rooms = roomManager.getMeserver().getRooms();
        roomIds.removeAll(rooms);
        roomIds.addAll(rooms);
        return roomIds;
    }

    public JSONObject listRoomDetails(ChatClient client){
        ChatRoom room = client.getRoom();
        JSONObject message = getWhoReply(room.getRoomId(),room.getUserIds(), room.getOwner().getChatClientID());
        return message;
    }

//    public synchronized ChatRoom createRoom(ChatClient client, String roomId){
//        ChatRoom former = client.getRoom();
//
//        if(former.getOwner() == client){
//            return null;
//        }
//        boolean isAvailable = roomManager.isRoomIdAvailableToCreate(roomId);
//        if (isAvailable){
//            ChatRoom current = roomManager.createRoom(roomId, client);
//            return current;
//        }
//        return null;
//    }

    public static void sendRoomChangeBroadcast(ChatClient client, ChatRoom formerRoom, ChatRoom newRoom){
        JSONObject message =getRoomChangeBroadcast(client.getChatClientID(),formerRoom.getRoomId(),newRoom.getRoomId());
        roomManager.broadcastMessageToMembers(formerRoom,message);
        roomManager.broadcastMessageToMembers(newRoom,message);
    }
//    public void sendRoomCreateBroadcast(ChatClient client, ChatRoom room){
//        ChatRoom former = client.getRoom();
////        System.out.println(former.getRoomId());
////        System.out.println(room.getRoomId());
//        JSONObject message =getRoomChangeBroadcast(client.getChatClientID(),former.getRoomId(),room.getRoomId());
//        client.setRoom(room);
//        roomManager.broadcastMessageToMembers(former,message);
//        roomManager.removeUserFromChatRoom(client);
//
//    }

    public synchronized boolean clientDeleteRoom(ChatClient client, String roomId){
        ChatClient owner = roomManager.findOwnerOfRoom(roomId);
        if(owner != client){
            return false;
        }
        else if(owner == null){
            return false;
        }
        ownerDeleteRoom(client);
        JSONObject deleteMessage = sendDeleteRoom(roomId,roomManager.getMeserver().getServerId());
        ServerManager.sendBroadcast(deleteMessage);
        return true;
    }

    public synchronized boolean joinRoom(ChatClient client, String roomId){
        ChatRoom formerRoom = client.getRoom();

        if(roomManager.findIfOwner(client)){
            return false;
        }
        ChatRoom joinRoom = roomManager.findRoomExists(roomId);
        System.out.println(joinRoom);
        if (joinRoom != null){
            client.setRoom(joinRoom);
            formerRoom.removeMember(client);
            joinRoom.addMember(client);
            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(),formerRoom.getRoomId(),joinRoom.getRoomId());
            roomManager.broadcastMessageToMembers(formerRoom,message);
            roomManager.broadcastMessageToMembers(joinRoom,message);
            return true;
        }
        else{
            Server s = roomManager.findGlobalRoom(roomId);
            if(s == null){
                return false;
            } else {
                formerRoom.removeMember(client);
                JSONObject message = getRoomChangeBroadcast(client.getChatClientID(),formerRoom.getRoomId(),roomId);
                roomManager.broadcastMessageToMembers(formerRoom,message);
                chatClients.remove(client);
//                JSONObject deleteMessage = sendDeleteIdenity(client.getChatClientID());
//                roomManager.removeIdentity(client.getChatClientID(),deleteMessage);
                JSONObject routeMessage = getRouteUser(client.getChatClientID(), s.getIpAddress(), roomId, String.valueOf(s.getClientPort()));
                try {
//                    ServerManager.sendBroadcast(deleteMessage);
                    client.getMessageThread().send(routeMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
//        return false;
    }

    public synchronized void moveJoinRoom(String identity, String joinRoomId, ClientMessageThread recieveThread,
                                          String formerRoomId){
        ChatRoom room = roomManager.findRoomExists(joinRoomId);
        ChatClient c = new ChatClient(identity, recieveThread);
        recieveThread.setClient(c);
        JSONObject movejoinreply = getMoveJoinReply(identity,true,roomManager.getMeserver().getServerId());
        try {
            c.getMessageThread().send(movejoinreply);
            chatClients.add(c);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO Client has disconnected add quit code
        }

        if(room != null){
            room.addMember(c);
            c.setRoom(room);
            JSONObject message = getRoomChangeBroadcast(c.getChatClientID(),formerRoomId,room.getRoomId());
            roomManager.broadcastMessageToMembers(room,message);
        }
        else{
            ChatRoom mainHall = roomManager.getMainHall();
            mainHall.addMember(c);
            c.setRoom(mainHall);
            JSONObject message = getRoomChangeBroadcast(c.getChatClientID(),formerRoomId,mainHall.getRoomId());
            roomManager.broadcastMessageToMembers(mainHall,message);
        }
    }

    public void sendMessage(String content, ChatClient user){
        JSONObject message = getMessageBroadcast(content, user.getChatClientID());
        roomManager.broadcastMessageToMembers(user.getRoom(), message);
    }

    public synchronized boolean chatClientQuit(ChatClient client){
        if (client!=null){
            chatClients.remove(client);
            roomManager.getMeserver().removeIdentity(client.getChatClientID());
            JSONObject deleteMessage = sendDeleteIdenity(client.getChatClientID());
            ServerManager.sendBroadcast(deleteMessage);
            boolean isOwner = roomManager.removeUserFromChatRoom(client);
            return isOwner;
        }
        return false;


    }

    public synchronized void ownerDeleteRoom(ChatClient client){
        ChatRoom room = client.getRoom();
        roomManager.deleteRoom(room);
    }
}
