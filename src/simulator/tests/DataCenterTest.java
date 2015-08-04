package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.system.Systems;

public class DataCenterTest {

    @Test
    public void testDataCenterCreation() {
        Environment environment = new Environment();
        Systems systems = new Systems(environment);
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder("configs/DC.xml", environment);
        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
        DataCenter dataCenter = new DataCenter(dataCenterPOD, environment, systems);
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
