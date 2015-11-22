package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.SimulationResults;
import simulator.Simulator;
import simulator.Simulator.StrategyEnum;
import simulator.am.ApplicationAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;

public class SimulatorIT {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {

        Injector injector = Guice.createInjector(new ITModule());

        Systems systems = injector.getInstance(Systems.class);

        SystemsPOD systemsPOD = injector.getInstance(SystemsPOD.class);
        for (EnterpriseSystemPOD enterpriseSystemPOD : systemsPOD.getEnterpriseSystemsPOD()) {
            EnterpriseSystemAM enterpriseSystemAM = new EnterpriseSystemAM(injector.getInstance(Environment.class),
                    injector.getInstance(SLAViolationLogger.class));
            Scheduler scheduler = new FIFOScheduler();
            ResourceAllocation resourceAllocation = new MHR(injector.getInstance(Environment.class),
                    injector.getInstance(DataCenter.class));
            List<EnterpriseApp> applications = new ArrayList<EnterpriseApp>();
            for (EnterpriseApplicationPOD pod : enterpriseSystemPOD.getApplicationPODs()) {
                ApplicationAM applicationAM = new ApplicationAM(applications, enterpriseSystemAM,
                        injector.getInstance(Environment.class));
                EnterpriseApp enterpriseApplication = new EnterpriseApp(pod, scheduler, resourceAllocation,
                        injector.getInstance(Environment.class));
                enterpriseApplication.setAM(applicationAM);
                applications.add(enterpriseApplication);
            }
            EnterpriseSystem enterpriseSystem = new EnterpriseSystem(enterpriseSystemPOD, applications, scheduler,
                    resourceAllocation, enterpriseSystemAM);
            enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
            enterpriseSystem.setupAM();
            systems.addEnterpriseSystem(enterpriseSystem);
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
        systems.setup();

        injector.injectMembers(systems);

        Simulator simulator = injector.getInstance(Simulator.class);

        simulator.run();
        SimulationResults results = new SimulationResults(simulator);
        final double expectedTotalPowerConsumption = 7.555028576875995E9;
        assertEquals(expectedTotalPowerConsumption, results.getTotalPowerConsumption(), 1.0E-5);
        final double expectedLocalTime = 686293.0;
        assertEquals(expectedLocalTime, results.getLocalTime(), 0.01);
        final double meanPowerConsumption = 11008.459326;
        assertEquals(meanPowerConsumption, results.getMeanPowerConsumption(), 1.0E5);
        final int expectedOverRedTemperatureNumber = 0;
        assertEquals(expectedOverRedTemperatureNumber, results.getOverRedTemperatureNumber());
        final int expectedNumberMessagesFromDataCenterToSystem = 11508;
        assertEquals(expectedNumberMessagesFromDataCenterToSystem, results.getNumberOfMessagesFromDataCenterToSystem());
        final int expectedNumberMessagesFromSystemToNodes = 198253;
        assertEquals(expectedNumberMessagesFromSystemToNodes, results.getNumberOfMessagesFromSystemToNodes());
    }

}
