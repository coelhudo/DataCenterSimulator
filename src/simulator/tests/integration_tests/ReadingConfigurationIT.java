package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import simulator.Environment;
import simulator.Simulator;
import simulator.SimulatorBuilder;
import simulator.SimulatorPOD;
import simulator.am.DataCenterAM;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;
import simulator.physical.Rack;
import simulator.system.ComputeSystem;
import simulator.system.EnterpriseSystem;
import simulator.system.InteractiveSystem;
import simulator.system.Systems;

public class ReadingConfigurationIT {

    @Test
    public void testSimulatorBuilder() {
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        SimulatorPOD simulatorPOD = dataCenterBuilder.build();

        Environment environment = new Environment();
        Simulator simulator = new Simulator(simulatorPOD, environment);

        DataCenter dataCenter = simulator.getDatacenter();
        Collection<Rack> racks = dataCenter.getRacks();
        assertEquals(10, racks.size());
        Set<DataCenterEntityID> chassisIDs = new HashSet<DataCenterEntityID>();
        Set<DataCenterEntityID> serverIDs = new HashSet<DataCenterEntityID>();
        
        for (Rack rack : racks) {
            for (Chassis chassis : rack.getChassis()) {
                assertEquals(DataCenterEntityID.createChassisID(rack.getID().getRackID()+1, chassis.getID().getChassisID()+1), chassis.getID());
                chassisIDs.add(chassis.getID());
                assertEquals(1, chassis.getServers().size());
                for (BladeServer bladeServer : chassis.getServers()) {
                    serverIDs.add(bladeServer.getID());
                    assertEquals("HP Proliant DL3", bladeServer.getBladeType());
                    assertEquals(1.0, bladeServer.getFrequencyLevelAt(0), 1.0E-8);
                    assertEquals(1.04, bladeServer.getFrequencyLevelAt(1), 1.0E-8);
                    assertEquals(1.4, bladeServer.getFrequencyLevelAt(2), 1.0E-8);
                    assertEquals(300.0, bladeServer.getPowerBusyAt(0), 1.0E-8);
                    assertEquals(336.0, bladeServer.getPowerBusyAt(1), 1.0E-8);
                    assertEquals(448.0, bladeServer.getPowerBusyAt(2), 1.0E-8);
                    assertEquals(100.0, bladeServer.getPowerIdleAt(0), 1.0E-8);
                    assertEquals(100.0, bladeServer.getPowerIdleAt(1), 1.0E-8);
                    assertEquals(128.0, bladeServer.getPowerIdleAt(2), 1.0E-8);
                    assertEquals(5.0, bladeServer.getIdleConsumption(), 1.0E-8);
                }
            }
        }
        assertEquals(50, chassisIDs.size());
        assertEquals(50, serverIDs.size());

        Systems systems = simulator.getSystems();
        List<ComputeSystem> computeSystems = systems.getComputeSystems();
        List<EnterpriseSystem> enterpriseSystems = systems.getEnterpriseSystems();
        List<InteractiveSystem> interactiveSystems = systems.getInteractiveSystems();

        DataCenterAM dataCenterAM = dataCenter.getAM();
        assertFalse(dataCenterAM.isSlowDownFromCooler());
        assertEquals(0, dataCenterAM.getBlockTimer());

        assertEquals(0.0, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertFalse(computeSystems.isEmpty());
        assertFalse(enterpriseSystems.isEmpty());
        assertTrue(interactiveSystems.isEmpty());
        assertEquals(1, computeSystems.size());

        ComputeSystem computeSystem = computeSystems.get(0);

        Set<Integer> serversInComputeSystem = new HashSet<Integer>(Arrays.asList(0, 2, 3, 4, 5, 6, 7, 8, 9, 21, 20, 23,
                22, 25, 24, 27, 26, 29, 28, 35, 38, 39, 36, 37, 42, 43, 40, 41, 46, 47, 44, 45, 49, 48));
        Set<Integer> chassisInComputeSystem = new HashSet<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 21, 20,
                23, 22, 25, 24, 27, 26, 29, 28, 35, 38, 39, 36, 37, 42, 43, 40, 41, 46, 47, 44, 45, 49, 48));
        Set<Integer> racksInComputeSystem = new HashSet<Integer>(Arrays.asList(0, 1, 4, 5, 7, 8, 9));
        for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
            chassisInComputeSystem.remove(bladeServer.getID().getChassisID());
            racksInComputeSystem.remove(bladeServer.getID().getRackID());
            serversInComputeSystem.remove(bladeServer.getID().getServerID());
        }
        assertTrue(serversInComputeSystem.isEmpty());
        assertTrue(chassisInComputeSystem.isEmpty());
        assertTrue(racksInComputeSystem.isEmpty());

        assertFalse(computeSystem.isDone());
        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertEquals("HPC_First", computeSystem.getName());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(35, computeSystem.getComputeNodeList().size()); // TODO:
                                                                     // check
                                                                     // how the
                                                                     // last two
                                                                     // lists
                                                                     // are
                                                                     // correlated.
        assertEquals(35, computeSystem.numberOfRunningNode()); // FIXME: is this
                                                               // correct? Check
                                                               // Active (below)
                                                               // vs Running
        assertEquals(0, computeSystem.numberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(35, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getSLAviolation());
        assertFalse(computeSystem.isBlocked());
        assertNotEquals(null, computeSystem.getResourceAllocation());
        assertNotEquals(null, computeSystem.getScheduler());

        assertEquals(1, enterpriseSystems.size());
        EnterpriseSystem enterpriseSystem = enterpriseSystems.get(0);

        Set<Integer> serversInEnterpriseSystem = new HashSet<Integer>(Arrays.asList(17, 19, 18, 10, 11, 12, 13, 14));
        Set<Integer> chassisInEnterpriseSystem = new HashSet<Integer>(Arrays.asList(17, 19, 18, 10, 11, 12, 13, 14));
        Set<Integer> racksInEnterpriseSystem = new HashSet<Integer>(Arrays.asList(2, 3));
        for (BladeServer bladeServer : enterpriseSystem.getComputeNodeList()) {
            chassisInEnterpriseSystem.remove(bladeServer.getID().getChassisID());
            racksInEnterpriseSystem.remove(bladeServer.getID().getRackID());
            serversInEnterpriseSystem.remove(bladeServer.getID().getServerID());
        }
        assertTrue(serversInEnterpriseSystem.isEmpty());
        assertTrue(chassisInEnterpriseSystem.isEmpty());
        assertTrue(racksInEnterpriseSystem.isEmpty());

        assertFalse(enterpriseSystem.isDone());
        assertEquals(0, enterpriseSystem.getAccumolatedViolation());
        assertEquals("Enterprise_01", enterpriseSystem.getName());
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
