package K5s;

import K5s.connectionManager.ClientMessageThread;
import K5s.connectionManager.ServerMessageThread;
import K5s.storage.Server;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        boolean debug;
        ArrayList<Server> servers=new  ArrayList<>(); //list of
        ChatServer meServer = null;
        Socket clientSocket;
        ServerSocket clientServerSocket;
        ServerSocket serverServerSocket;
//        TODO initiate a different thread with new server socket for server to server communication


        //load command line args
        CmdValues values = new CmdValues();
        CmdLineParser parser = new CmdLineParser(values);
        try {
            parser.parseArgument(args);
            String serverid = values.getHost();
            System.out.println( "serverid :"+serverid);
            debug = values.isDebug();
            System.out.println("debug :"+ debug);
            String path = values.getPath();
            System.out.println( "path :"+path);

//          read configuration from file
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                String[] split = data.split("\\s+");
                System.out.println(data);
                System.out.println(split[0]);

                if (split[0].equalsIgnoreCase(serverid)){
                    meServer  =new ChatServer(split[0],split[1],Integer.parseInt(split[2]),Integer.parseInt(split[3]));
                }else{
                    servers.add(new Server(split[0],split[1],Integer.parseInt(split[2]),Integer.parseInt(split[3])));
                }
            }
            scanner.close();
            /**
             * if current server id passed from cmd line is not available in the configuration file
             * throw error
             */

            if (meServer==null){
                System.err.println("Error while parsing File specified server not found: ");
            }else{
                for (Server server :servers) meServer.addServer(server);
            }
            System.out.println("servers : " + servers.size());

        } catch (CmdLineException e) {
            System.err.println("Error while parsing cmd line arguments: " + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            assert meServer != null;
            /**
             * create a new clientServerSocket for client server communication
             * and listen on the port specified in the configuration file
             * allow reuse address
             * and set connection queue size to 50 : can be increased
             */
            clientServerSocket = meServer.getClientServerSocket();
            clientServerSocket.setReuseAddress(true);
            System.out.println("InetServerAddress : " + clientServerSocket.getInetAddress());
            System.out.println("InetClientPort: " + clientServerSocket.getLocalPort());


            serverServerSocket=meServer.getServerServerSocket();
            serverServerSocket.setReuseAddress(true);
            System.out.println("InetServerAddress : " + serverServerSocket.getInetAddress());
            System.out.println("InetServerPort: " + serverServerSocket.getLocalPort());

            Thread serverMessageThread = new Thread(new ServerMessageThread(serverServerSocket,meServer));
            serverMessageThread.start();
            /**
             * For new connection received to the clientPort accept and create a Socket(clientSocket)
             * log client address and port for references.
             * Create a new messageReceiveThread to handle the  clientRequest and pass the clientSocket to the thread
             */
            //noinspection InfiniteLoopStatement
            RoomManager roomManager = new RoomManager(meServer);
            ClientManager clientManager = new ClientManager(roomManager);
            // start sending thread

            while (true) {
                clientSocket = clientServerSocket.accept();
                System.out.println("Connection received from " + clientSocket.getInetAddress().getHostName() + "to port : " + clientSocket.getPort());
                Thread receiveThread = new Thread(new ClientMessageThread(clientSocket, clientManager,meServer));
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
