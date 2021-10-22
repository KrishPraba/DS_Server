package K5s;

import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static K5s.protocol.ServerToClientMessages.getRoomChangeBroadcast;

public class RoomManager {

    private ArrayList<ChatRoom> chatRooms;
    private ChatRoom mainHall;
    private ChatServer meserver;

    public RoomManager(ChatServer meserver){
        this.mainHall = new ChatRoom("MainHall-s1",meserver.getServerId() );
        this.chatRooms = new ArrayList<>();
        chatRooms.add(this.mainHall);
        this.meserver=meserver;
//        TODO:add mainhall to global roomlist
    }

    public synchronized void addToMainHall(ChatClient client){
        this.mainHall.addMember(client);
        this.meserver.addNewIdentity(client.getChatClientID());
    }

    public ChatRoom getMainHall(){ return this.mainHall;}

    public synchronized void broadcastMessageToMembers(ChatRoom room, JSONObject jsonObject){
        ArrayList<ChatClient> members = room.getMembers();
        members.forEach(user -> {
            try{
                if (user.getMessageThread()!=null) {
                    user.getMessageThread().MessageReceive(jsonObject);
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    public void broadcastSeperateMessageToMember(ChatClient client, JSONObject jsonObject){
        try{
            if (client.getMessageThread()!=null) {
                client.getMessageThread().MessageReceive(jsonObject);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getRoomIds(){
        ArrayList<String> roomIds =new ArrayList<>();
        this.chatRooms.forEach(room -> roomIds.add(room.getRoomId()));
        return roomIds;
    }

    public synchronized boolean isRoomIdAvailableToCreate(String roomId){
        if ((roomId.matches("[a-zA-Z0-9]+")) && (Character.isAlphabetic(roomId.charAt(0)))
                && (roomId.length() >= 3) && (roomId.length()<=16)){
            for (ChatRoom room:chatRooms){
                if (room.getRoomId().equalsIgnoreCase(roomId)) {
                    return false;
                }
            }
            //        TODO : check availablity of the room id in the system by referring thru server manager
            return true;
        }
        return false;
    }


    public synchronized ChatRoom createRoom(String roomId, ChatClient client){
        ChatRoom room = new ChatRoom(roomId, client);
        this.chatRooms.add(room);
        return room;
    }

    public synchronized boolean removeUserFromChatRoom(ChatClient client){

        ChatRoom room = client.getRoom();
        room.removeMember(client);
        if (client.equals(room.getOwner())){
            return true;
        }
        else{
            return false;
        }
    }

    public ChatClient findOwnerOfRoom(String roomId){
        for(ChatRoom room: this.chatRooms){
            if (room.getRoomId().equalsIgnoreCase(roomId)) {
                return room.getOwner();
            }
        }
        return null;
    }

    public boolean findIfOwner(ChatClient client){
        for(ChatRoom room: this.chatRooms){
            if (room.getOwner().equals(client)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void deleteRoom(ChatRoom room){
        ArrayList<ChatClient> members = room.getMembers();
        for (ChatClient client:members) {
            JSONObject message =getRoomChangeBroadcast(client.getChatClientID(),room.getRoomId(),mainHall.getRoomId());
            broadcastSeperateMessageToMember(client, message);
            room.removeMember(client);
            JSONObject message2 = getRoomChangeBroadcast(client.getChatClientID(),room.getRoomId(),mainHall.getRoomId());
            broadcastMessageToMembers(mainHall,message2);
            mainHall.addMember(client);
            chatRooms.remove(room);
            client.setRoom(mainHall);
        }
    }

    public ChatRoom findRoomExists(String roomId){
        for(ChatRoom room: this.chatRooms){
            if (room.getRoomId().equalsIgnoreCase(roomId)) {
                return room;
            }
        }
        return null;
    }
}
