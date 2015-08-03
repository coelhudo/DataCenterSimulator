package simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.physical.DataCenter;

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

    public Simulator() {
    }

    public void initialize(String config) {
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder(environment, slaViolationLogger);
        dataCenterBuilder.buildLogicalDataCenter(config);

        datacenter = dataCenterBuilder.getDataCenter();
        systems = dataCenterBuilder.getSystems();

        // set the overal policy here
        // Data Center is green!
        datacenter.getAM().setStrategy(StrategyEnum.Green);
        
        class DataCenterAMXunxo implements Observer {
            @Override
            public void update(Observable o, Object arg) {
                LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
                datacenter.getAM().resetBlockTimer();
            }
        }
        
        systems.addObserver(new DataCenterAMXunxo());
        // CS.get(0).AM.strtg=strategyEnum.SLA;
        // CS.get(1).AM.strtg=strategyEnum.Green;

    }

    private Environment environment = new Environment();
    // private int epochSys = 120, epochSideApp = 120;
    // private List<ResponseTime> responseArray;
    // public int communicationAM = 0;
    private DataCenter datacenter;
    private SLAViolationLogger slaViolationLogger = new SLAViolationLogger(environment);
    private Systems systems;

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
        // FIXME: 50?
        for (int i = 0; i < 50; i++) {
            datacenter.getChassisSet().get(i).getServers().get(0).setReady(-1);
            datacenter.getChassisSet().get(i).getServers().get(0).setMips(1);// 1.04
                                                                             // 1.4;
            datacenter.getChassisSet().get(i).getServers().get(0).setCurrentCPU(100);
        }
        datacenter.calculatePower();
    }

    public static void main(String[] args) throws IOException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        Simulator simulator = new Simulator();
        SimulationResults results = simulator.execute();
        // LOGGER.info("Total JOBs= "+CS.totalJob);
        LOGGER.info("Total energy Consumption= " + results.getTotalPowerConsumption());
        LOGGER.info("LocalTime= " + results.getLocalTime());
        LOGGER.info("Mean Power Consumption= " + results.getTotalPowerConsumption() / results.getLocalTime());
        LOGGER.info("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }

    public SimulationResults execute() throws IOException {
        initialize("configs/DC_Logic.xml");
        LOGGER.info("Systems start running");
        run();
        csFinalize();
        LOGGER.info("Simulation finished");
        return new SimulationResults(this);
    }

    void csFinalize() {
        systems.logTotalResponseTimeComputeSystem();
        try {
            datacenter.shutDownDC();
            slaViolationLogger.finish();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }

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
