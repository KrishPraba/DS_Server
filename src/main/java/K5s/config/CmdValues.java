package K5s.config;

import lombok.Getter;
import org.kohsuke.args4j.Option;

public class CmdValues {

    @Option(required = true, name = "-s", aliases = "--server_id", usage = "Server IP address")
    private String serverId;

    @Option(required = true, name = "-p", aliases = "--path", usage = "Server configuration file path number")
    private String configPath;

    @Option(name = "-d", aliases = "--debug", usage = "Debug mode")
    @Getter
    private boolean debug = false;

    /**
     * @return String current serverId
     */
    public String getHost() {
        return serverId;
    }

    /**
     * @return String the path to the configuration file
     */
    public String getPath() {
        return configPath;
    }
}
