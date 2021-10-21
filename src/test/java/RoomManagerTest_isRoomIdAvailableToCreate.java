import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import K5s.RoomManager;
import K5s.storage.Server;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;

@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class RoomManagerTest_isRoomIdAvailableToCreate {

    @NonNull
    String roomId;

    @NonNull
    boolean actual;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Server meserver;

    RoomManager roomManager;

    @Before
    public void init() {
        given(meserver.getServerId()).willReturn("s1");
        roomManager = new RoomManager(meserver);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> input() {
        return Arrays.asList(new Object[][]{
                {"MainHall-s1", false},
                {"ap", false},
                {"-IJJY", false},
                {"RoomIdWithLengthGreaterThanSixteen", false},
                {"True", true},
                {"0337", false}
        });
    }

    @Test
    public void testIsRoomIdAvailableToCreate() {
        assertEquals(roomManager.isRoomIdAvailableToCreate(roomId), actual);
    }
}
