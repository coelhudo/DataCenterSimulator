package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;

public class BladeServersCollectionOperationTest {

    @Test
    public void testTotalFinishedJob() {
        List<BladeServer> bladeServers = new ArrayList<BladeServer>();
        BladeServer mockedBladeServerOne = mock(BladeServer.class);
        BladeServer mockedBladeServerTwo = mock(BladeServer.class);
        bladeServers.add(mockedBladeServerOne);
        bladeServers.add(mockedBladeServerTwo);
        
        when(mockedBladeServerOne.getTotalFinishedJob()).thenReturn(37);
        when(mockedBladeServerTwo.getTotalFinishedJob()).thenReturn(63);
        
        assertEquals(100, BladeServerCollectionOperations.totalFinishedJob(bladeServers));
        
        verify(mockedBladeServerOne).getTotalFinishedJob();
        verify(mockedBladeServerTwo).getTotalFinishedJob();
        
        verifyNoMoreInteractions(mockedBladeServerOne, mockedBladeServerTwo);
    }

}
