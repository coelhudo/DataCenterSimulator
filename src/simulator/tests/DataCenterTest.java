package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.physical.ActivitiesLogger;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.system.Systems;

public class DataCenterTest {

    @Test
    public void testDataCenterCreation() {
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder("configs/DC.xml", mockedEnvironment);
        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
        dataCenter.getAM();
        List<Chassis> chassis = dataCenter.getChassisSet();
        assertFalse(chassis.isEmpty());
        assertEquals(50, chassis.size());
        assertEquals(0, dataCenter.getOverRed());
        assertNotNull(dataCenter.getServer(0));
        assertNotNull(dataCenter.getServer(0, 0));
        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        
        verifyNoMoreInteractions(mockedActivitiesLogger, mockedEnvironment, mockedEnvironment);
    }
    
    @Test 
    public void testCalculatePowerSlowingDownFromCooler() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.power()).thenReturn(150.0);
        dataCenterPOD.appendChassis(mockedChassis);
        dataCenterPOD.setD(0, 0, 200.0);
        dataCenterPOD.setRedTemperature(0);
        
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
        
        dataCenter.calculatePower();
        
        assertEquals(150.00002450, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertTrue(dataCenter.getAM().isSlowDownFromCooler());
        
        verify(mockedChassis, times(2)).power();
        
        verify(mockedChassis, times(2)).power();
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedSystems).getComputeSystems();
        
        verifyNoMoreInteractions(mockedChassis, mockedActivitiesLogger, mockedEnvironment, mockedSystems);        
    }
    
    @Test 
    public void testCalculatePowerNotSlowingDownFromCooler() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.power()).thenReturn(1.0);
        dataCenterPOD.appendChassis(mockedChassis);
        dataCenterPOD.setD(0, 0, 1.0);
        dataCenterPOD.setRedTemperature(10);
        
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
        
        dataCenter.calculatePower();
        
        assertEquals(1.9842, dataCenter.getTotalPowerConsumption(), 1.0E-4);
        assertFalse(dataCenter.getAM().isSlowDownFromCooler());
        
        verify(mockedChassis, times(2)).power();
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedSystems).getComputeSystems();
        
        verifyNoMoreInteractions(mockedChassis, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
    }

}
