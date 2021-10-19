package K5s.protocol;

import org.json.simple.JSONObject;

import java.util.List;


public class GossipMessages {

    /**
     * * {
     * * serverRooms:{
     * serverId:[roomId]
     * },
     * * identity:{}
     * * }
     *
     * @param state
     * @param gossipServerList
     * @return
     */
    public static JSONObject gossipMessage(JSONObject state, List<String> gossipServerList) {
        JSONObject gossip = new JSONObject();
        gossip.put("state", state);
        gossip.put("gossipServerList", gossipServerList);
        return gossip;
    }

}