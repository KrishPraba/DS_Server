package protocol;

import static org.junit.Assert.assertEquals;

import K5s.protocol.ServerMessage;
import org.junit.Test;

public class ServerMessageTest {

    @Test
    public void testGetElectionMessage() {

        String result = "{\"type\":\"deleteroom\",\"serverid\":\"jjj\",\"roomid\":\"iii\"}";

        assertEquals(result, ServerMessage.getDeleteRoomInform("iii", "jjj").toJSONString());
    }

}
