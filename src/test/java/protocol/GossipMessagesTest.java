package protocol;

import static org.junit.Assert.assertEquals;

import K5s.protocol.GossipMessages;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GossipMessagesTest {

    private JSONObject state;

    private JSONArray gossipServerList;

    @Test
    public void testGossipMessage() {
        // some random state
        state = new JSONObject();
        state.put("owner", "s1");
        state.put("identities", Arrays.asList("adel", "maria"));
        state.put("type", "roomcontents");
        state.put("roomid", "MainHall-s1");

        gossipServerList = new JSONArray();
        gossipServerList.addAll(List.of(new String[]{"222", "333"}));

        String result = "{\"gossipServerList\":[\"222\",\"333\"],\"kind\":\"stateUpdate\",\"state\":{\"owner\":\"s1\",\"identities\":[\"adel\",\"maria\"],\"type\":\"roomcontents\",\"roomid\":\"MainHall-s1\"},\"type\":\"gossip\"}";

        assertEquals(result, GossipMessages.gossipMessage(state, gossipServerList).toJSONString());
    }

}
