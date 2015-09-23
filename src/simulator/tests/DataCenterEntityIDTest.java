package simulator.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        DataCenterEntityID.createServerID(-1, 0, 0);    
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(-1, -1, 0);      
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(-1, -1, -1);      
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(0, 0, 0);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(0, 1, 0);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(0, 1, 1);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(1, 0, 1);
        
        expected.expect(RuntimeException.class);
        DataCenterEntityID.createServerID(0, 0, 1);
    }

    @Test
    public void testRackIDCreation() {
        DataCenterEntityID rackID = DataCenterEntityID.createRackID(1);
        assertEquals("1.0.0", rackID.toString());
    }
    
    @Test
    public void testRackIDComparison() {
        DataCenterEntityID rackIDX = DataCenterEntityID.createRackID(1);
        assertEquals("1.0.0", rackIDX.toString());
        DataCenterEntityID rackIDY = DataCenterEntityID.createRackID(2);
        assertEquals("2.0.0", rackIDY.toString());
        DataCenterEntityID rackIDZ = DataCenterEntityID.createRackID(3);
        assertEquals("3.0.0", rackIDZ.toString());
        assertFalse(rackIDX.equals(rackIDZ));        
    }
    
    @Test
    public void testChassisIDCreation() {
        DataCenterEntityID chassisID = DataCenterEntityID.createChassisID(1, 2);
        assertEquals("1.2.0", chassisID.toString());
    }
    
    @Test
    public void testChassisIDComparison() {
        DataCenterEntityID chassisIDX = DataCenterEntityID.createChassisID(1, 1);
        assertEquals("1.1.0", chassisIDX.toString());
        DataCenterEntityID chassisIDY = DataCenterEntityID.createChassisID(1, 2);
        assertEquals("1.2.0", chassisIDY.toString());
        DataCenterEntityID chassisIDZ = DataCenterEntityID.createChassisID(1, 3);
        assertEquals("1.3.0", chassisIDZ.toString());
        assertFalse(chassisIDX.equals(chassisIDY));
        assertFalse(chassisIDY.equals(chassisIDZ));
        assertFalse(chassisIDX.equals(chassisIDZ));        
    }
    
    @Test
    public void testServerIDCreation() {
        DataCenterEntityID rackID = DataCenterEntityID.createServerID(1, 2, 3);
        assertEquals("1.2.3", rackID.toString());
    }
    
    @Test
    public void testServerIDComparison() {
        DataCenterEntityID serverIDX = DataCenterEntityID.createServerID(1, 1, 1);
        assertEquals("1.1.1", serverIDX.toString());
        DataCenterEntityID serverIDY = DataCenterEntityID.createServerID(1, 1, 2);
        assertEquals("1.1.2", serverIDY.toString());
        DataCenterEntityID serverIDZ = DataCenterEntityID.createServerID(1, 1, 3);
        assertEquals("1.1.3", serverIDZ.toString());
        assertFalse(serverIDX.equals(serverIDY));
        assertFalse(serverIDY.equals(serverIDZ));
        assertFalse(serverIDX.equals(serverIDZ));        
    }
    
    @Test
    public void sort() {
        List<DataCenterEntityID> list = new ArrayList<DataCenterEntityID>();
        list.add(DataCenterEntityID.createServerID(1, 1, 3));
        list.add(DataCenterEntityID.createChassisID(1, 1));
        list.add(DataCenterEntityID.createServerID(1, 1, 1));
        list.add(DataCenterEntityID.createRackID(1));
        list.add(DataCenterEntityID.createServerID(1, 1, 2));
        
        
        assertEquals("[1.1.3, 1.1.0, 1.1.1, 1.0.0, 1.1.2]", list.toString());
        
        Collections.sort(list);
        
        assertEquals("[1.0.0, 1.1.0, 1.1.1, 1.1.2, 1.1.3]", list.toString());
    }
}
