package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.ChassisPOD;

public class ChassisTest {

    @Test
    public void testChassisCreation() {
        final int chassisID = 1;
        ChassisPOD chassisPOD = new ChassisPOD();
        Chassis chassis = new Chassis(chassisPOD, chassisID);
        assertEquals(chassisID, chassis.getChassisID());
        assertEquals(0, chassis.getRackID());
        List<BladeServer> bladeServers = chassis.getServers();
        assertTrue(bladeServers.isEmpty());
    }

}
