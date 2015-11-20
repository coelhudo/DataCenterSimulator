package simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.am.ApplicationAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.am.ComputeSystemAM;
import simulator.am.GeneralAM;
import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
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
import simulator.utils.ActivitiesLogger;

public class Simulator implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    // private int epochSys = 120, epochSideApp = 120;
    private DataCenter dataCenter;
    private Environment environment;
    private Systems systems;
    private BlockingQueue<DataCenterStats> partialResults;
    
    @Inject
    public Simulator(SimulatorPOD simulatorPOD, Environment environment,
            BlockingQueue<DataCenterStats> partialResults, SLAViolationLogger slaViolationLogger) {
        this.environment = environment;
        this.partialResults = partialResults;

        slaViolationLogger = new SLAViolationLogger(environment);
        systems = new Systems(environment);

        ActivitiesLogger activitiesLogger = new ActivitiesLogger("out_W.txt");
        final DataCenterAM dataCenterAM = new DataCenterAM(environment, systems);
        dataCenterAM.setStrategy(StrategyEnum.Green);
        dataCenter = new DataCenter(simulatorPOD.getDataCenterPOD(), dataCenterAM, activitiesLogger, environment);

        SystemsPOD systemsPOD = simulatorPOD.getSystemsPOD();
        loadEnterpriseSystemIntoSystems(systems, systemsPOD.getEnterpriseSystemsPOD(), slaViolationLogger);
        for (ComputeSystemPOD computeSystemPOD : systemsPOD.getComputeSystemsPOD()) {
            systems.addComputeSystem(
                    ComputeSystem.create(computeSystemPOD, environment, new LeastRemainFirstScheduler(),
                            new MHR(environment, dataCenter), slaViolationLogger, new ComputeSystemAM(environment)));
        }

        for (InteractiveSystemPOD interactivePOD : systemsPOD.getInteractiveSystemsPOD()) {
            systems.addInteractiveSystem(InteractiveSystem.create(interactivePOD, environment, new FIFOScheduler(),
                    new MHR(environment, dataCenter), slaViolationLogger));
        }

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
                dataCenterAM.resetBlockTimer();
            }
        }

        systems.addObserver(new DataCenterAMXunxo());
    }

    public void run() {
        int count = 0;
        int skipStats = 0;
        
        while (!areSystemsDone()) {
            // LOGGER.info("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePower();
            dataCenter.calculatePower();
            environment.updateCurrentLocalTime();
            try {
                if (dataCenter.newStatsAvailable() && (skipStats++ % 5 == 0)) {
                    count++;
                    partialResults.put(dataCenter.getStats());
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            // ////Data Center Level AM MAPE Loop
            // if(Main.localTime%1==0)
            // {
            // mesg++;
            // DataCenter.AM.monitor();
            // DataCenter.AM.analysis(0);
            // }
            // ///////////////
        }

        LOGGER.info("Simulation done, messages created " + count);

        csFinalize();
    }

    private void loadEnterpriseSystemIntoSystems(Systems systems, List<EnterpriseSystemPOD> enterpriseSystemPODs, SLAViolationLogger slaViolationLogger) {
        for (EnterpriseSystemPOD enterpriseSystemPOD : enterpriseSystemPODs) {
            EnterpriseSystemAM enterpriseSystemAM = new EnterpriseSystemAM(environment, slaViolationLogger);
            Scheduler scheduler = new FIFOScheduler();
            ResourceAllocation resourceAllocation = new MHR(environment, dataCenter);
            List<EnterpriseApp> applications = loadEnterpriseSystemApplications(
                    enterpriseSystemPOD.getApplicationPODs(), enterpriseSystemAM, resourceAllocation, scheduler);
            systems.addEnterpriseSystem(EnterpriseSystem.create(enterpriseSystemPOD, scheduler, resourceAllocation,
                    enterpriseSystemAM, applications));
        }
    }

    private List<EnterpriseApp> loadEnterpriseSystemApplications(
            List<EnterpriseApplicationPOD> enterpriseApplicationPODs, GeneralAM enterpriseSystemAM,
            ResourceAllocation resourceAllocation, Scheduler scheduler) {
        List<EnterpriseApp> applications = new ArrayList<EnterpriseApp>();
        for (EnterpriseApplicationPOD pod : enterpriseApplicationPODs) {
            ApplicationAM applicationAM = new ApplicationAM(applications, enterpriseSystemAM, environment);
            EnterpriseApp enterpriseApplication = EnterpriseApp.create(pod, scheduler, resourceAllocation, environment,
                    applicationAM);
            applications.add(enterpriseApplication);
        }

        return applications;
    }

    protected double getTotalPowerConsumption() {
        return dataCenter.getTotalPowerConsumption();
    }

    protected int getOverRedTempNumber() {
        return dataCenter.getOverRed();
    }

    public DataCenter getDatacenter() {
        return dataCenter;
    }

    public enum StrategyEnum {
        Green, SLA
    };

    public boolean anySystem() {
        return systems.allJobsDone();
    }

    public void allSystemRunACycle() {
        systems.runACycle();
    }

    public void allSystemCalculatePower() {
        systems.calculatePower();
    }

    void csFinalize() {
        systems.logTotalResponseTimeComputeSystem();
        dataCenter.shutDownDC();
    }

    public boolean areSystemsDone() {
        return systems.removeJobsThatAreDone();

    }

    public Environment getEnvironment() {
        return environment;
    }

    public Systems getSystems() {
        return systems;
    }
}
