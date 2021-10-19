package K5s;

import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatServer extends Server {

    private ArrayList<Server> otherServers = new ArrayList<>();
    private Map<String,ArrayList> globalServerState;
    private ArrayList<String> globalIdentity;

    /**
     * @param serverId         serverID
     * @param ipAddress        ipaddress of the current server
     * @param clientPort       port exposed to client
     * @param coordinationPort port exposed to other servers in the system
     */
    public ChatServer(String serverId, String ipAddress, int clientPort, int coordinationPort) {
        super(serverId, ipAddress, clientPort, coordinationPort);
        globalIdentity = new ArrayList<>();
        globalServerState = new HashMap<>();
    }

    public void updateState(JSONObject gossip){
        JSONObject state = (JSONObject) gossip.get("state");
        ArrayList<String > identity = (ArrayList<String>) gossip.get("identity");
        for (String i :identity) {
            if (!globalIdentity.contains(i)){
                globalIdentity.add(i);
            }

        }
//        TODO: update server state
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
