package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;

public class ChassisTest {

    @Test
    public void testChassisCreation() {
        Environment environment = new Environment();
        final int chassisID = 1;
        Chassis chassis = new Chassis(chassisID, environment);
        assertEquals(chassisID, chassis.getChassisID());
        assertEquals(0, chassis.getRackID());
        List<BladeServer> bladeServers = chassis.getServers();
        assertTrue(bladeServers.isEmpty());
    }

}
