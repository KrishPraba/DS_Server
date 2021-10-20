package K5s.connectionManager;

import static K5s.protocol.GossipMessages.gossipMessage;

import K5s.ChatServer;
import K5s.storage.Server;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ServerMessageThread implements Runnable {

    private final ChatServer meServer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ServerSocket serverServerSocket;
    private BufferedReader in;
    private JSONParser parser = new JSONParser();
    private DataOutputStream out;

    public ServerMessageThread(ServerSocket serverServerSocket, ChatServer meServer) throws IOException {
        this.serverServerSocket = serverServerSocket;
        this.meServer = meServer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket serverSocket = serverServerSocket.accept();
                log.debug("Connection received from Server {} to port {}.", serverSocket.getInetAddress().getHostName(), serverSocket.getPort());
                this.in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message;
                while (this.running.get()) {
                    message = (JSONObject) parser.parse(this.in.readLine());
                    System.out.println("Receiving Client Message: " + message);
                    log.debug("Receiving Client Message: {}", message);
                    MessageReceive(message);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void MessageReceive(JSONObject message) throws IOException {
        String type = (String) message.get("type");
        String kind = (String) message.get("kind");

        switch (type) {

            case "bully":
                String serverId = (String) message.get("serverid");
                switch (kind) {
                    case "ELECTION":
//                        if (serverId.hashCode()<meServer.getServerId().hashCode()) {
//                            send(getOkMessage(meServer.getServerId()));
//                        }
//                        break;
                    case "COORDINATOR":
//                        if (serverId.hashCode()> meServer.getServerId().hashCode()){
//                            Leader.setLeader(meServer.getServer(serverId));
//                        }
//                        else{
//                            send(getCoordinatorMessage(meServer.getServerId()));
//                        }
//                        break;
                    case "OK":
                        break;
                    default:
                        log.info("{} not configured", message);
                }

            case "gossip":

                switch (kind) {

                    case "stateUpdate":

                        JSONObject state = (JSONObject) message.get("state");
                        meServer.updateState(state);
                        JSONArray neighbour = (JSONArray) message.get("gossipServerList");
                        neighbour.remove(meServer.getServerId());

                        log.info("List of gossipServerList rooms: {}", neighbour.toJSONString());

                        String gossipNeighbour = (String) neighbour.get(new Random(neighbour.size()).nextInt());
                        gossipMessage(state, neighbour);
                        send(message, gossipNeighbour);
                        break;

                    default:
                        System.out.println(message + "not configured");
                        log.info("{} not configured", message);
                }

            default:
                log.info("{} not configured", message);
        }
    }

    private void send(JSONObject obj, String serverId) throws IOException {
        log.debug("Reply: {}", obj);
        Server server = meServer.getServer(serverId);
        if (server != null) {
            server.getSocket().getOutputStream().write((obj.toString() + "\n").getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
        out.close();
    }
}
