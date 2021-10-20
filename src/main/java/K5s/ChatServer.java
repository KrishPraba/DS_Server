package K5s;

import K5s.storage.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static K5s.protocol.LeaderProtocol.coordinatorMessage;
import static K5s.protocol.LeaderProtocol.electionMessage;

public class ChatServer extends Server {

    private ArrayList<Server> otherServers = new ArrayList<>();
    private Map<String,ArrayList<String>> globalServerState;
    private ArrayList<String> globalIdentity;
    private boolean isLeader = false;
    private String leader;
    private boolean electionInProgress = false;

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
        Map<String,JSONArray> state = (Map<String, JSONArray>) gossip.get("serverRooms");
        JSONArray identity = (JSONArray) gossip.get("identity");

//        for each identity in the received gossip add them to the local identity list
        for (Object i :identity) {
            String identityString= (String) i;
            if (!globalIdentity.contains(identityString)){
                globalIdentity.add(identityString);
            }
        }
//        for each server's room list if the record not found in the local copy update accordingly
        state.keySet().forEach(server->{
            if (server!=this.getServerId()){
                if(globalServerState.containsKey(server)){
                    for (Object s :state.get(server)){
                        String serverString= (String) s;
                        if (!globalServerState.get(server).contains(serverString)){
                            globalServerState.get(server).add(serverString);
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

    /**
     * get Server from serverId
     * @param id  serverid
     * @return Server
     */
    public Server getServer(String id){
        for(Server server:otherServers){
            if (server.getServerId().equals(id)){
                return server;
            }
        }
        return null;
    }

    public ArrayList<Server> getOtherServers(){
        return this.otherServers;
    }

    public void setLeader(String serverId){
        if (serverId.compareTo(this.getServerId()) == 0){
            this.isLeader = true;
        }
        this.leader = serverId;
    }

    public boolean checkLeader(){
        return this.isLeader;
    }

    public void setElectionInProgress(boolean x){
        this.electionInProgress = x;
    }

    public boolean getElectionInProgress(){
        return this.electionInProgress;
    }
}
