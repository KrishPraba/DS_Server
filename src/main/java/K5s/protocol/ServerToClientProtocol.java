package K5s.protocol;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerToClientProtocol {
    
    
    public static JSONObject getCreateRoomRequest(String roomid) {
        JSONObject create_room = new JSONObject();
        create_room.put("type", "createroom");
        create_room.put("roomid", roomid);
        return create_room;
    }
    //reply
    public static JSONObject getCreateRoomReply(String roomid,Boolean approved) {
        JSONObject create_room = new JSONObject();
        create_room.put("type", "createroom");
        create_room.put("roomid", roomid);
        create_room.put("approved", approved.toString());
        return create_room;
    }
    //    joinRoom
    //request
    public static JSONObject getJoinRoomRequest(String roomid) {
        JSONObject join = new JSONObject();
        join.put("type", "joinroom");
        join.put("roomid", roomid);
        return join;
    }
    //reply
    public static JSONObject getJoinRoomReply(String roomid) {
        JSONObject join = new JSONObject();
        join.put("type", "joinroom");
        join.put("roomid", roomid);
        return join;
    }
    //  roomChange broadcast
    public static JSONObject getRoomChangeBroadcast(String identity,String former , String roomid) {
        JSONObject change_room = new JSONObject();
        change_room.put("type", "roomchange");
        change_room.put("identity", identity);
        change_room.put("roomid", roomid);
        change_room.put("former", former);
        return change_room;
    }
    //    route user
    public static JSONObject getRouteUser(String identity,String host , String roomid,String port) {
        JSONObject route_message = new JSONObject();
        route_message.put("type", "route");
        route_message.put("roomid", roomid);
        route_message.put("host", host);
        route_message.put("port", port);
        return route_message;
    }
    //    movejoin
    //request
    public static JSONObject getMoveJoinRequest(String identity, String former, String roomid) {
        JSONObject movejoin = new JSONObject();
        movejoin.put("type", "movejoin");
        movejoin.put("identity", identity);
        movejoin.put("former", former);
        movejoin.put("roomid", roomid);
        return movejoin;
    }

    //reply
    public static JSONObject getMoveJoinReply(String identity, Boolean approved, String serverid) {
        JSONObject movejoin = new JSONObject();
        movejoin.put("type", "serverchange");
        movejoin.put("approved", approved.toString());
        movejoin.put("serverid", serverid);
        return movejoin;
    }

    //    delete room
    //request
    public static JSONObject getDeleteRoomRequest(String roomid,Boolean approved) {
        JSONObject delete = new JSONObject();
        delete.put("type", "deleteroom");
        delete.put("roomid", roomid);
        delete.put("approved", approved.toString());
        return delete;
    }

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

    //reply
    public static JSONObject getDeleteRoomRequest(String roomid) {
        JSONObject delete = new JSONObject();
        delete.put("type", "deleteroom");
        delete.put("roomid", roomid);
        return delete;
    }
    //  List
    //request
    public static JSONObject getListRequest() {
        JSONObject list = new JSONObject();
        list.put("type", "list");
        return list;
    }

    //reply
    public static JSONObject getListReply(ArrayList<String> rooms) {
        JSONObject list = new JSONObject();
        list.put("type", "roomlist");
        list.put("rooms", rooms);
        return list;
    }

    //    who
    //request
    public static JSONObject getWhoRequest() {
        JSONObject who = new JSONObject();
        who.put("type", "who");
        return who;
    }
    //reply
    public static JSONObject getWhoReply(String roomid, List<String> identities , String owner) {
        JSONObject who = new JSONObject();
        who.put("type", "roomcontents");
        who.put("roomid", roomid);
        who.put("identities", identities);
        who.put("owner", owner);
        return who;
    }

    //  message
    //request
    public static JSONObject getMessageRequest(String content) {
        JSONObject message = new JSONObject();
        message.put("type", "message");
        message.put("content", content);
        return message;
    }

    //broadcast
    public static JSONObject getMessageBroadcast(String content,String identity) {
        JSONObject message = new JSONObject();
        message.put("type", "message");
        message.put("content", content);
        message.put("identity",identity);
        return message;
    }

    //    Quit
    //request
    public static JSONObject getQuitRequest() {
        JSONObject quit = new JSONObject();
        quit.put("type", "quit");
        return quit;
    }

    //reply
    public static JSONObject quitOwnerReply(String identity, String former) {
        JSONObject change_room = new JSONObject();
        change_room.put("type", "roomchange");
        change_room.put("identity", identity);
        change_room.put("roomid", "");
        change_room.put("former", former);
        return change_room;
    }

    //    newIdentity request
    public static JSONObject getNewIdentityRequest(String identity) {
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "newidentity");
        newIdentity.put("identity", identity);
        return newIdentity;
    }
    //reply

    public static JSONObject getNewIdentityReply(String identity,Boolean approved) {
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "newidentity");
        newIdentity.put("identity", identity);
        newIdentity.put("approved", approved.toString());
        return newIdentity;
    }
}
