package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Simulator;
import simulator.Simulator.StrategyEnum;
import simulator.am.ApplicationAM;
import simulator.am.ComputeSystemAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;
import simulator.physical.Rack;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.LeastRemainFirstScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveSystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;

public class ReadingConfigurationIT {

    @Test
    public void testSimulatorBuilder() {
        Injector injector = Guice.createInjector(new ITModule());
        
        Systems systems = injector.getInstance(Systems.class);

        SystemsPOD systemsPOD = injector.getInstance(SystemsPOD.class);
        for (EnterpriseSystemPOD enterpriseSystemPOD : systemsPOD.getEnterpriseSystemsPOD()) {
            EnterpriseSystemAM enterpriseSystemAM = new EnterpriseSystemAM(injector.getInstance(Environment.class),
                    injector.getInstance(SLAViolationLogger.class));
            Scheduler scheduler = new FIFOScheduler();
            ResourceAllocation resourceAllocation = new MHR(injector.getInstance(Environment.class), injector.getInstance(DataCenter.class));
            List<EnterpriseApp> applications = new ArrayList<EnterpriseApp>();
            for (EnterpriseApplicationPOD pod : enterpriseSystemPOD.getApplicationPODs()) {
                ApplicationAM applicationAM = new ApplicationAM(applications, enterpriseSystemAM, injector.getInstance(Environment.class));
                EnterpriseApp enterpriseApplication = EnterpriseApp.create(pod, scheduler, resourceAllocation, injector.getInstance(Environment.class),
                        applicationAM);
                applications.add(enterpriseApplication);
            }
            systems.addEnterpriseSystem(EnterpriseSystem.create(enterpriseSystemPOD, scheduler, resourceAllocation,
                    enterpriseSystemAM, applications));
        }
        for (ComputeSystemPOD computeSystemPOD : systemsPOD.getComputeSystemsPOD()) {
            systems.addComputeSystem(ComputeSystem.create(computeSystemPOD, injector.getInstance(Environment.class),
                    new LeastRemainFirstScheduler(),
                    new MHR(injector.getInstance(Environment.class), injector.getInstance(DataCenter.class)),
                    injector.getInstance(SLAViolationLogger.class),
                    new ComputeSystemAM(injector.getInstance(Environment.class))));
        }

        for (InteractiveSystemPOD interactivePOD : systemsPOD.getInteractiveSystemsPOD()) {
            systems.addInteractiveSystem(InteractiveSystem.create(interactivePOD,
                    injector.getInstance(Environment.class), new FIFOScheduler(),
                    new MHR(injector.getInstance(Environment.class), injector.getInstance(DataCenter.class)),
                    injector.getInstance(SLAViolationLogger.class)));
        }

        final DataCenterAM dataCenterAM = new DataCenterAM(injector.getInstance(Environment.class), systems);
        dataCenterAM.setStrategy(StrategyEnum.Green);

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                dataCenterAM.resetBlockTimer();
            }
        }

        injector.getInstance(DataCenter.class).setAM(dataCenterAM);

        systems.addObserver(new DataCenterAMXunxo());
        
        injector.injectMembers(systems);
        
        Simulator simulator = injector.getInstance(Simulator.class);

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

        List<ComputeSystem> computeSystems = systems.getComputeSystems();
        List<EnterpriseSystem> enterpriseSystems = systems.getEnterpriseSystems();
        List<InteractiveSystem> interactiveSystems = systems.getInteractiveSystems();

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
