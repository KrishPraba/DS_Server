package K5s;

import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * receive the gossip message from the server socker and update the global state object of the local copy accordingly
     *
     * @param gossip
     */
    public void updateState(JSONObject gossip){
        Map<String,ArrayList<String>> state = (Map<String, ArrayList<String>>) gossip.get("serverRooms");
        ArrayList<String > identity = (ArrayList<String>) gossip.get("identity");

//        for each identity in the received gossip add them to the local identity list
        for (String i :identity) {
            if (!globalIdentity.contains(i)){
                globalIdentity.add(i);
            }
        }
//        for each server's room list if the record not found in the local copy update accordingly
        state.keySet().forEach(server->{
            if (server!=this.getServerId()){
                if(globalServerState.containsKey(server)){
                    for (String s :state.get(server)){
                        if (!globalServerState.get(server).contains(s)){
                            globalServerState.get(server).add(s);
                        }
                    }
                }
            }
        });
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
