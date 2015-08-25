package simulator;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import simulator.physical.DataCenter;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveSystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;
import simulator.am.DataCenterAM;
import simulator.utils.ActivitiesLogger;

public class Simulator {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    private void run() throws IOException {
        ///////////////////////
        while (!areSystemsDone()) {
            // LOGGER.info("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePower();
            datacenter.calculatePower();
            environment.updateCurrentLocalTime();
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
        datacenter = new DataCenter(simulatorPOD.getDataCenterPOD(), dataCenterAM, activitiesLogger, environment);
        SystemsPOD systemsPOD = simulatorPOD.getSystemsPOD();
        for (EnterpriseSystemPOD enterprisesystemPOD : systemsPOD.getEnterpriseSystemsPOD()) {
            systems.addEnterpriseSystem(
                    EnterpriseSystem.Create(enterprisesystemPOD, environment, datacenter, slaViolationLogger));
        }
        for (ComputeSystemPOD computeSystemPOD : systemsPOD.getComputeSystemsPOD()) {
            systems.addComputeSystem(
                    ComputeSystem.Create(computeSystemPOD, environment, datacenter, slaViolationLogger));
        }
        for (InteractiveSystemPOD interactivePOD : systemsPOD.getInteractiveSystemsPOD()) {
            systems.addInteractiveSystem(
                    InteractiveSystem.Create(interactivePOD, environment, datacenter, slaViolationLogger));
        }

        datacenter.getAM().setStrategy(StrategyEnum.Green);

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
                datacenter.getAM().resetBlockTimer();
            }
        }

        systems.addObserver(new DataCenterAMXunxo());
    }

    // private int epochSys = 120, epochSideApp = 120;
    // private List<ResponseTime> responseArray;
    // public int communicationAM = 0;
    private DataCenter datacenter;
    private Environment environment;
    private Systems systems;
    private SLAViolationLogger slaViolationLogger;
    private DataCenterAM dataCenterAM;
    
    protected double getTotalPowerConsumption() {
        return datacenter.getTotalPowerConsumption();
    }

    protected int getOverRedTempNumber() {
        return datacenter.getOverRed();
    }

    public DataCenter getDatacenter() {
        return datacenter;
    }

    public enum StrategyEnum {

        Green, SLA
    };

    public boolean anySysetm() {
        return systems.allJobsDone();
    }

    public void allSystemRunACycle() throws IOException {
        systems.runACycle();
    }

    public void allSystemCalculatePower() throws IOException {
        systems.calculatePower();
    }

    public void GetStat() {
        for (int i = 0; i < 50; i++) {
            datacenter.getChassisSet().get(i).getServers().get(0).setStatusAsIdle();
            datacenter.getChassisSet().get(i).getServers().get(0).setMips(1);// 1.04
                                                                             // 1.4;
            datacenter.getChassisSet().get(i).getServers().get(0).setCurrentCPU(100);
        }
        datacenter.calculatePower();
    }

    public static void main(String[] args) throws IOException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        Environment environment = new Environment();
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml", environment);
        SimulatorPOD simulatorPOD = dataCenterBuilder.buildLogicalDataCenter();

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
        datacenter.shutDownDC();
    }

    public boolean areSystemsDone() {
        return systems.removeJobsThatAreDone();

    }
    // void getPeakEstimate()
    // {
    // File f;
    // peakEstimate=new double[71];
    // BufferedReader bis = null;
    // try {
    // f = new File("Z:\\PWMNG\\peakEstimation3times.txt");
    // bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
    // } catch (IOException e) {
    // LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
    // } finally {
    // }
    // try {
    // String line = bis.readLine();
    // int i=0;
    // while(line!=null)
    // {
    // String[] numbers= new String[1];
    // numbers = line.trim().split(" ");
    // peakEstimate[i++] = Double.parseDouble(numbers[0]);
    // //LOGGER.info("Readed inputTime= " + inputTime + " Job Reqested
    // Time=" + j.startTime+" Total job so far="+ total);
    // line = bis.readLine();
    // }
    // } catch (IOException ex) {
    // LOGGER.info("readJOB EXC readJOB false ");
    // Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
    // }
    // }
    /*
     * void coordinator(int times) { //return every server in ready state then
     * try to make some of them idle for(int
     * j=0;j<webSet1.ComputeNodeList.size();j++) {
     * webSet1.ComputeNodeList.get(j).ready=1;
     * webSet1.ComputeNodeList.get(j).currentCPU=0;
     * webSet1.ComputeNodeList.get(j).Mips=1; }
     * ///////////////////////////////////////////////////////////////////////
     * int suc=0; if(times>=70) return; double peak=peakEstimate[times]; int
     * numberOfidleServer=webSet1.ComputeNodeList.size()-(int)Math.ceil(peak/
     * 1000); //LOGGER.info(numberOfidleServer); if(numberOfidleServer<0)
     * return; for(int j=0;j<numberOfidleServer;j++)
     * if(webSet1.ComputeNodeList.get(j).queueLength==0) { suc++;
     * webSet1.ComputeNodeList.get(j).ready=-1;
     * webSet1.ComputeNodeList.get(j).currentCPU=0;
     * webSet1.ComputeNodeList.get(j).Mips=1; } else {LOGGER.info(
     * "In Coordinator and else   ");numberOfidleServer++;} //border ra jabeja
     * mikonim //if(suc==numberOfidleServer) LOGGER.info(numberOfidleServer+
     * "\t suc= "+suc); } public void addToresponseArray(double num,int time) {
     * responseTime t= new responseTime(); t.numberOfJob=num;
     * t.responseTime=time; responseArray.add(t); return; }
     */

    public Environment getEnvironment() {
        return environment;
    }

    public Systems getSystems() {
        return systems;
    }
}
