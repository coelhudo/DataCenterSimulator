package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertTrue(chassis.getServers().isEmpty());
    }
    
    @Test
    public void testPower() {
        BladeServer mockedBladeServerOne = mock(BladeServer.class);
        when(mockedBladeServerOne.getPower()).thenReturn(15.0);
        BladeServer mockedBladeServerTwo = mock(BladeServer.class);
        when(mockedBladeServerTwo.getPower()).thenReturn(17.0);
        
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServer(mockedBladeServerOne);
        chassisPOD.appendServer(mockedBladeServerTwo);
        
        Chassis chassis = new Chassis(chassisPOD, -1);
        assertEquals(32.0, chassis.power(), 1.0E-8);
    }
    
    @Test
    public void testIsNotReady() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(0);
        
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServer(mockedBladeServer);
        
        Chassis chassis = new Chassis(chassisPOD, -1);
        assertFalse(chassis.isReady());
    }
    
    @Test
    public void testIsReady() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
        
        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServer(mockedBladeServer);
        
        Chassis chassis = new Chassis(chassisPOD, -1);
        assertTrue(chassis.isReady());
    }

}
