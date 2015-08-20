package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import simulator.Simulator;
import simulator.am.DataCenterAM;
import simulator.physical.DataCenter;
import simulator.system.ComputeSystem;
import simulator.system.EnterpriseSystem;
import simulator.system.InteractiveSystem;
import simulator.system.Systems;

public class ReadingConfigurationTest {

    @Test
    public void testSimulatorBuilder() {
        Simulator simulator = new Simulator();
        simulator.initialize("configs/DC_Logic.xml");

        DataCenter dataCenter = simulator.getDatacenter();
        Systems systems = simulator.getSystems();
        List<ComputeSystem> computeSystems = systems.getComputeSystems();
        List<EnterpriseSystem> enterpriseSystems = systems.getEnterpriseSystems();
        List<InteractiveSystem> interactiveSystems = systems.getInteractiveSystems();

        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertFalse(computeSystems.isEmpty());
        assertFalse(enterpriseSystems.isEmpty());
        assertTrue(interactiveSystems.isEmpty());
        assertEquals(1, computeSystems.size());
        assertEquals(1, enterpriseSystems.size());
        
        DataCenterAM dataCenterAM = dataCenter.getAM();
        assertFalse(dataCenterAM.isSlowDownFromCooler());
        assertEquals(0, dataCenterAM.getBlockTimer());
        
        ComputeSystem computeSystem = computeSystems.get(0);
        assertFalse(computeSystem.isDone());
        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertEquals("HPC_First", computeSystem.getName());
        assertFalse(computeSystem.getComputeNodeIndex().isEmpty());
        assertEquals(35, computeSystem.getComputeNodeIndex().size());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(35, computeSystem.getComputeNodeList().size()); //TODO: check how the last two lists are correlated.
        assertEquals(35, computeSystem.numberOfRunningNode()); //FIXME: is this correct? Check Active (below) vs Running
        assertEquals(0, computeSystem.numberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(35, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getSLAviolation());
        assertFalse(computeSystem.isBlocked());
        assertNotEquals(null, computeSystem.getResourceAllocation());
        assertNotEquals(null, computeSystem.getScheduler());
        
        EnterpriseSystem enterpriseSystem = enterpriseSystems.get(0);
        assertFalse(enterpriseSystem.isDone());
        assertEquals(0, enterpriseSystem.getAccumolatedViolation());
        assertEquals("Enterprise_01", enterpriseSystem.getName());
        assertFalse(enterpriseSystem.getComputeNodeIndex().isEmpty());
        assertEquals(8, enterpriseSystem.getComputeNodeIndex().size());
        assertFalse(enterpriseSystem.getComputeNodeList().isEmpty());
        assertEquals(8, enterpriseSystem.getComputeNodeList().size());
        assertEquals(0, enterpriseSystem.getAccumolatedViolation());
        assertEquals(0, enterpriseSystem.getNumberOfActiveServ());
        assertEquals(8, enterpriseSystem.getNumberOfNode());
        assertEquals(0.0, enterpriseSystem.getPower(), 1.0E-8);
        assertEquals(0, enterpriseSystem.getSLAviolation());
        assertNotEquals(null, enterpriseSystem.getResourceAllocation());
        assertNotEquals(null, enterpriseSystem.getScheduler());
        
    }
}
