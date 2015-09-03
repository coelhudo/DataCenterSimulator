package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.ra.MHR;
import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;

public class MHRTest {

    public Environment mockedEnvironment;
    public DataCenter mockedDataCenter;
    public MHR mininumHeatRecirculation;

    @Before
    public void setUp() {
        mockedEnvironment = mock(Environment.class);
        mockedDataCenter = mock(DataCenter.class);
        mininumHeatRecirculation = new MHR(mockedEnvironment, mockedDataCenter);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter);
    }

    @Test
    public void testNextServer_EmptyBladeServers() {
        List<BladeServer> bladeServer = new ArrayList<BladeServer>();
        assertEquals(-2, mininumHeatRecirculation.nextServer(bladeServer));
    }

    @Test
    public void testNextServer_NonEmptyBladeServers_NotRunningNormal() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(false);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        assertEquals(-2, mininumHeatRecirculation.nextServer(bladeServers));

        verify(mockedBladeServer, times(50)).isRunningNormal();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testNextServer_NonEmptyBladeServers_RunningNormal() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        assertEquals(0, mininumHeatRecirculation.nextServer(bladeServers));

        verify(mockedBladeServer, times(25)).isRunningNormal();
        verify(mockedBladeServer, times(25)).getChassisID();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedBladeServer);
    }

    @Test
    public void testNextServerSys_EmptyChassis() {
        List<Integer> chassis = new ArrayList<Integer>();
        int[] result = mininumHeatRecirculation.nextServerSys(chassis);
        assertEquals(-2, result[0]);
        assertEquals(-2, result[1]);
    }

    @Test
    public void testNextServerSys_ChassisNotExpected() {
        List<Integer> chassis = Arrays.asList(51);
        int[] result = mininumHeatRecirculation.nextServerSys(chassis);
        assertEquals(-2, result[0]);
        assertEquals(-2, result[1]);
    }

    @Test
    public void testNextServerSys_ChassisExpected_BladeServerSystemAssigned() {
        List<Integer> chassisIndex = Arrays.asList(0);
        Chassis mockedChassis = mock(Chassis.class);
        List<Chassis> chassis = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassis);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        when(mockedChassis.getServers()).thenReturn(bladeServers);
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(false);

        int[] result = mininumHeatRecirculation.nextServerSys(chassisIndex);
        assertEquals(-2, result[0]);
        assertEquals(-2, result[1]);

        verify(mockedDataCenter, times(3)).getChassisSet();
        verify(mockedChassis, times(3)).getServers();
        verify(mockedBladeServer).isNotSystemAssigned();

        verifyNoMoreInteractions(mockedChassis, mockedBladeServer);
    }

    @Test
    public void testNextServerSys_ChassisExpected_BladeServerNotSystemAssigned() {
        List<Integer> chassisIndex = Arrays.asList(0);
        Chassis mockedChassis = mock(Chassis.class);
        List<Chassis> chassis = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassis);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        when(mockedChassis.getServers()).thenReturn(bladeServers);
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(true);

        int[] result = mininumHeatRecirculation.nextServerSys(chassisIndex);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);

        verify(mockedDataCenter, times(2)).getChassisSet();
        verify(mockedChassis, times(2)).getServers();
        verify(mockedBladeServer).isNotSystemAssigned();

        verifyNoMoreInteractions(mockedChassis, mockedBladeServer);
    }

    @Test
    public void testAllocateSystemLevelServer_WithoutBladeServers() {
        int[] result = { 0 };
        List<BladeServer> bladeServers = Arrays.asList();
        
        mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, result);
        
        assertEquals(-2, result[0]);
    }
    
    @Test
    public void testAllocateSystemLevelServer_BladeServerNotRunningNormal() {
        int[] result = { 0 };
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(false);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        
        mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, result);
        
        assertEquals(-2, result[0]);
        
        verify(mockedBladeServer).isRunningNormal();
        
        verifyNoMoreInteractions(mockedBladeServer);
    }
    
    @Test
    public void testAllocateSystemLevelServer_BladeServerRunningNormal_ChassisNotExpected() {
        int[] result = { 0 };
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getChassisID()).thenReturn(51);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        
        mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, result);
        
        assertEquals(-2, result[0]);
        
        verify(mockedBladeServer, times(51)).isRunningNormal();
        verify(mockedBladeServer, times(50)).getChassisID();
        
        verifyNoMoreInteractions(mockedBladeServer);
    }
    
    @Test
    public void testAllocateSystemLevelServer_BladeServerRunningNormal() {
        int[] result = { -2 };
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        
        mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, result);
        
        assertEquals(0, result[0]);
        
        verify(mockedBladeServer, times(27)).isRunningNormal();
        verify(mockedBladeServer, times(26)).getChassisID();
        
        verifyNoMoreInteractions(mockedBladeServer);
    }

}
