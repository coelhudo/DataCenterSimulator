package simulator.physical;

import java.util.List;

public final class BladeServerCollectionOperations {

    public static void runAllServers(List<BladeServer> bladeServers) {
        for(BladeServer bladeServer : bladeServers) {
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
}
