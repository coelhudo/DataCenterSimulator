package simulator.tests;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simulator.physical.DataCenterEntityID;

public class DataCenterEntityIDTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testInvalidCreation() {
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(-1, 0, 0);    
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(-1, -1, 0);      
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(-1, -1, -1);      
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(0, 0, 0);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(0, 1, 0);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(0, 1, 1);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(1, 0, 1);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.create(0, 0, 1);
    }

    @Test
    public void testRackIDCreation() {
        DataCenterEntityID rackID = DataCenterEntityID.create(1, 0, 0);
        assertEquals("1.0.0", rackID.toString());
    }
    
    @Test
    public void testRackIDComparison() {
        DataCenterEntityID rackIDX = DataCenterEntityID.create(1, 0, 0);
        assertEquals("1.0.0", rackIDX.toString());
        DataCenterEntityID rackIDY = DataCenterEntityID.create(2, 0, 0);
        assertEquals("2.0.0", rackIDY.toString());
        DataCenterEntityID rackIDZ = DataCenterEntityID.create(3, 0, 0);
        assertEquals("3.0.0", rackIDZ.toString());
        assertTrue(rackIDX.compareTo(rackIDY) < 0);
        assertTrue(rackIDY.compareTo(rackIDZ) < 0);
        assertTrue(rackIDX.compareTo(rackIDZ) < 0);        
    }
    
    @Test
    public void testChassisIDCreation() {
        DataCenterEntityID chassisID = DataCenterEntityID.create(1, 2, 0);
        assertEquals("1.2.0", chassisID.toString());
    }
    
    @Test
    public void testChassisIDComparison() {
        DataCenterEntityID chassisIDX = DataCenterEntityID.create(1, 1, 0);
        assertEquals("1.1.0", chassisIDX.toString());
        DataCenterEntityID chassisIDY = DataCenterEntityID.create(1, 2, 0);
        assertEquals("1.2.0", chassisIDY.toString());
        DataCenterEntityID chassisIDZ = DataCenterEntityID.create(1, 3, 0);
        assertEquals("1.3.0", chassisIDZ.toString());
        assertTrue(chassisIDX.compareTo(chassisIDY) < 0);
        assertTrue(chassisIDY.compareTo(chassisIDZ) < 0);
        assertTrue(chassisIDX.compareTo(chassisIDZ) < 0);        
    }
    
    @Test
    public void testServerIDCreation() {
        DataCenterEntityID rackID = DataCenterEntityID.create(1, 2, 3);
        assertEquals("1.2.3", rackID.toString());
    }
    
    @Test
    public void testServerIDComparison() {
        DataCenterEntityID serverIDX = DataCenterEntityID.create(1, 1, 1);
        assertEquals("1.1.1", serverIDX.toString());
        DataCenterEntityID serverIDY = DataCenterEntityID.create(1, 1, 2);
        assertEquals("1.1.2", serverIDY.toString());
        DataCenterEntityID serverIDZ = DataCenterEntityID.create(1, 1, 3);
        assertEquals("1.1.3", serverIDZ.toString());
        assertTrue(serverIDX.compareTo(serverIDY) < 0);
        assertTrue(serverIDY.compareTo(serverIDZ) < 0);
        assertTrue(serverIDX.compareTo(serverIDZ) < 0);        
    }
}
