package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServerPOD;
import simulator.physical.Chassis;
import simulator.physical.ChassisPOD;

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
    }

    @Test
    public void testChassisCreation() {
        Environment mockedEnvironment = mock(Environment.class);
        final int chassisID = 1;
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.setID(1);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertEquals(chassisID, chassis.getChassisID());
        assertEquals(0, chassis.getRackID());
        assertTrue(chassis.getServers().isEmpty());

        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testPowerIdle() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertFalse(chassis.getServers().isEmpty());
        assertEquals(5.0, chassis.power(), 1.0E-8);

        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testPowerBusy() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertFalse(chassis.getServers().isEmpty());
        chassis.getServers().get(0).setStatusAsRunningBusy();
        assertEquals(100.0, chassis.power(), 1.0E-8);

        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testIsNotReady() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertFalse(chassis.getServers().isEmpty());
        assertFalse(chassis.isReady());
    }

    @Test
    public void testIsNotReadyWhenThereAreNoServers() {
        ChassisPOD chassisPOD = new ChassisPOD();
        Environment mockedEnvironment = mock(Environment.class);
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertTrue(chassis.getServers().isEmpty());
        assertFalse(chassis.isReady());

        verifyNoMoreInteractions(mockedEnvironment);
    }

    @Test
    public void testIsNotReadyWhenBusy() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertFalse(chassis.getServers().isEmpty());
        chassis.getServers().get(0).setStatusAsRunningBusy();
        
        assertFalse(chassis.isReady());
        
        verifyNoMoreInteractions(mockedEnvironment);
        
    }
    
    @Test
    public void testIsReadyWhenRunningNormal() {
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        Environment mockedEnvironment = mock(Environment.class);
        
        Chassis chassis = new Chassis(chassisPOD, mockedEnvironment);
        assertFalse(chassis.getServers().isEmpty());
        chassis.getServers().get(0).setStatusAsRunningNormal();
        
        assertTrue(chassis.isReady());
        
        verifyNoMoreInteractions(mockedEnvironment);
        
    }

}
