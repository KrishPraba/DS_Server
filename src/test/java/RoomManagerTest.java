import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

import K5s.RoomManager;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import K5s.storage.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class RoomManagerTest {

    private RoomManager roomManager;

    @Mock
    private Server meserver;

    @Mock
    private ChatClient client;

    @Mock
    private ChatClient other;

    private ChatRoom room;

    @Before
    public void init() {
        given(meserver.getServerId()).willReturn("s1");
        roomManager = new RoomManager(meserver);
    }

    @Test
    public void testGetMainHall() {
        ChatRoom actual = roomManager.getMainHall();
        assertEquals("MainHall-s1", actual.getRoomId());
        assertEquals("s1", actual.getOwner().getChatClientID());
    }

    @Test
    public void testGetRoomIds() {
        ArrayList<String> roomIds = roomManager.getRoomIds();
        assertEquals(roomIds.size(), 1);
        assertEquals(roomIds.get(0), "MainHall-s1");
    }

    @Test
    public void testCreateRoom() {
        room = roomManager.createRoom("someRoomID", client);
        assertEquals("someRoomID", room.getRoomId());
    }

    @Test
    public void testFindOwnerOfRoom_exists() {
        room = roomManager.createRoom("someRoomID", client);
        assertEquals(client, roomManager.findOwnerOfRoom("someRoomID"));
    }

    @Test
    public void testFindOwnerOfRoom_null() {
        room = roomManager.createRoom("someRoomID", client);
        assertNull(roomManager.findOwnerOfRoom("someOtherRoomID"));
    }

    @Test
    public void testFindIfOwner() {
        room = roomManager.createRoom("someRoomID", client);
        assertTrue(roomManager.findIfOwner(client));
        assertFalse(roomManager.findIfOwner(other));
    }


    @Test
    public void testRemoveUserFromChatRoom_someOtherUser() {

        room = roomManager.createRoom("someRoomID", client);

        room.addMember(other);
        given(other.getRoom()).willReturn(room);

        boolean actual = roomManager.removeUserFromChatRoom(other);
        assertFalse(actual);
    }

    @Test
    public void testRemoveUserFromChatRoom_owner() {

        room = roomManager.createRoom("someRoomID", client);
        given(client.getRoom()).willReturn(room);

        boolean actual = roomManager.removeUserFromChatRoom(client);
        assertTrue(actual);
    }

    @Test
    public void testFindRoomExists_positive() {
        room = roomManager.createRoom("someRoomID", client);
        assertEquals(room, roomManager.findRoomExists("someRoomID"));
    }

    @Test
    public void testFindRoomExists_negative() {
        room = roomManager.createRoom("someRoomID", client);
        roomManager.deleteRoom(room);
        assertNull(roomManager.findRoomExists("someRoomID"));
    }
}
