package K5s.config;

import K5s.ChatServer;
import K5s.storage.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

@Slf4j
public class ServerConfig {

    @Getter
    private ChatServer meServer;

    @Getter
    ArrayList<Server> servers = new ArrayList<>();

    public ServerConfig(String path, String serverId) throws FileNotFoundException {

        File file = new File(path);

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {

                String data = scanner.nextLine();
                String[] split = data.split("\\s+");

                log.debug(data);
                log.debug(split[0]);

                if (split[0].equalsIgnoreCase(serverId)) {
                    meServer = new ChatServer(split[0], split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                    servers.add(meServer);
                } else {
                    servers.add(new Server(split[0], split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3])));
                }
            }

        } catch (FileNotFoundException e) {
            log.error("Server configuration file could not be found");
            throw new FileNotFoundException("Server configuration file could not be found");
        }

        if (meServer == null) {
            log.error("Error while parsing file. Specified server not found!");
        } else {
            for (Server server : servers) meServer.addServer(server);
        }
        log.debug("server count: {}", servers.size());
    }
}
