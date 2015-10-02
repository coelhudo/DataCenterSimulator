package simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.am.ApplicationAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.am.GeneralAM;
import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
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
    private SLAViolationLogger slaViolationLogger;
    private DataCenterAM dataCenterAM;
    private BlockingQueue<DataCenterStats> partialResults;
    
    public void run() {
        while (!areSystemsDone()) {
            // LOGGER.info("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePower();
            dataCenter.calculatePower();
            environment.updateCurrentLocalTime();
            try {
                partialResults.put(dataCenter.GetStats());
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
        
        LOGGER.info("Simulation done");
        
        csFinalize();
    }

    public Simulator(SimulatorPOD simulatorPOD, Environment environment, BlockingQueue<DataCenterStats> partialResults) {
        this.environment = environment;
        this.partialResults = partialResults;
        ActivitiesLogger activitiesLogger = new ActivitiesLogger("out_W.txt");
        slaViolationLogger = new SLAViolationLogger(environment);
        systems = new Systems(environment);
        dataCenterAM = new DataCenterAM(environment, systems);
        dataCenter = new DataCenter(simulatorPOD.getDataCenterPOD(), dataCenterAM, activitiesLogger, environment);
        SystemsPOD systemsPOD = simulatorPOD.getSystemsPOD();
        loadEnterpriseSystemIntoSystems(systems, systemsPOD.getEnterpriseSystemsPOD());
        for (ComputeSystemPOD computeSystemPOD : systemsPOD.getComputeSystemsPOD()) {
            systems.addComputeSystem(
                    ComputeSystem.create(computeSystemPOD, environment, dataCenter, slaViolationLogger));
        }
        for (InteractiveSystemPOD interactivePOD : systemsPOD.getInteractiveSystemsPOD()) {
            systems.addInteractiveSystem(
                    InteractiveSystem.create(interactivePOD, environment, dataCenter, slaViolationLogger));
        }

        dataCenter.getAM().setStrategy(StrategyEnum.Green);

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
                dataCenter.getAM().resetBlockTimer();
            }
        }

        systems.addObserver(new DataCenterAMXunxo());
    }

    private void loadEnterpriseSystemIntoSystems(Systems systems, List<EnterpriseSystemPOD> enterpriseSystemPODs) {
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

    public boolean anySysetm() {
        return systems.allJobsDone();
    }

    public void allSystemRunACycle() {
        systems.runACycle();
    }

    public void allSystemCalculatePower() {
        systems.calculatePower();
    }

    void csFinalize() {
        slaViolationLogger.finish();
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
