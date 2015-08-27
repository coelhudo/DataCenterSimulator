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
        when(mockedBladeServerOne.getReady()).thenReturn(-1);
        when(mockedBladeServerTwo.getReady()).thenReturn(-1);
        
        assertTrue(BladeServerCollectionOperations.allIdle(bladeServers));
        
        verify(mockedBladeServerOne).getReady();
        verify(mockedBladeServerTwo).getReady();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testAllIdleFalse() {
        when(mockedBladeServerOne.getReady()).thenReturn(-1);
        when(mockedBladeServerTwo.getReady()).thenReturn(0);
        
        assertFalse(BladeServerCollectionOperations.allIdle(bladeServers));
        
        verify(mockedBladeServerOne).getReady();
        verify(mockedBladeServerTwo).getReady();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testcountIdle() {
        when(mockedBladeServerOne.getReady()).thenReturn(-1);
        when(mockedBladeServerTwo.getReady()).thenReturn(0);
        
        assertEquals(1, BladeServerCollectionOperations.countIdle(bladeServers));
        
        verify(mockedBladeServerOne).getReady();
        verify(mockedBladeServerTwo).getReady();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }
    
    @Test
    public void testcountRunning() {
        when(mockedBladeServerOne.getReady()).thenReturn(0);
        when(mockedBladeServerTwo.getReady()).thenReturn(0);
        
        assertEquals(2, BladeServerCollectionOperations.countRunning(bladeServers));
        
        verify(mockedBladeServerOne).getReady();
        verify(mockedBladeServerTwo).getReady();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }

}
