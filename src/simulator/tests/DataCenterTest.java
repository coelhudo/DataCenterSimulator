package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServerPOD;
import simulator.physical.Chassis;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.system.Systems;
import simulator.utils.ActivitiesLogger;

public class DataCenterTest {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };
    public BladeServerPOD bladeServerPOD;
    public ChassisPOD chassisPOD;

    @Before
    public void setUp() {
        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setIdleConsumption(5.0);
        chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);   
    }

    
    @Test
    public void testDataCenterCreation() {
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder("configs/DC.xml", mockedEnvironment);
        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
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
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.setD(0, 0, 200.0);
        dataCenterPOD.setRedTemperature(0);
        
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
        dataCenter.getChassisSet().get(0).getServers().get(0).setStatusAsRunningBusy();
        dataCenter.calculatePower();
        
        assertEquals(100.00003676, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertTrue(dataCenter.getAM().isSlowDownFromCooler());
        
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedSystems).getComputeSystems();
        
        verifyNoMoreInteractions(mockedActivitiesLogger, mockedEnvironment, mockedSystems);        
    }
    
    @Test 
    public void testCalculatePowerNotSlowingDownFromCooler() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.setD(0, 0, 1.0);
        dataCenterPOD.setRedTemperature(10);
        
        Environment mockedEnvironment = mock(Environment.class);
        Systems mockedSystems = mock(Systems.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, mockedSystems);
        dataCenter.getChassisSet().get(0).getServers().get(0).setStatusAsIdle();
        
        dataCenter.calculatePower();
        
        assertEquals(12.91139, dataCenter.getTotalPowerConsumption(), 1.0E-4);
        assertFalse(dataCenter.getAM().isSlowDownFromCooler());
        
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedSystems).getComputeSystems();
        
        verifyNoMoreInteractions(mockedActivitiesLogger, mockedEnvironment, mockedSystems);
    }

}
