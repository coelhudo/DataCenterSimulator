package simulator.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.ra.ResourceAllocation;
import simulator.system.ComputeSystem;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseSystem;
import simulator.system.InteractiveSystem;

public class ResourceAllocationTest {

    public class TestableResourceAllocation extends ResourceAllocation {

        private int [] nextSysValueResult = new int[2];
        
        public TestableResourceAllocation(Environment environment, DataCenter dataCenter) {
            super(environment, dataCenter);
            nextSysValueResult[0] = 0;
            nextSysValueResult[1] = 0;
        }

        @Override
        public int[] nextServerSys(List<Integer> chassisList) {
            return nextSysValueResult;
        }

        @Override
        public int nextServer(List<BladeServer> bladeList) {
            return 0;
        }

        @Override
        public int[] allocateSystemLevelServer(List<BladeServer> bs, int[] list) {
            list[0] = bs.get(0).getChassisID();
            list[1] = bs.get(0).getServerID();
            return list;
        }

        @Override
        public void resourceAloc(EnterpriseSystem enterpriseSystem) {
            
        }

        @Override
        public void resourceAloc(InteractiveSystem interactiveSystem) {
            
        }
        
        public void setNextServerSysResult(int [] result) {
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
        int [] result = {-2,-2};
        resourceAllocation.setNextServerSysResult(result);
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

        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));

        when(mockedDataCenter.getChassisSet()).thenReturn(Arrays.asList(mockedChassis));
        when(mockedDataCenter.getServer(0, 0)).thenReturn(mockedBladeServer);

        when(mockedComputeSystem.getNumberOfNode()).thenReturn(1);
        when(mockedComputeSystem.getRackIDs()).thenReturn(Arrays.asList(0));

        resourceAllocation.initialResourceAloc(mockedComputeSystem);

        verify(mockedComputeSystem).getRackIDs();
        verify(mockedComputeSystem, times(2)).getNumberOfNode();
        verify(mockedComputeSystem).addComputeNodeToSys(mockedBladeServer);
        verify(mockedComputeSystem).appendBladeServerIndexIntoComputeNodeIndex(0);

        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();
        
        verify(mockedBladeServer).setStatusAsRunningNormal();

        verify(mockedDataCenter).getChassisSet();
        verify(mockedDataCenter).getServer(0, 0);

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
        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));
        List<Chassis> chassisSet = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassisSet);
        when(mockedDataCenter.getServer(0, 0)).thenReturn(mockedBladeServer);

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
        verify(mockedEnterpriseSystem).appendBladeServerIndexIntoComputeNodeIndex(0);

        verify(mockedDataCenter, times(1)).getChassisSet();
        verify(mockedDataCenter).getServer(0, 0);

        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();

        verify(mockedBladeServer).setStatusAsNotAssignedToAnyApplication();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedBladeServer, mockedChassis);
    }

    @Test
    public void testInicialResourceAllocation_EnterpriseSystem_WithRackIDs_WithNode_WithApplication() {
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        when(mockedEnterpriseSystem.getNumberOfNode()).thenReturn(1);

        when(mockedEnterpriseSystem.getRackIDs()).thenReturn(Arrays.asList(0));

        Chassis mockedChassis = mock(Chassis.class);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isNotSystemAssigned()).thenReturn(true);
        when(mockedChassis.getServers()).thenReturn(Arrays.asList(mockedBladeServer));
        List<Chassis> chassisSet = Arrays.asList(mockedChassis);
        when(mockedDataCenter.getChassisSet()).thenReturn(chassisSet);
        when(mockedDataCenter.getServer(0, 0)).thenReturn(mockedBladeServer);

        EnterpriseApp mockedApplication = mock(EnterpriseApp.class);
        when(mockedApplication.getMinProc()).thenReturn(1);
        when(mockedEnterpriseSystem.getApplications()).thenReturn(Arrays.asList(mockedApplication));

        when(mockedEnterpriseSystem.getComputeNodeList()).thenReturn(Arrays.asList(mockedBladeServer));
        resourceAllocation.initialResourceAlocator(mockedEnterpriseSystem);

        verify(mockedEnterpriseSystem).getRackIDs();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfNode();
        verify(mockedEnterpriseSystem).getApplications();
        verify(mockedEnterpriseSystem).setNumberOfIdleNode(0);
        verify(mockedEnterpriseSystem, times(3)).getComputeNodeList();
        verify(mockedEnterpriseSystem, times(2)).getNumberOfIdleNode();
        verify(mockedEnterpriseSystem).addComputeNodeToSys(mockedBladeServer);
        verify(mockedEnterpriseSystem).appendBladeServerIndexIntoComputeNodeIndex(0);

        verify(mockedDataCenter, times(3)).getChassisSet();
        verify(mockedDataCenter, times(2)).getServer(0, 0);

        verify(mockedChassis).getRackID();
        verify(mockedChassis).getChassisID();
        verify(mockedChassis, times(2)).getServers();

        verify(mockedBladeServer).getChassisID();
        verify(mockedBladeServer, times(2)).getServerID();
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
