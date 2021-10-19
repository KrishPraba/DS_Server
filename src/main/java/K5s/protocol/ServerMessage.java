package K5s.protocol;

import org.json.simple.JSONObject;

public class ServerMessage {

    /**
     * message to inform other servers when a room is deleted
     *
     * @param roomid roomId to be deleted
     * @param serverid current serverId
     * @return      JSONObject
     */
    public static JSONObject getDeleteRoomInform(String roomid,String serverid) {
        JSONObject delete = new JSONObject();
        delete.put("type", "deleteroom");
        delete.put("roomid", roomid);
        delete.put("serverid", serverid);
        return delete;
    }
}
