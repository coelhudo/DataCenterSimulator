package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.am.DataCenterAM;
import simulator.physical.BladeServerPOD;
import simulator.physical.Chassis;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.physical.RackPOD;
import simulator.utils.ActivitiesLogger;

public class DataCenterTest {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };
    public BladeServerPOD bladeServerPOD;
    public RackPOD rackPOD;
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
        rackPOD = new RackPOD();
        rackPOD.appendChassis(chassisPOD);
    }

    @Test
    public void testDataCenterCreation() {
        Environment mockedEnvironment = mock(Environment.class);
        DataCenterAM mockeddataCenterAM = mock(DataCenterAM.class);
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder("configs/DC.xml");
        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockeddataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);
        List<Chassis> chassis = dataCenter.getChassisSet();
        assertFalse(chassis.isEmpty());
        assertEquals(50, chassis.size());
        assertEquals(0, dataCenter.getOverRed());
        assertNotNull(dataCenter.getServer(0));
        assertNotNull(dataCenter.getServer(0, 0));
        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E-8);

        verifyNoMoreInteractions(mockedActivitiesLogger, mockeddataCenterAM, mockedEnvironment);
    }

    @Test
    public void testCalculatePowerSlowingDownFromCooler() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 200.0);
        dataCenterPOD.setRedTemperature(0);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenterAM mockeddataCenterAM = mock(DataCenterAM.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockeddataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);
        dataCenter.getChassisSet().get(0).getServers().get(0).setStatusAsRunningBusy();
        dataCenter.calculatePower();

        assertEquals(100.00003676, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        
        verify(mockeddataCenterAM).setSlowDownFromCooler(true);
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();

        verifyNoMoreInteractions(mockedActivitiesLogger, mockeddataCenterAM, mockedEnvironment);
    }

    @Test
    public void testCalculatePowerNotSlowingDownFromCooler() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 1.0);
        dataCenterPOD.setRedTemperature(10);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenterAM mockeddataCenterAM = mock(DataCenterAM.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockeddataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);
        dataCenter.getChassisSet().get(0).getServers().get(0).setStatusAsIdle();

        dataCenter.calculatePower();

        assertEquals(12.91139, dataCenter.getTotalPowerConsumption(), 1.0E-4);

        verify(mockeddataCenterAM).setSlowDownFromCooler(false);
        verify(mockedActivitiesLogger, times(2)).write(anyString());
        verify(mockedEnvironment).getCurrentLocalTime();

        verifyNoMoreInteractions(mockedActivitiesLogger, mockeddataCenterAM, mockedEnvironment);
    }

}
