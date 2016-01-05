package simulator.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.am.ComputeSystemAM;
import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;
import simulator.system.ComputeSystem;

public class ComputeSystemAMTest {

    public Environment mockedEnvironment;
    public ComputeSystem mockedComputeSystem;
    
    @Before
    public void setUp() {
        mockedEnvironment = mock(Environment.class);
        mockedComputeSystem = mock(ComputeSystem.class);
    }
    
    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedEnvironment, mockedComputeSystem);
    }
    
    @Test
    public void testAnalysis_LocalTimeByEpochTrue() {
        ComputeSystemAM computeSystemAM = new ComputeSystemAM(mockedEnvironment);
        computeSystemAM.setManagedResource(mockedComputeSystem);
        
        when(mockedEnvironment.localTimeByEpoch()).thenReturn(true);
        
        computeSystemAM.analysis();
        
        verify(mockedEnvironment).localTimeByEpoch();
    }
    
    @Test
    public void testAnalysis_LocalTimeByEpochFalse_SLAViolationGreaterThanZero_NoIdleServers() {
        ComputeSystemAM computeSystemAM = new ComputeSystemAM(mockedEnvironment);
        computeSystemAM.setManagedResource(mockedComputeSystem);
        
        when(mockedEnvironment.localTimeByEpoch()).thenReturn(false);
        when(mockedComputeSystem.getNumberOFSLAViolation()).thenReturn(100);
        
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningBusy()).thenReturn(true);
        when(mockedComputeSystem.getComputeNodeList()).thenReturn(Arrays.asList(mockedBladeServer));
        
        computeSystemAM.analysis();
        
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        
        verify(mockedComputeSystem, times(2)).getComputeNodeList();
        verify(mockedComputeSystem).numberOfIdleNode();
        verify(mockedComputeSystem).getNumberOFSLAViolation();
        
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer).increaseFrequency();
        
        verifyNoMoreInteractions(mockedBladeServer);
    }
    
    @Test
    public void testAnalysis_LocalTimeByEpochFalse_SLAViolationGreaterThanZero_NoRunningServers() {
        ComputeSystemAM computeSystemAM = new ComputeSystemAM(mockedEnvironment);
        computeSystemAM.setManagedResource(mockedComputeSystem);
        
        when(mockedEnvironment.localTimeByEpoch()).thenReturn(false);
        when(mockedComputeSystem.getNumberOFSLAViolation()).thenReturn(100);
        
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(true);
        when(mockedComputeSystem.getComputeNodeList()).thenReturn(Arrays.asList(mockedBladeServer));
        
        computeSystemAM.analysis();
        
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();
        
        
        verify(mockedComputeSystem, times(2)).getComputeNodeList();
        verify(mockedComputeSystem).numberOfIdleNode();
        verify(mockedComputeSystem).getNumberOFSLAViolation();
        verify(mockedComputeSystem).numberOfRunningNode();
        
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer).setMips(1.4);
        
        verifyNoMoreInteractions(mockedBladeServer);
    }
    
    @Test
    public void testAnalysis_LocalTimeByEpochFalse_SLAViolationGreaterEqualsZero_NoIdleServers() {
        ComputeSystemAM computeSystemAM = new ComputeSystemAM(mockedEnvironment);
        computeSystemAM.setManagedResource(mockedComputeSystem);
        
        when(mockedEnvironment.localTimeByEpoch()).thenReturn(false);
        when(mockedComputeSystem.getNumberOFSLAViolation()).thenReturn(0);
        
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunning()).thenReturn(true);
        when(mockedBladeServer.activeBatchJobs()).thenReturn(new ArrayList<BatchJob>());
        when(mockedBladeServer.getBlockedBatchList()).thenReturn(new ArrayList<BatchJob>());
        when(mockedComputeSystem.getComputeNodeList()).thenReturn(Arrays.asList(mockedBladeServer));
        
        computeSystemAM.analysis();
        
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();
        
        verify(mockedComputeSystem, times(2)).getComputeNodeList();
        verify(mockedComputeSystem).getNumberOFSLAViolation();
        
        verify(mockedBladeServer, times(2)).isRunning();
        verify(mockedBladeServer).setStatusAsIdle();
        verify(mockedBladeServer).decreaseFrequency();
        verify(mockedBladeServer).activeBatchJobs();
        verify(mockedBladeServer).getBlockedBatchList();
        
        verifyNoMoreInteractions(mockedBladeServer);
    }

}
