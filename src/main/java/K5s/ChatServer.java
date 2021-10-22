package K5s;

import K5s.storage.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatServer extends Server {

    private ArrayList<Server> otherServers = new ArrayList<>();
    private Map<String,ArrayList<String>> globalServerState;
    private ArrayList<String> globalIdentity;
    private static boolean isLeader = false;
    private String leader;
    private boolean electionInProgress = false;
    private boolean isOkMessageReceived =false;
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
    public void addNewIdentity(String identity){
        globalIdentity.add(identity);
//      TODO: need to validate the identity pattern and only approve valid pattern(no nessary since the server itself check for this)

    }

    /**
     * receive the gossip message from the server socket and update the global state object of the local copy accordingly
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
    public JSONObject getState(){
        JSONObject state =new JSONObject();

        state.put("serverRooms",new JSONObject(globalServerState));
        state.put("identity", getIdentityJSONArray());
        return state;
    }
    public String getRandomeNeighbour(){
        return getOtherServerIdList().get(new Random().nextInt(otherServers.size()));
    }
    public String getLeader(){
        return  this.leader;
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
    public ArrayList<String> getOtherServerIdList(){
        ArrayList<String> a= new ArrayList<>();
        otherServers.forEach(s->a.add(s.getServerId()));
        return  a;
    }
    public JSONArray getOtherServerIdJSONArray(){
        JSONArray o =new JSONArray();
        otherServers.forEach(s->o.add(s.getServerId()));
        return  o;
    }
    public JSONArray getIdentityJSONArray(){
        JSONArray o = new JSONArray();
        globalIdentity.forEach(i->o.add(i));
        return  o;
    }
    public ArrayList<Server> getOtherServers(){
        return this.otherServers;
    }

    public void setLeader(String serverId){
        if (serverId.compareTo(this.getServerId()) == 0){
            this.isLeader = true;
        }
        else{
            this.isLeader = false;
        }
        this.leader = serverId;
        System.out.println(serverId+" is the leader and "+this.getServerId() +" is Leader : " +isLeader());
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

    public boolean isOkMessageReceived() {
        return isOkMessageReceived;
    }

    public void setisOkMessageReceived(boolean x){
        this.isOkMessageReceived = x;
    }
    public static boolean isLeader(){
        return isLeader;
    }
    public ArrayList<String> getGlobalIdentity(){
        return this.globalIdentity;
    }
    public void addRoom(String server,String room){
        if(globalServerState.containsKey(server)){
            ArrayList<String> s =globalServerState.get(server);
            if(!s.contains(room)){
                s.add(room);
            }
        }else{
            ArrayList<String> r=new ArrayList<>();
            r.add(room);
            globalServerState.put(server,r);
        }
    }

    public ArrayList<String> getRooms() {
        ArrayList<String> r = new ArrayList<>();
        globalServerState.keySet().forEach(key ->
                globalServerState.get(key).forEach(room ->
                        r.add(room)
                )
        );
        return r;
    }
}
