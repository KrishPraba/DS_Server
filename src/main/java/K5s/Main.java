package K5s;

import K5s.config.CMDParser;
import K5s.config.CmdValues;
import K5s.config.ServerConfig;
import K5s.connectionManager.ClientMessageThread;
import K5s.connectionManager.ServerMessageThread;
import K5s.storage.Server;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

@Slf4j
public class Main {

    public static void main(String[] args) {


        Socket clientSocket;
        ServerSocket clientServerSocket;
        ServerSocket serverServerSocket;

        //TODO initiate a different thread with new server socket for server to server communication

        //load command line args
        CmdValues values = new CmdValues();
        CMDParser parser;
        try {
            parser = new CMDParser(values, args);
        } catch (CmdLineException e) {
            return;
        }

        String serverId = parser.getServerId();
        boolean debug = values.isDebug();
        String path = values.getPath();

        // load file values
        ChatServer meServer;
        ArrayList<Server> servers;
        try {
            ServerConfig config = new ServerConfig(path, serverId);
            meServer = config.getMeServer();
            servers = config.getServers();
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            assert meServer != null;

            /*
             * create a new clientServerSocket for client server communication
             * and listen on the port specified in the configuration file
             * allow reuse address
             * and set connection queue size to 50 : can be increased
             */
            clientServerSocket = meServer.getClientServerSocket();
            clientServerSocket.setReuseAddress(true);
            log.debug("InetServerAddress: {}, InetClientPort: {}", clientServerSocket.getInetAddress(), clientServerSocket.getLocalPort());


            serverServerSocket = meServer.getServerServerSocket();
            serverServerSocket.setReuseAddress(true);
            log.debug("InetServerAddress: {}, InetServerPort: {}", serverServerSocket.getInetAddress(), serverServerSocket.getLocalPort());

            Thread serverMessageThread = new Thread(new ServerMessageThread(serverServerSocket, meServer));
            serverMessageThread.start();

            /*
             * For new connection received to the clientPort accept and create a Socket(clientSocket)
             * log client address and port for references.
             * Create a new messageReceiveThread to handle the  clientRequest and pass the clientSocket to the thread
             */
            RoomManager roomManager = new RoomManager(meServer);
            ClientManager clientManager = new ClientManager(roomManager);
            // start sending thread

            while (true) {
                clientSocket = clientServerSocket.accept();
                log.debug("Connection received from {} to port {}", clientSocket.getInetAddress().getHostName(), clientSocket.getPort());
                Thread receiveThread = new Thread(new ClientMessageThread(clientSocket, clientManager, meServer));
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
