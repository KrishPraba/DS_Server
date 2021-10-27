package K5s.protocol;

import org.json.simple.JSONObject;

public class ServerToServerProtocol {


    public static JSONObject sendDeleteIdenity(String identity) {
        JSONObject delete_identity = new JSONObject();
        delete_identity.put("type", "deleteidenity");
        delete_identity.put("identity", identity);
        return delete_identity;
    }

    public static JSONObject sendDeleteRoom(String roomId, String serverId) {
        JSONObject delete_identity = new JSONObject();
        delete_identity.put("type", "deleteroom");
        delete_identity.put("roomid", roomId);
        delete_identity.put("serverid", serverId);
        return delete_identity;
    }
}
