package K5s.protocol;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;


public class GossipMessages {

    /**
     * {
     *    state:{
     *          serverRooms:{
     *              serverId:[roomId]
     *          },
     *          identity:{}
     *   }
     *   gossipServerList:[<remaining servers to be gossiped>]
     *
     * @param state
     * @param gossipServerList
     * @return
     */
    public static JSONObject gossipMessage(JSONObject state, JSONArray gossipServerList) {
        JSONObject gossip = new JSONObject();
        gossip.put("type", "gossip");
        gossip.put("kind","stateUpdate");
        gossip.put("state", state);
        gossip.put("gossipServerList", gossipServerList);
        return gossip;
    }
}