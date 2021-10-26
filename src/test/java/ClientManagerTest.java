import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import K5s.ClientManager;
import K5s.RoomManager;
import K5s.connectionManager.ClientMessageThread;
import K5s.storage.ChatClient;
import K5s.storage.ChatRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class ClientManagerTest {

    @Mock
    private RoomManager roomManager;

    @Mock
    private ClientMessageThread clientMessageThread;

    @Mock
    private ChatRoom mainHall;

    private ClientManager clientManager;

    @Before
    public void init() {
        clientManager = new ClientManager(roomManager);
    }

    @Test
    public void testIsAvailableIdentity_true() {
        boolean actual = clientManager.isAvailableIdentity("someIdentity");
        assertTrue(actual);
    }

    @Test
    public void testNewIdentity_returnClient() {
        given(roomManager.getMainHall()).willReturn(mainHall);
        ChatClient client = clientManager.newIdentity("someIdentity", clientMessageThread);
        assertNotNull(client);
    }

    @Test
    public void testNewIdentity_returnNull() {
        given(roomManager.getMainHall()).willReturn(mainHall);
        ChatClient client = clientManager.newIdentity("someIdentity", clientMessageThread);
        ChatClient other = clientManager.newIdentity("someIdentity", clientMessageThread);
        assertNull(other);
    }

    @Test
    public void testListRoomIds() {
        ArrayList<String> actual = new ArrayList<>(Arrays.asList("r1", "r2", "r3"));
        given(roomManager.getRoomIds()).willReturn(actual);
        assertEquals(clientManager.listRoomIds(), actual);
    }
}
