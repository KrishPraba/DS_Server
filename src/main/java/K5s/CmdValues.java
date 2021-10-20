package K5s;

import org.kohsuke.args4j.Option;

public class CmdValues {
    @Option(required = true, name = "-s", aliases = "--serverid", usage = "Server ipaddress")
    private String serverid;

    @Option(required = true, name = "-p", aliases = "--path", usage = "Server configuration file pathnumber")
    private String configpath;

    @Option(name = "-d", aliases = "--debug", usage = "Debug mode")
    private final boolean debug = false;

    /**
     * @return String current serverId
     */
    public String getHost() {
        return serverid;
    }

    /**
     * @return String the path to the configuration file
     */
    public String getPath() {
        return configpath;
    }

    /**
     * @return boolean isDebug
     */
    public boolean isDebug() {
        return debug;
    }
}
