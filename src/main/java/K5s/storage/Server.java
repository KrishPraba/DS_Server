package K5s.storage;

import java.util.ArrayList;

public class Server {
    private final String serverId;
    private final String ipAddress;
    private final int clientPort ;
    private final int coordinationPort ;
    private ArrayList<String> roomIds;

    /**
     *
     * @param serverId  serverID
     * @param ipAddress ipaddress of the current server
     * @param clientPort port exposed to client
     * @param coordinationPort port exposed to other servers in the system
     */
    public Server(String serverId, String ipAddress ,int clientPort ,int coordinationPort){
        this.clientPort=clientPort;
        this.coordinationPort=coordinationPort;
        this.serverId=serverId;
        this.ipAddress=ipAddress;
        this.roomIds = new ArrayList<>();
    }

    public synchronized void addRoomId(String roomId) {
        this.roomIds.add(roomId);
    }

    public synchronized void deleteRoomId(String roomId){
        this.roomIds.remove(roomId);
    }

    public synchronized ArrayList<String> getRoomIds(){
        return this.roomIds;
    }
    /**
     *
     * @return      current serverID
     */
    public synchronized String getServerId() {
        return this.serverId;
    }

    /**
     *
     * @return      ipAddress of the current server
     */
    public synchronized String getIpAddress() {
        return this.ipAddress;
    }

    /**
     *
     * @return      clientPort
     */
    public synchronized int getClientPort() {
        return this.clientPort;
    }

    /**
     *
     * @return coordinator port
     */
    public synchronized int getCoordinationPort() {
        return this.coordinationPort;
    }

}
