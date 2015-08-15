package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

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
        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E-8);
    }
    
    @Test 
    public void testCalculatePower() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.power()).thenReturn(150.0);
        dataCenterPOD.appendChassis(mockedChassis);
        dataCenterPOD.setD(0, 0, 200.0);
        
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedEnvironment, mockedSystems);
        
        dataCenter.calculatePower();
        
        assertEquals(150.00002450, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        
        verify(mockedChassis, times(2)).power();
        
        verifyNoMoreInteractions(mockedChassis);
    }

}
