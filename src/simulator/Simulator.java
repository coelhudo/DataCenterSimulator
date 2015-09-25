package simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.schedulers.Scheduler;
import simulator.schedulers.FIFOScheduler;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveSystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;
import simulator.am.ApplicationAM;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.am.GeneralAM;
import simulator.utils.ActivitiesLogger;

public class Simulator {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    private void run() throws IOException {
        ///////////////////////
        while (!areSystemsDone()) {
            // LOGGER.info("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePower();
            dataCenter.calculatePower();
            environment.updateCurrentLocalTime();
            dataCenter.GetStats();
            // ////Data Center Level AM MAPE Loop
            // if(Main.localTime%1==0)
            // {
            // mesg++;
            // DataCenter.AM.monitor();
            // DataCenter.AM.analysis(0);
            // }
            // ///////////////
        }
    }

    public Simulator(SimulatorPOD simulatorPOD, Environment environment) {
        this.environment = environment;
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

    // private int epochSys = 120, epochSideApp = 120;
    // private List<ResponseTime> responseArray;
    // public int communicationAM = 0;
    private DataCenter dataCenter;
    private Environment environment;
    private Systems systems;
    private SLAViolationLogger slaViolationLogger;
    private DataCenterAM dataCenterAM;

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

    public static void main(String[] args) throws IOException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        SimulatorPOD simulatorPOD = dataCenterBuilder.build();

        Environment environment = new Environment();
        Simulator simulator = new Simulator(simulatorPOD, environment);
        SimulationResults results = simulator.execute();
        LOGGER.info("Total energy Consumption= " + results.getTotalPowerConsumption());
        LOGGER.info("LocalTime= " + results.getLocalTime());
        LOGGER.info("Mean Power Consumption= " + results.getMeanPowerConsumption());
        LOGGER.info("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }

    public SimulationResults execute() throws IOException {
        LOGGER.info("Systems start running");
        run();
        csFinalize();
        LOGGER.info("Simulation finished");
        return new SimulationResults(this);
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
