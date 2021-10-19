package K5s;

import K5s.storage.Server;

import java.util.ArrayList;

public class ChatServer extends Server {

    private ArrayList<Server> otherServers = new ArrayList<>();

    /**
     * @param serverId         serverID
     * @param ipAddress        ipaddress of the current server
     * @param clientPort       port exposed to client
     * @param coordinationPort port exposed to other servers in the system
     */
    public ChatServer(String serverId, String ipAddress, int clientPort, int coordinationPort) {
        super(serverId, ipAddress, clientPort, coordinationPort);
    }

    /**
     * add other servers in the system
     *
     * @param server  server to be added
     */
    public synchronized void addServer(Server server) {
        this.otherServers.add(server);
    }

}
