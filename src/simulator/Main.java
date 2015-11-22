package simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.Simulator.StrategyEnum;
import simulator.am.ApplicationAM;
import simulator.am.ComputeSystemAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.am.InteractiveSystemAM;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemFactory;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveSystemFactory;
import simulator.system.InteractiveSystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());
    
    public static void main(String[] args) throws IOException, InterruptedException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        Injector injector = Guice.createInjector(new MainModule());

        Systems systems = injector.getInstance(Systems.class);

        SystemsPOD systemsPOD = injector.getInstance(SystemsPOD.class);
        for (EnterpriseSystemPOD enterpriseSystemPOD : systemsPOD.getEnterpriseSystemsPOD()) {
            EnterpriseSystemAM enterpriseSystemAM = injector.getInstance(EnterpriseSystemAM.class);

            Scheduler scheduler = injector.getInstance(FIFOScheduler.class);
            ResourceAllocation resourceAllocation = injector.getInstance(MHR.class); 
            List<EnterpriseApp> applications = new ArrayList<EnterpriseApp>();
            for (EnterpriseApplicationPOD pod : enterpriseSystemPOD.getApplicationPODs()) {
                ApplicationAM applicationAM = new ApplicationAM(applications, enterpriseSystemAM,
                        injector.getInstance(Environment.class));
                EnterpriseApp enterpriseApplication = new EnterpriseApp(pod, scheduler, resourceAllocation, injector.getInstance(Environment.class));
                enterpriseApplication.setAM(applicationAM);
                applications.add(enterpriseApplication);
            }
            EnterpriseSystem enterpriseSystem = new EnterpriseSystem(enterpriseSystemPOD, applications, scheduler, resourceAllocation);
            enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
            enterpriseSystem.setAM(enterpriseSystemAM);
            systems.addEnterpriseSystem(enterpriseSystem);
        }
        
        ComputeSystemFactory computeSystemFactory = injector.getInstance(ComputeSystemFactory.class);
        for (ComputeSystemPOD computeSystemPOD : systemsPOD.getComputeSystemsPOD()) {
            ComputeSystem computeSystem = computeSystemFactory.create(computeSystemPOD);
            computeSystem.getResourceAllocation().initialResourceAloc(computeSystem);
            computeSystem.setAM(injector.getInstance(ComputeSystemAM.class));
            systems.addComputeSystem(computeSystem);
        }

        for (InteractiveSystemPOD interactivePOD : systemsPOD.getInteractiveSystemsPOD()) {
            InteractiveSystemFactory interactiveSystemFactory = injector.getInstance(InteractiveSystemFactory.class);
            InteractiveSystem interactiveSystem =interactiveSystemFactory.create(interactivePOD);
            interactiveSystem.getResourceAllocation().initialResourceAlocator(interactiveSystem);
            interactiveSystem.setAM(injector.getInstance(InteractiveSystemAM.class));
            systems.addInteractiveSystem(interactiveSystem);
        }

        final DataCenterAM dataCenterAM = new DataCenterAM(injector.getInstance(Environment.class), systems);
        dataCenterAM.setStrategy(StrategyEnum.Green);

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
                dataCenterAM.resetBlockTimer();
            }
        }

        injector.getInstance(DataCenter.class).setAM(dataCenterAM);

        systems.addObserver(new DataCenterAMXunxo());

        injector.injectMembers(systems);

        Simulator simulator = injector.getInstance(Simulator.class);

        PartialDataCenterStatsConsumer partialDataCenterStatsConsumer = injector
                .getInstance(PartialDataCenterStatsConsumer.class);

        Thread simulatorThread = new Thread(simulator);
        Thread partialResultConsumerThread = new Thread(partialDataCenterStatsConsumer);
        simulatorThread.start();
        partialResultConsumerThread.start();
        simulatorThread.join();
        partialResultConsumerThread.join();

        SimulationResults results = new SimulationResults(simulator);
        LOGGER.info("Total energy Consumption= " + results.getTotalPowerConsumption());
        LOGGER.info("LocalTime= " + results.getLocalTime());
        LOGGER.info("Mean Power Consumption= " + results.getMeanPowerConsumption());
        LOGGER.info("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }
}
