package simulator.physical;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

public final class BladeServerCollectionOperations {

    public static final Logger LOGGER = Logger.getLogger(BladeServerCollectionOperations.class.getName());
    
    public static void runAll(List<BladeServer> bladeServers) {
        for (BladeServer bladeServer : bladeServers) {
            bladeServer.run();
        }
    }

    public static int totalFinishedJob(List<BladeServer> bladeServers) {
        int result = 0;
        for (BladeServer bladeServer : bladeServers) {
            result += bladeServer.getTotalFinishedJob();
        }
        return result;
    }
    
    public static double totalResponseTime(List<BladeServer> bladeServers) {
        double result = 0;
        for (BladeServer bladeServer : bladeServers) {
            result += bladeServer.getResponseTime();
        }
        return result;
    }

    public static boolean allIdle(List<BladeServer> bladeServers) {
        for (BladeServer bladeServer : bladeServers) {
            if (!bladeServer.isIdle()) {
                return false;
            }
        }
        return true;
    }

    public static int countIdle(List<BladeServer> bladeServers) {
        int count = 0;
        for (BladeServer bladeServer : bladeServers) {
            if (bladeServer.isIdle()) {
                count++;
            }
        }
        return count;
    }

    public static int countRunning(List<BladeServer> bladeServers) {
        int count = 0;
        for (BladeServer bladeServer : bladeServers) {
            if (bladeServer.isRunningBusy() || bladeServer.isRunningNormal()) {
                count++;
            }
        }
        return count;
    }
}
