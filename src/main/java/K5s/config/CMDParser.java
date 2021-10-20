package K5s.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

@Slf4j
public class CMDParser {

    @Getter
    private String serverId;

    @Getter
    private String configPath;

    @Getter
    private boolean debug = false;


    public CMDParser(CmdValues values, String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(values);

        try {

            parser.parseArgument(args);

            serverId = values.getHost();
            debug = values.isDebug();
            configPath = values.getPath();

            log.debug("Server ID: {}, Path: {}, Debug: {}", serverId, configPath, debug);

        } catch (CmdLineException exception) {
            log.error("Error while parsing cmd line arguments: {}", exception.getLocalizedMessage());
            throw new CmdLineException(parser, "Error while parsing cmd line arguments", exception);
        }
    }
}
