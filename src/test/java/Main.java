import lombok.extern.slf4j.Slf4j;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

@Slf4j
public class Main {
    public static void main(String[] args) {

        Result result = JUnitCore.runClasses(

        );

        for (Failure failure : result.getFailures()) {
            log.error(failure.toString());
        }

        if (result.wasSuccessful()) log.info("Successfully completed all tests");
        else log.warn("Tests failed");
    }
}
