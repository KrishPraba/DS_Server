package K5s.storage;

import java.util.ArrayList;

public class ChatRoom {
    private final String roomId;
    private ArrayList<ChatClient> members = new ArrayList<>();
    private final ChatClient owner;

    /**
     *
     * @param roomId unique identifier for the room
     * @param owner owner of the room
     */

    public ChatRoom(String roomId,ChatClient owner){
        this.roomId=roomId;
        this.owner=owner;
        this.members.add(owner);
    }

    public ChatRoom(String roomId, String serverid){
        this.roomId = roomId;
        this.owner = new ChatClient(serverid,null);
    }

    /**
     *
     * @return      owner of the room
     */
    public synchronized ChatClient getOwner(){
        return owner;
    }

    /**
     *
     * @return      list of users in the room
     */
    public synchronized ArrayList<ChatClient> getMembers(){
        return members;
    }

    /**
     *
     * @return list of users' ID in the room
     */

    public synchronized ArrayList<String> getUserIds(){
        ArrayList<String> userIds =new ArrayList<>();
        if (members.size()>0) {
            members.forEach(user -> userIds.add(user.getChatClientID()));
        }
        return userIds;
    }

    /**
     *
     * @return      identity of the room
     */
    public synchronized String getRoomId(){
        return roomId;
    }

    /**
     *
     * @param user add user to the room
     */

    public synchronized boolean addMember(ChatClient user){
        if (!members.contains(user)){
            members.add(user);
            return true;
        }
        else{
            return false;
        }
    }

    /**
     *
     * @param user user to be removed from the room
     */
    public synchronized boolean removeMember(ChatClient user){
        if (user.equals(owner)){
            return false;
        }
        members.remove(user);
        return true;
    }
}
