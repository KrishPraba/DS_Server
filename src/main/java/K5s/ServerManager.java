package K5s;

import K5s.connectionManager.ServerMessageThread;
import K5s.storage.Server;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static K5s.protocol.LeaderProtocol.*;

public class ServerManager {
    private static ServerManager instance;
    private static ChatServer meServer;

    private ServerManager(ChatServer meServer){
        this.meServer=meServer;
    }
    public static boolean isAvailableIdentity(String identity){
        if(meServer.getGlobalIdentity().contains(identity)){
            return false;
        }else {
            if (meServer.isLeader()){
                JSONObject newtIdentityApprovalRequest = newtIdentityApprovalRequest(meServer.getServerId(),identity);
                if(!meServer.getLeader().isEmpty()){
                    try {
                        send(newtIdentityApprovalRequest,meServer.getLeader());
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                        //this it just an availability measure
                    }
                }else {
//                    TODO:electionmessage
                }
            }
        }
        return false;
    }

    public static ServerManager getInstance(ChatServer meserver){
        if (instance==null){
            instance=new ServerManager(meserver);
        }
        return instance;
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
