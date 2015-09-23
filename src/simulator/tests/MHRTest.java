package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;
import simulator.ra.MHR;

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
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);
        assertEquals(0, mininumHeatRecirculation.nextServer(bladeServers));

        verify(mockedBladeServer, times(25)).isRunningNormal();
        verify(mockedBladeServer, times(25)).getID();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedBladeServer);
    }

    @Test
    public void testNextServerSys_EmptyChassis() {
        List<Chassis> chassis = new ArrayList<Chassis>();
        assertNull(mininumHeatRecirculation.nextServerSys(chassis));
    }

    @Test
    public void testNextServerSys_ChassisNotExpected() {
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 1));
        List<Chassis> chassis = Arrays.asList(mockedChassis);
        assertNull(mininumHeatRecirculation.nextServerSys(chassis));
    }

    @Test
    public void testNextServerSys_ChassisExpected_BladeServerSystemAssigned() {
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 1));
        when(mockedChassis.getNextNotAssignedBladeServer()).thenReturn(null);
        
        List<Chassis> chassis = Arrays.asList(mockedChassis);
        
        assertNull(mininumHeatRecirculation.nextServerSys(chassis));

        verify(mockedChassis, times(50)).getID();
        verify(mockedChassis).getNextNotAssignedBladeServer();
        
        verifyNoMoreInteractions(mockedChassis);
    }

    @Test
    public void testNextServerSys_ChassisExpected_BladeServerNotSystemAssigned() {
        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 1));
        List<Chassis> chassis = Arrays.asList(mockedChassis);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedChassis.getNextNotAssignedBladeServer()).thenReturn(mockedBladeServer);
        assertEquals(mockedBladeServer, mininumHeatRecirculation.nextServerSys(chassis));
        
        verify(mockedChassis, times(26)).getID();
        verify(mockedChassis).getNextNotAssignedBladeServer();
        
        verifyNoMoreInteractions(mockedChassis, mockedBladeServer);
    }

    @Test
    public void testAllocateSystemLevelServer_WithoutBladeServers() {
        List<BladeServer> bladeServers = Arrays.asList();

        List<BladeServer> result = mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, 1);

        assertNull(result);
    }

    @Test
    public void testAllocateSystemLevelServer_BladeServerNotRunningNormal() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(false);
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);

        List<BladeServer> result = mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, 1);

        assertNull(result);

        verify(mockedBladeServer).isRunningNormal();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testAllocateSystemLevelServer_BladeServerRunningNormal_ChassisNotExpected() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 51));
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);

        List<BladeServer> result = mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, 1);

        assertTrue(result.isEmpty());

        verify(mockedBladeServer, times(51)).isRunningNormal();
        verify(mockedBladeServer, times(50)).getID();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testAllocateSystemLevelServer_BladeServerRunningNormal() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 10));
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServer);

        List<BladeServer> result = mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, 1);

        assertEquals(1, result.size());
        
        verify(mockedBladeServer, times(27)).isRunningNormal();
        verify(mockedBladeServer, times(26)).getID();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testAllocateSystemLevelServer_BladeServerRunningNormal_MultipleResults() {
        BladeServer mockedBladeServerOne = mock(BladeServer.class);
        when(mockedBladeServerOne.isRunningNormal()).thenReturn(true);
        when(mockedBladeServerOne.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 10));
        BladeServer mockedBladeServerTwo = mock(BladeServer.class);
        when(mockedBladeServerTwo.isRunningNormal()).thenReturn(false);
        when(mockedBladeServerTwo.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 20));
        BladeServer mockedBladeServerThree = mock(BladeServer.class);
        when(mockedBladeServerThree.isRunningNormal()).thenReturn(true);
        when(mockedBladeServerThree.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 30));
        List<BladeServer> bladeServers = Arrays.asList(mockedBladeServerOne, mockedBladeServerTwo,
                mockedBladeServerThree);

        List<BladeServer> result = mininumHeatRecirculation.allocateSystemLevelServer(bladeServers, 2);

        assertEquals(2, result.size());
        assertEquals("1.1.10", result.get(0).getID().toString());
        assertEquals("1.1.30", result.get(1).getID().toString());

        verify(mockedBladeServerOne, times(27)).isRunningNormal();
        verify(mockedBladeServerOne, times(27)).getID();
        verify(mockedBladeServerTwo, times(27)).isRunningNormal();
        verify(mockedBladeServerThree, times(27)).isRunningNormal();
        verify(mockedBladeServerThree, times(27)).getID();

        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo, mockedBladeServerThree);
    }
}
