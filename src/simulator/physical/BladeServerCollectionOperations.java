package simulator.physical;

import java.util.List;

public final class BladeServerCollectionOperations {

    public static void runAllServers(List<BladeServer> bladeServers) {
        for(BladeServer bladeServer : bladeServers) {
            bladeServer.run();
        }
    }
}
