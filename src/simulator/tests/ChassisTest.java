package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServerPOD;
import simulator.physical.Chassis;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenterEntityID;

public class ChassisTest {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };
    public BladeServerPOD bladeServerPOD;

    @Before
    public void setUp() {
        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setIdleConsumption(5.0);
        bladeServerPOD.setID(DataCenterEntityID.createServerID(1, 1, 1));
    }

    @Test
    public void testChassisCreation() {
        Environment mockedEnvironment = mock(Environment.class);
        final String chassisID = "1_1_0";
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.setID(DataCenterEntityID.createChassisID(1, 1));
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertEquals(chassisID, chassis.getID().toString());
        
        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testPowerIdle() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertEquals(5.0, chassis.power(), 1.0E-8);

        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testPowerBusy() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        chassis.getServer(bladeServerPOD.getID()).setStatusAsRunningBusy();
        assertEquals(100.0, chassis.power(), 1.0E-8);

        verifyNoMoreInteractions(mockedEnvironment);
    }
}
