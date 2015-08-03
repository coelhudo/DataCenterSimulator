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
import simulator.physical.DataCenterBuilder;

public class DataCenterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testDataCenterCreation() {
        final String configurationPath = new String("configs/DC.xml");
        Environment environment = new Environment();
        Systems systems = new Systems(environment);
        DataCenter dataCenter = new DataCenter(new DataCenterBuilder(configurationPath, environment), environment, systems);
        dataCenter.getAM();
        List<Chassis> chassis = dataCenter.getChassisSet();
        assertFalse(chassis.isEmpty());
        assertEquals(50, chassis.size());
        assertEquals(0, dataCenter.getOverRed());
        assertNotNull(dataCenter.getServer(0));
        assertNotNull(dataCenter.getServer(0, 0));
        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E8);
        
    }

}
