package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;

public class BladeServersCollectionOperationsTest {
    
    public List<BladeServer> bladeServers = new ArrayList<BladeServer>();
    BladeServer mockedBladeServerOne = mock(BladeServer.class);
    BladeServer mockedBladeServerTwo = mock(BladeServer.class);
    
    @Before
    public void setUp() {
        bladeServers.add(mockedBladeServerOne);
        bladeServers.add(mockedBladeServerTwo);
    }
    
    @Test
    public void testTotalFinishedJob() {
        when(mockedBladeServerOne.getTotalFinishedJob()).thenReturn(37);
        when(mockedBladeServerTwo.getTotalFinishedJob()).thenReturn(63);
        
        assertEquals(100, BladeServerCollectionOperations.totalFinishedJob(bladeServers));
        
        verify(mockedBladeServerOne).getTotalFinishedJob();
        verify(mockedBladeServerTwo).getTotalFinishedJob();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testAllIdleTrue() {
        when(mockedBladeServerOne.isIdle()).thenReturn(true);
        when(mockedBladeServerTwo.isIdle()).thenReturn(true);
        
        assertTrue(BladeServerCollectionOperations.allIdle(bladeServers));
        
        verify(mockedBladeServerOne).isIdle();
        verify(mockedBladeServerTwo).isIdle();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testAllIdleFalse() {
        when(mockedBladeServerOne.isIdle()).thenReturn(true);
        when(mockedBladeServerTwo.isIdle()).thenReturn(false);
        
        assertFalse(BladeServerCollectionOperations.allIdle(bladeServers));
        
        verify(mockedBladeServerOne).isIdle();
        verify(mockedBladeServerTwo).isIdle();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testcountIdle() {
        when(mockedBladeServerOne.isIdle()).thenReturn(true);
        when(mockedBladeServerTwo.isIdle()).thenReturn(false);
        
        assertEquals(1, BladeServerCollectionOperations.countIdle(bladeServers));
        
        verify(mockedBladeServerOne).isIdle();
        verify(mockedBladeServerTwo).isIdle();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testcountRunning() {
        when(mockedBladeServerOne.isRunningNormal()).thenReturn(true);
        when(mockedBladeServerTwo.isRunningBusy()).thenReturn(true);
        
        assertEquals(2, BladeServerCollectionOperations.countRunning(bladeServers));
        
        verify(mockedBladeServerOne).isRunningBusy();
        verify(mockedBladeServerOne).isRunningNormal();
        verify(mockedBladeServerTwo).isRunningBusy();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }

}
