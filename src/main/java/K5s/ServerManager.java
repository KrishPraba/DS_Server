package K5s;

import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static K5s.protocol.GossipMessages.gossipMessage;
import static K5s.protocol.LeaderProtocol.*;

public class ServerManager {
    private static ServerManager instance;
    private static ChatServer meServer;

    private ServerManager(ChatServer meServer){
        this.meServer=meServer;
    }

    /**
     * returns true if the identity might get approved else false
     * @param identity
     * @return
     */
    public static String  isAvailableIdentity(String identity){
        if(meServer.getGlobalIdentity().contains(identity)){
            return "FALSE";
        }else {
            System.out.println("Leader : "+meServer.getLeader());
            if (!meServer.isLeader()){
                JSONObject newtIdentityApprovalRequest = newtIdentityApprovalRequest(meServer.getServerId(),identity);
                if(!meServer.getLeader().isEmpty()){
                    try {
                        send(newtIdentityApprovalRequest,meServer.getLeader());
                        return "WAITING";
                    } catch (IOException e) {
                        e.printStackTrace();
                        initiateLeaderElection();
                        return "FALSE";
                        //this it just an availability measure
                    }
                }
            }else {
                return "TRUE";
            }
        }
        return "FALSE";
    }
    public static  String getServerIdOfRoom(String roomid){
        Map<String, ArrayList<String>> gS = meServer.getGlobalServerState();
        for (String key : gS.keySet()) {
            for (String room : gS.get(key)) {
                if (room == roomid) {
                    return key;
                }
            }
        }
        return null;
    }
    public static String  isAvailableRoomId(String roomid){
        if(getServerIdOfRoom(roomid)!=null){
            return "FALSE";
        }else {
            System.out.println("Leader is : "+meServer.getLeader());
            if (!meServer.isLeader()){
                JSONObject newtRoomIdApprovalRequest = newtRoomIdApprovalRequest(roomid,meServer.getServerId());
                if(!meServer.getLeader().isEmpty()){
                    try {
                        send(newtRoomIdApprovalRequest,meServer.getLeader());
                        return "WAITING";
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "FALSE";
                        //this it just an availability measure
                    }
                }else {
                    initiateLeaderElection();
//                    TODO:electionmessage
                }
            }else {
                return "TRUE";
            }
        }
        return "FALSE";
    }


    public static ServerManager getInstance(ChatServer meserver){
        if (instance==null){
            instance=new ServerManager(meserver);
        }
        return instance;
    }

    public static void initiateLeaderElection(){
        meServer.setElectionInProgress(true);
        meServer.setisOkMessageReceived(false);
        ArrayList<Server> otherServers = meServer.getOtherServers();
        int count = 0;
        for(Server s : otherServers){
            if (s.getServerId().compareTo(meServer.getServerId()) > 0){
                try{
                    send(electionMessage(meServer.getServerId()), s.getServerId());
                    count += 1;
                }catch(IOException e){
                    System.out.println("ELECTION Server "+s.getServerId()+ "is down");
                }
            }
        }

        if(count == 0){

            for (Server s: otherServers) {
                try{
                    send(coordinatorMessage(meServer.getServerId()), s.getServerId());
                } catch(IOException e) {
                    System.out.println("COORDINATOR Server "+s.getServerId()+ " is down");
                }
            }

            meServer.setLeader(meServer.getServerId());
            meServer.setElectionInProgress(false);
            System.out.println(meServer.getServerId()+ " is Leader");
        }
//        timer.schedule(task, 4000);
    }
    public static void gossipState()  {
        try {
            send(gossipMessage(meServer.getState(), meServer.getOtherServerIdJSONArray()), meServer.getRandomeNeighbour());
        } catch (IOException e){
            try {
                send(gossipMessage(meServer.getState(), meServer.getOtherServerIdJSONArray()), meServer.getRandomeNeighbour());
            }catch (IOException ioException){
//                            TODO :detect failure
            }
        }
    }

    public static ChatServer getMeServer() {
        return meServer;
    }

    public static void sendBroadcast(JSONObject message){
        for (Server s:
             meServer.getOtherServers()) {
            try {
                send(message,s.getServerId());
            } catch (IOException e) {
                System.out.println("BROADCAST Server " + s.getServerId() + " is down");
                if (s.getServerId() == meServer.getLeader()){
                    initiateLeaderElection();
                }
            }
        }
    }

    private static void send(JSONObject obj, String serverId) throws IOException {
        Server server=meServer.getServer(serverId);
        if (server!=null){
            Socket ss=server.getSocket();
            System.out.println(ss.getPort());
            OutputStream out = ss.getOutputStream();
            out.write((obj.toString() + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
        System.out.println("Reply :" + obj );

//        out.flush();
//        out.close();
    }
}
