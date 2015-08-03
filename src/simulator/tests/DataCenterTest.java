package simulator.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simulator.Environment;
import simulator.Systems;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;

public class DataCenterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testDataCenterCreation() {
        final String configurationPath = new String("configs/DC_Logic.xml");
        Environment environment = new Environment();
        Systems systems = new Systems(environment);
        DataCenter dataCenter = new DataCenter(configurationPath, environment, systems);
        dataCenter.getAM();
        List<Chassis> chassis = dataCenter.getChassisSet();
        assertTrue(chassis.isEmpty());
        assertEquals(0, dataCenter.getOverRed());
        exception.expect(IndexOutOfBoundsException.class);
        dataCenter.getServer(0);
        exception.expect(IndexOutOfBoundsException.class);
        dataCenter.getServer(0, 0);
        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E8);
        
    }

}
