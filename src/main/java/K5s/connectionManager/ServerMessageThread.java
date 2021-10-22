package K5s.connectionManager;

import K5s.ChatServer;
import K5s.ClientManager;
import K5s.ServerManager;
import K5s.storage.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static K5s.protocol.GossipMessages.*;
import static K5s.protocol.LeaderProtocol.*;

public class ServerMessageThread implements Runnable{

    private ServerSocket serverServerSocket;
//    private final ChatServer meServer;
    private BufferedReader in;
    private JSONParser parser = new JSONParser();
    private final AtomicBoolean running=new AtomicBoolean(true);
    private ServerManager manager;
//    private Timer timer = new Timer();
//    private DataOutputStream out;


    public ServerMessageThread(ServerSocket serverServerSocket, ServerManager manager) throws IOException {
        this.serverServerSocket=serverServerSocket;
        this.manager = manager;
    }
    @Override
    public void run() {
        try {
            manager.initiateLeaderElection();
            while (this.running.get()){
                Socket serverSocket = serverServerSocket.accept();
                System.out.println("Connection received from Server" + serverSocket.getInetAddress().getHostName() + "to port : " + serverSocket.getPort());

                this.in=new BufferedReader(new InputStreamReader(serverSocket.getInputStream(),StandardCharsets.UTF_8));
                JSONObject message;
                message=(JSONObject) parser.parse(in.readLine());
                System.out.println("Receiving Server Message: " + message);
                MessageReceive(message);
                }
                this.in.close();
//                System.out.println("socket closed");
//                this.serverServerSocket.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    private  void MessageReceive(JSONObject message) throws IOException {
        String type = (String) message.get("type");
        String kind =(String) message.get("kind");
//        System.out.println(kind);
        switch (type){
            case "bully":
                String serverId = (String) message.get("serverid");
                switch (kind) {
                    case "ELECTION":

                        if (serverId.compareTo(manager.getMeServer().getServerId()) < 0){
                            try{
                                send(okMessage(manager.getMeServer().getServerId()), serverId);
                                if(manager.getMeServer().checkLeader()){
                                    send(coordinatorMessage(manager.getMeServer().getServerId()), serverId);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("OK AND OR COORDINATOR Server " + serverId + " is down");
                            }
                        } else {
                            manager.initiateLeaderElection();
                        }

                        break;

                    case "COORDINATOR":
//                        timer.cancel();
                        if (serverId.compareTo(manager.getMeServer().getServerId()) < 0){
                            manager.initiateLeaderElection();
                        } else{
                            manager.getMeServer().setLeader(serverId);
                            manager.getMeServer().setElectionInProgress(false);
                            System.out.println(serverId+ " has been elected as Leader.");
                        }
                        break;

                    case "OK":
                        manager.getMeServer().setisOkMessageReceived(true);
                        System.out.println("OK received from "+serverId);
                        manager.getMeServer().setElectionInProgress(true);
                        manager.getMeServer().setisOkMessageReceived(false);
//                        Timer timer = new Timer();
//                        timer.schedule(task, 4000);
                        break;
                    default:
                        System.out.println(message + " not configured");
                }
                break;
            case "gossip":
                switch (kind){
                    case "stateUpdate":
                        JSONObject state = (JSONObject) message.get("state");
                        manager.getMeServer().updateState(state);
                        JSONArray neighbour=(JSONArray) message.get("gossipServerList");
                        System.out.print("List of gossipServerList rooms:" +neighbour);
                        neighbour.remove(manager.getMeServer().getServerId());
                        for (int i = 0; i < neighbour.size(); i++) {
                            System.out.print(" " + neighbour.get(i));
                        }
                        String gossipNeighbour= (String) neighbour.get(new Random().nextInt(neighbour.size()));
                        message = gossipMessage(state,neighbour);
                        try {
                            send(message,gossipNeighbour);
                        }catch (IOException ex){
                            gossipNeighbour= (String) neighbour.get(new Random().nextInt(neighbour.size()));
                            try {
                                send(message, gossipNeighbour);
                            }catch (IOException e){
//                                TODO :report server failure
                            }
                        }
                        break;
                    default:
                        System.out.println(message + "not configured");
                }
                break;
            case "confirmIdentity":
                String identity = (String) message.get("identity");
                System.out.println("received confirm identity for : "+identity+" from leader");
                boolean approved = (boolean) message.get("approved");
                ClientManager.replyIdentityRequest(identity,approved);
                break;
            case "requestIdentityApproval":
                String i = (String) message.get("identity");
                String serverid = (String) message.get("serverid");
                System.out.println("received identity approval req for : "+i+" from server : "+serverid);
                if(manager.getMeServer().getGlobalIdentity().contains(i)){
                    //Assumption : this case message will only be received by leader.
                    send(newIdentityApprovalReply(false,i),serverid);
                }else{
                    manager.getMeServer().addNewIdentity(i);
                    send(newIdentityApprovalReply(true,i),serverid);
                    try {
                        send(gossipMessage(manager.getMeServer().getState(), manager.getMeServer().getOtherServerIdJSONArray()), manager.getMeServer().getRandomeNeighbour());
                    } catch (IOException e){
                        try {
                            send(gossipMessage(manager.getMeServer().getState(), manager.getMeServer().getOtherServerIdJSONArray()), manager.getMeServer().getRandomeNeighbour());
                        }catch (IOException ioException){
//                            TODO :detect failure
                        }
                    }
//                    TODO:update Leader state and the methode updates the leader state should initiate the gossip
                }
            default:
                System.out.println(message + "not configured");
        }
    }
    private void send(JSONObject obj,String serverId) throws IOException {
        Server server=manager.getMeServer().getServer(serverId);
        if (server!=null){
            Socket ss=server.getSocket();
            System.out.println(ss.getPort());
            OutputStream out = ss.getOutputStream();
            out.write((obj.toString() + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
//            out.close();
        }
        System.out.println("Reply :" + obj );

//        out.flush();
//        out.close();
    }

//    TimerTask task = new TimerTask() {
//        public void run() {
//            if (meServer.getElectionInProgress() && meServer.isOkMessageReceived()){
//                initiateLeaderElection();
//            }
//        }
//    };


}
