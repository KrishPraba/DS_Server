package K5s;

import static K5s.protocol.ServerToClientMessages.*;

import K5s.connectionManager.ClientMessageThread;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.util.ArrayList;

@Slf4j
public class ClientManager {

    private final ArrayList<ChatClient> chatClients;
    private final RoomManager roomManager;

    public ClientManager(RoomManager manager) {
        chatClients = new ArrayList<>();
        this.roomManager = manager;

    }

    public synchronized ChatClient newIdentity(String identity, ClientMessageThread clientMessageThread) {

        if (isAvailableIdentity(identity)) {
            log.info("User has been approved.");
            ChatClient user = new ChatClient(identity, clientMessageThread);
            chatClients.add(user);
            user.setRoom(roomManager.getMainHall());
            roomManager.addToMainHall(user);
            return user;
        } else {
            log.info("{} already in use.", identity);
            return null;
        }
    }

    public synchronized boolean isAvailableIdentity(String identity) {
        for (ChatClient u : chatClients) {
            if (u.getChatClientID().equalsIgnoreCase(identity)) {
                return false;
            }
        }
//        TODO : check availablity of the idenity in the system by asking leader
        return true;
    }

    public void sendMainhallBroadcast(ChatClient client) {
        JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), "", "MainHall-s1");
        roomManager.broadcastMessageToMembers(roomManager.getMainHall(), message);
    }

    public ArrayList<String> listRoomIds() {
        ArrayList<String> roomIds = roomManager.getRoomIds();

        // TODO : get list of other rooms in system by referring other server details thru server manager
        return roomIds;
    }

    public JSONObject listRoomDetails(ChatClient client) {
        ChatRoom room = client.getRoom();
        JSONObject message = getWhoReply(room.getRoomId(), room.getUserIds(), room.getOwner().getChatClientID());
        return message;
    }

    public synchronized ChatRoom createRoom(ChatClient client, String roomId) {
        ChatRoom former = client.getRoom();

        if (former.getOwner() == client) {
            return null;
        }
        boolean isAvailable = roomManager.isRoomIdAvailableToCreate(roomId);
        if (isAvailable) {
            ChatRoom current = roomManager.createRoom(roomId, client);
            return current;
        }
        return null;
    }

    public void sendRoomCreateBroadcast(ChatClient client, ChatRoom room) {
        ChatRoom former = client.getRoom();
//        System.out.println(former.getRoomId());
//        System.out.println(room.getRoomId());
        JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), former.getRoomId(), room.getRoomId());
        client.setRoom(room);
        roomManager.broadcastMessageToMembers(former, message);
        roomManager.removeUserFromChatRoom(client);

    }

    public synchronized boolean clientDeleteRoom(ChatClient client, String roomId) {
        ChatClient owner = roomManager.findOwnerOfRoom(roomId);
        if (owner != client) {
            return false;
        } else if (owner == null) {
            return false;
        }
        ownerDeleteRoom(client);
        return true;
    }

    public synchronized boolean joinRoom(ChatClient client, String roomId) {
        ChatRoom formerRoom = client.getRoom();

        if (roomManager.findIfOwner(client)) {
            return false;
        }
        ChatRoom joinRoom = roomManager.findRoomExists(roomId);
        if (joinRoom != null) {
            client.setRoom(joinRoom);
            formerRoom.removeMember(client);
            joinRoom.addMember(client);
            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), formerRoom.getRoomId(), joinRoom.getRoomId());
            roomManager.broadcastMessageToMembers(formerRoom, message);
            roomManager.broadcastMessageToMembers(joinRoom, message);
            return true;
        }
        return false;
    }

    public synchronized boolean moveJoinRoom(String identity, String joinRoomId, ClientMessageThread recieveThread,
                                             String formerRoomId) {
        ChatRoom room = roomManager.findRoomExists(joinRoomId);
        ChatClient client = newIdentity(identity, recieveThread);
        if ((room != null) && (client != null)) {
            roomManager.getMainHall().removeMember(client);
            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), formerRoomId, room.getRoomId());
            client.setRoom(room);
            room.addMember(client);
            roomManager.broadcastMessageToMembers(room, message);
            return true;
        } else {
            ChatRoom mainHall = roomManager.getMainHall();
            JSONObject message = getRoomChangeBroadcast(client.getChatClientID(), formerRoomId, mainHall.getRoomId());
            roomManager.broadcastMessageToMembers(mainHall, message);
            return false;
        }
    }

    public void sendMessage(String content, ChatClient user) {
        JSONObject message = getMessageBroadcast(content, user.getChatClientID());
        roomManager.broadcastMessageToMembers(user.getRoom(), message);
    }

    public synchronized boolean chatClientQuit(ChatClient client) {
        chatClients.remove(client);
        boolean isOwner = roomManager.removeUserFromChatRoom(client);
        return isOwner;
    }

    public synchronized void ownerDeleteRoom(ChatClient client) {
        roomManager.deleteRoom(client.getRoom());
    }


}
