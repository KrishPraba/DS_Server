package K5s.protocol;

import org.json.simple.JSONObject;

public class LeaderProtocol {

    /*
      Bully Algorithm Details.
      Any process P can initiate an check.
      P sends election message to all process with higher IDS and awaits OK messages.
      If no OK messages, P becomes coordinator and sends coordinator messages to all processes.
      If it receives an OK, it drops out and waits for an coordinator.
      If a process receive an election message.
      Immediately sends coordinator message if it is the process with highest ID.
      Otherwise returns an OK and start election.
      If a process receives a coordinator message, it treats sender as a co-coordinator.
     */

    /**
     *ELECTION message
     *
     * @param serverid current serverId
     * @return   JSONObject
     */
    public static JSONObject electionMessage(String serverid) {
        JSONObject leader = new JSONObject();
        leader.put("type", "bully");
        leader.put("kind", "ELECTION");
        leader.put("serverid", serverid);
        return leader;
    }


    /**
     * OK message
     *
     * @param serverid current serverId
     * @return   JSONObject
     */
    public static JSONObject okMessage( String serverid) {
        JSONObject leader = new JSONObject();
        leader.put("type", "bully");
        leader.put("kind", "OK");
        leader.put("serverid", serverid);
        return leader;
    }

    /**
     *  COORDINATOR message
     *
     * @param serverid current serverId
     * @return   JSONObject
     */
    public static JSONObject coordinatorMessage( String serverid) {
        JSONObject leader = new JSONObject();
        leader.put("type", "bully");
        leader.put("kind", "COORDINATOR");
        leader.put("serverid", serverid);
        return leader;
    }
//    -------------------------------------------------------------------

//    get new identity approval from leader

    /**
     *
     * @param serverid current serverId
     * @param identity user identity to be checked
     * @return      JSONObject
     */
    public static JSONObject newtIdentityApprovalRequest( String serverid,String identity) {
        JSONObject leader = new JSONObject();
        leader.put("type", "requestIdentityApproval");
        leader.put("serverid", serverid);
        leader.put("identity", identity);
        return leader;
    }

    /**
     *
     * @param approved boolean true if approved else false
     * @param identity user identity to be checked
     * @return JSONObject
     */
    public static JSONObject newtIdentityApprovalReply( String approved,String identity) {
        JSONObject leader = new JSONObject();
        leader.put("type", "confirmIdentity");
        leader.put("approved", approved);
        leader.put("identity", identity);
        return leader;
    }
//    TODO:Note: identity is not in the interim report protocol


    /**
     * get new roomID approval from leader
     * @param roomid roomId to be checked
     * @param identity identity
     * @return      JSONObject
     */
    public static JSONObject newtRoomIdApprovalRequest( String roomid,String identity) {
        JSONObject leader = new JSONObject();
        leader.put("type", "requestRoomIDApproval");
        leader.put("roomid", roomid);
        leader.put("identity", identity);
        return leader;
    }

    /**
     *
     * @param approved true if approved else false
     * @param roomid    roomid of the request
     * @return      JSONObject
     */
    public static JSONObject newtRoomIdApprovalReply( String approved,String roomid) {
        JSONObject leader = new JSONObject();
        leader.put("type", "confirmRoomID");
        leader.put("approved", approved);
        leader.put("roomid", roomid);
        return leader;
    }
//    TODO:Note: roomid  is not in the interim report protocol




}
