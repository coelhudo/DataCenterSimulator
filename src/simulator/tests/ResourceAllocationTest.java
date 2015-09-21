package simulator.tests;

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
import simulator.ra.ResourceAllocation;
import simulator.system.ComputeSystem;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseSystem;

public class ResourceAllocationTest {

    public class TestableResourceAllocation extends ResourceAllocation {

        private BladeServer nextSysValueResult = mock(BladeServer.class);

        public TestableResourceAllocation(Environment environment, DataCenter dataCenter) {
            super(environment, dataCenter);
        }

        @Override
        public BladeServer nextServerSys(List<Integer> chassisList) {
            return nextSysValueResult;
        }

        @Override
        public int nextServer(List<BladeServer> bladeList) {
            return 0;
        }

        @Override
        public List<BladeServer> allocateSystemLevelServer(List<BladeServer> bs, int numberOfRequestedServers) {
            List<BladeServer> requestedServers = new ArrayList<BladeServer>();
            requestedServers.add(bs.get(0));
            return requestedServers;
        }

        public void setNextServerSysResult(BladeServer result) {
            nextSysValueResult = result;
        }

    }

    public Environment mockedEnvironment;
    public DataCenter mockedDataCenter;
    public TestableResourceAllocation resourceAllocation;

    @Before
    public void setUp() {
        mockedEnvironment = mock(Environment.class);
        mockedDataCenter = mock(DataCenter.class);
        resourceAllocation = new TestableResourceAllocation(mockedEnvironment, mockedDataCenter);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter);
    }

    @Test
    public void testInicialResourceAllocation_ComputeSystem_EmptyRackIDs_WithoutNode() {
        ComputeSystem mockedComputeSystem = mock(ComputeSystem.class);
        List<Integer> rackIDs = Arrays.asList();
        when(mockedComputeSystem.getNumberOfNode()).thenReturn(0);
        when(mockedComputeSystem.getRackIDs()).thenReturn(rackIDs);
        resourceAllocation.initialResourceAloc(mockedComputeSystem);

        verify(mockedComputeSystem).getRackIDs();
        verify(mockedComputeSystem).getNumberOfNode();

        verifyNoMoreInteractions(mockedComputeSystem);
    }

    @Test
    public void testInicialResourceAllocation_ComputeSystem_EmptyRackIDs_WithNode() {
        resourceAllocation.setNextServerSysResult(null);
        ComputeSystem mockedComputeSystem = mock(ComputeSystem.class);
        List<Integer> rackIDs = Arrays.asList();
        when(mockedComputeSystem.getNumberOfNode()).thenReturn(1);
        when(mockedComputeSystem.getRackIDs()).thenReturn(rackIDs);
        resourceAllocation.initialResourceAloc(mockedComputeSystem);

        verify(mockedComputeSystem).getRackIDs();
        verify(mockedComputeSystem).getNumberOfNode();

        verifyNoMoreInteractions(mockedComputeSystem);
    }

    @Test
    public void testInicialResourceAllocation_ComputeSystem_WithRackIDs_WithNode() {
        ComputeSystem mockedComputeSystem = mock(ComputeSystem.class);

        Chassis mockedChassis = mock(Chassis.class);
        when(mockedChassis.getRackID()).thenReturn(0);
        when(mockedChassis.getChassisID()).thenReturn(0);

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));

        when(mockedDataCenter.getChassisSet()).thenReturn(Arrays.asList(mockedChassis));
        
        when(mockedComputeSystem.getNumberOfNode()).thenReturn(1);
        when(mockedComputeSystem.getRackIDs()).thenReturn(Arrays.asList(0));

        resourceAllocation.setNextServerSysResult(mockedBladeServer);
        resourceAllocation.initialResourceAloc(mockedComputeSystem);

        verify(mockedComputeSystem).getRackIDs();
        verify(mockedComputeSystem, times(2)).getNumberOfNode();
        verify(mockedComputeSystem).addComputeNodeToSys(mockedBladeServer);
        
        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();

        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer).getID();

        verify(mockedDataCenter).getChassisSet();
        
        verifyNoMoreInteractions(mockedComputeSystem, mockedChassis, mockedBladeServer);
    }

    @Test
    public void testInicialResourceAllocation_EnterpriseSystem_EmptyRackIDs_WithoutNode_WithoutApplication() {
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        when(mockedEnterpriseSystem.getNumberOfNode()).thenReturn(0);
        List<Integer> rackIDs = Arrays.asList();
        when(mockedEnterpriseSystem.getRackIDs()).thenReturn(rackIDs);
        List<EnterpriseApp> applications = Arrays.asList();
        when(mockedEnterpriseSystem.getApplications()).thenReturn(applications);
        List<BladeServer> bladeServers = Arrays.asList();
        when(mockedEnterpriseSystem.getComputeNodeList()).thenReturn(bladeServers);
        resourceAllocation.initialResourceAlocator(mockedEnterpriseSystem);

        verify(mockedEnterpriseSystem).getRackIDs();
        verify(mockedEnterpriseSystem).getNumberOfNode();
        verify(mockedEnterpriseSystem).getApplications();
        verify(mockedEnterpriseSystem).setNumberOfIdleNode(0);
        verify(mockedEnterpriseSystem).getComputeNodeList();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfIdleNode();

        verifyNoMoreInteractions(mockedEnterpriseSystem);
    }

    @Test
    public void testInicialResourceAllocation_EnterpriseSystem_WithRackIDs_WithNode_WithoutApplication() {
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        when(mockedEnterpriseSystem.getNumberOfNode()).thenReturn(1);

        when(mockedEnterpriseSystem.getRackIDs()).thenReturn(Arrays.asList(0));

        Chassis mockedChassis = mock(Chassis.class);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));
        resourceAllocation.setNextServerSysResult(mockedBladeServer);
        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));
        List<Chassis> chassisSet = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassisSet);

        List<EnterpriseApp> applications = Arrays.asList();
        when(mockedEnterpriseSystem.getApplications()).thenReturn(applications);

        List<BladeServer> bladeServers = Arrays.asList();
        when(mockedEnterpriseSystem.getComputeNodeList()).thenReturn(bladeServers);
        resourceAllocation.initialResourceAlocator(mockedEnterpriseSystem);

        verify(mockedEnterpriseSystem).getRackIDs();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfNode();
        verify(mockedEnterpriseSystem).getApplications();
        verify(mockedEnterpriseSystem).setNumberOfIdleNode(0);
        verify(mockedEnterpriseSystem).getComputeNodeList();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfIdleNode();
        verify(mockedEnterpriseSystem).addComputeNodeToSys(mockedBladeServer);
        
        verify(mockedDataCenter, times(1)).getChassisSet();
        
        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();

        verify(mockedBladeServer).setStatusAsNotAssignedToAnyApplication();
        verify(mockedBladeServer).getID();
        
        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedBladeServer, mockedChassis);
    }

    @Test
    public void testInicialResourceAllocation_EnterpriseSystem_WithRackIDs_WithNode_WithApplication() {

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        when(mockedEnterpriseSystem.getNumberOfNode()).thenReturn(1);

        when(mockedEnterpriseSystem.getRackIDs()).thenReturn(Arrays.asList(0));

        Chassis mockedChassis = mock(Chassis.class);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(true);
        when(mockedChassis.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 1));
        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));
        List<Chassis> chassisSet = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassisSet);
        when(mockedDataCenter.getServer(0, 0)).thenReturn(mockedBladeServer);
        
        EnterpriseApp mockedApplication = mock(EnterpriseApp.class);
        when(mockedApplication.getMinProc()).thenReturn(1);
        when(mockedEnterpriseSystem.getApplications()).thenReturn(Arrays.asList(mockedApplication));

        when(mockedEnterpriseSystem.getComputeNodeList()).thenReturn(Arrays.asList(mockedBladeServer));
        resourceAllocation.setNextServerSysResult(mockedBladeServer);
        resourceAllocation.initialResourceAlocator(mockedEnterpriseSystem);

        verify(mockedEnterpriseSystem).getRackIDs();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfNode();
        verify(mockedEnterpriseSystem).getApplications();
        verify(mockedEnterpriseSystem).setNumberOfIdleNode(0);
        verify(mockedEnterpriseSystem, times(3)).getComputeNodeList();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfIdleNode();
        verify(mockedEnterpriseSystem).addComputeNodeToSys(mockedBladeServer);
        
        verify(mockedDataCenter, times(3)).getChassisSet();
        verify(mockedDataCenter).getServer(0, 0);
        
        
        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();
        verify(mockedChassis, times(2)).getServers();

        verify(mockedBladeServer, times(4)).getID();
        verify(mockedBladeServer).setStatusAsNotAssignedToAnyApplication();
        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer).setSLAPercentage(0);
        verify(mockedBladeServer).setTimeTreshold(0);

        verify(mockedApplication).getMinProc();
        verify(mockedApplication).addCompNodetoBundle(mockedBladeServer);
        verify(mockedApplication).getSLAPercentage();
        verify(mockedApplication).getTimeTreshold();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedBladeServer, mockedChassis, mockedApplication);
    }

}
