package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.physical.DataCenter;

public class Simulator {

    // FIXME: now this is with a lot of responsabilities. Need to break this
    // down.
    public class Environment {
        private int localTime = 1;
        private int numberOfMessagesFromDataCenterToSystem = 0;
        private int numberOfMessagesFromSystemToNodes = 0;

        public int getCurrentLocalTime() {
            return localTime;
        }

        protected void updateCurrentLocalTime() {
            localTime++;
        }

        public void logHPCViolation(String name, Violation slaViolation) {
            try {
                SLALogH.write(name + "\t" + getCurrentLocalTime() + "\t" + slaViolation + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void logEnterpriseViolation(String name, int slaViolationNum) {
            try {
                SLALogE.write(name + "\t" + getCurrentLocalTime() + "\t" + slaViolationNum + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void logInteractiveViolation(String name, int slaViolation) {
            try {
                SLALogI.write(name + "\t" + getCurrentLocalTime() + "\t" + slaViolation + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void updateNumberOfMessagesFromDataCenterToSystem() {
            numberOfMessagesFromDataCenterToSystem++;
        }

        public int getNumberOfMessagesFromDataCenterToSystem() {
            return numberOfMessagesFromDataCenterToSystem;
        }

        public void updateNumberOfMessagesFromSystemToNodes() {
            numberOfMessagesFromSystemToNodes++;
        }

        public int getNumberOfMessagesFromSystemToNodes() {
            return numberOfMessagesFromSystemToNodes;
        }

        public boolean localTimeByEpoch() {
            return localTime % EPOCH_APP != 0;
        }

        public List<InteractiveSystem> getInteractiveSystems() {
            return interactiveSystems;
        }

        public List<ComputeSystem> getComputeSystems() {
            return computeSystems;
        }

        public List<EnterpriseSystem> getEnterpriseSystems() {
            return enterpriseSystems;
        }
    }

    private void run() throws IOException {
        ///////////////////////
        while (!anySystem()) {
            // System.out.println("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePwr();
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
        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder(environment);
        dataCenterBuilder.buildLogicalDataCenter(config);

        datacenter = dataCenterBuilder.getDataCenter();
        enterpriseSystems = dataCenterBuilder.getEnterpriseSystems();
        interactiveSystems = dataCenterBuilder.getInteractiveSystems();
        computeSystems = dataCenterBuilder.getComputeSystems();

        try {
            SLALogE = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogE.txt")));
            SLALogI = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogI.txt")));
            SLALogH = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogH.txt")));
        } catch (IOException e) {
            System.out.println("Uh oh, got an IOException error!" + e.getMessage());
        } finally {
        }

        // set the overal policy here
        // Data Center is green!
        datacenter.getAM().setStrategy(StrategyEnum.Green);
        // CS.get(0).AM.strtg=strategyEnum.SLA;
        // CS.get(1).AM.strtg=strategyEnum.Green;

    }

    private Environment environment = new Environment();
    private final int EPOCH_APP = 60;
    private int epochSys = 120, epochSideApp = 120;
    private List<ResponseTime> responseArray;
    private List<InteractiveSystem> interactiveSystems;
    private List<EnterpriseSystem> enterpriseSystems;
    private List<ComputeSystem> computeSystems;
    private double[] peakEstimate;
    private OutputStreamWriter SLALogE = null;
    private OutputStreamWriter SLALogI = null;
    private OutputStreamWriter SLALogH = null;
    // public int communicationAM = 0;
    private DataCenter datacenter;

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
        for (int i = 0; i < enterpriseSystems.size(); i++) {
            if (!enterpriseSystems.get(i).isDone()) {
                return false;
            }
        }
        for (int i = 0; i < interactiveSystems.size(); i++) {
            if (!interactiveSystems.get(i).isDone()) {
                return false;
            }
        }
        for (int i = 0; i < computeSystems.size(); i++) {
            if (!computeSystems.get(i).isDone()) {
                return false; // still we have work to do
            }
        }
        return true; // there is no job left in all system
    }

    public void allSystemRunACycle() throws IOException {
        for (EnterpriseSystem enterpriseSystem : enterpriseSystems) {
            if (!enterpriseSystem.isDone()) {
                enterpriseSystem.runAcycle();
            }
        }

        for (ComputeSystem computeSystem : computeSystems) {
            if (!computeSystem.isDone()) {
                computeSystem.runAcycle();
            }
        }

        for (InteractiveSystem interactiveSystem : interactiveSystems) {
            if (!interactiveSystem.isDone()) {
                interactiveSystem.runAcycle();
            }
        }
    }

    public void allSystemCalculatePwr() throws IOException {

        for (EnterpriseSystem enterpriseSystem : enterpriseSystems) {
            enterpriseSystem.calculatePower();
        }
        for (ComputeSystem computeSystem : computeSystems) {
            computeSystem.calculatePower();
        }
        for (InteractiveSystem interactiveSystem : interactiveSystems) {
            interactiveSystem.calculatePower();
        }
    }

    public void GetStat() {
        //FIXME: 50?
        for (int i = 0; i < 50; i++) {
            datacenter.getChassisSet().get(i).getServers().get(0).setReady(-1);
            datacenter.getChassisSet().get(i).getServers().get(0).setMips(1);// 1.04 1.4;
            datacenter.getChassisSet().get(i).getServers().get(0).setCurrentCPU(100);
        }
        datacenter.calculatePower();
    }

    public static void main(String[] args) throws IOException {
        Simulator simulator = new Simulator();
        SimulationResults results = simulator.execute();
        // System.out.println("Total JOBs= "+CS.totalJob);
        System.out.println("Total energy Consumption= " + results.getTotalPowerConsumption());
        System.out.println("LocalTime= " + results.getLocalTime());
        System.out.println("Mean Power Consumption= " + results.getTotalPowerConsumption() / results.getLocalTime());
        System.out.println("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }

    public SimulationResults execute() throws IOException {
        initialize("configs/DC_Logic.xml");
        System.out.println("------------------------------------------");
        System.out.println("Systems start running");
        System.out.println("------------------------------------------");
        run();
        System.out.println("------------------------------------------");
        csFinalize();
        return new SimulationResults(this);
    }

    void csFinalize() {
        for (int i = 0; i < computeSystems.size(); i++) {
            System.out.println("Total Response Time in CS " + i + "th CS = " + computeSystems.get(i).finalized());
        }
        try {
            datacenter.shutDownDC();
            SLALogE.close();
            SLALogH.close();
            SLALogI.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean anySystem() {
        boolean retValue = true;
        for (int i = 0; i < enterpriseSystems.size(); i++) {
            if (!enterpriseSystems.get(i).isDone()) {
                retValue = false;
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time EnterSys: " + enterpriseSystems.get(i).getName() + " at time: "
                        + environment.getCurrentLocalTime());
                System.out.println("Computing Power Consumed by  " + enterpriseSystems.get(i).getName() + " is: "
                        + enterpriseSystems.get(i).getPower());
                // System.out.println("Number of violation:
                // "+ES.get(i).accumolatedViolation);

                enterpriseSystems.remove(i);
                i--;
            }
        }
        for (int i = 0; i < interactiveSystems.size(); i++) {
            if (!interactiveSystems.get(i).isDone()) {
                retValue = false;
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time Interactive sys:  " + interactiveSystems.get(i).getName()
                        + " at time: " + environment.getCurrentLocalTime());
                System.out.println(
                        "Interactive sys: Number of violation: " + interactiveSystems.get(i).getAccumolatedViolation());
                System.out.println("Computing Power Consumed by  " + interactiveSystems.get(i).getName() + " is: "
                        + interactiveSystems.get(i).getPower());
                interactiveSystems.remove(i);
                i--;

                // opps !! hardcoded policy
                datacenter.getAM().resetBlockTimer();
            }
        }
        for (int i = 0; i < computeSystems.size(); i++) {
            if (!computeSystems.get(i).isDone()) {
                retValue = false; // means still we have work to do
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time HPC_Sys:  " + computeSystems.get(i).getName() + " at time: "
                        + environment.getCurrentLocalTime());
                System.out.println("Total Response Time= " + computeSystems.get(i).finalized());
                System.out.println("Number of violation HPC : " + computeSystems.get(i).getAccumolatedViolation());
                System.out.println("Computing Power Consumed by  " + computeSystems.get(i).getName() + " is: "
                        + computeSystems.get(i).getPower());
                computeSystems.remove(i);
                i--;
            }
        }
        return retValue; // there is no job left in all system
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
    // System.out.println("Uh oh, got an IOException error!" + e.getMessage());
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
    // //System.out.println("Readed inputTime= " + inputTime + " Job Reqested
    // Time=" + j.startTime+" Total job so far="+ total);
    // line = bis.readLine();
    // }
    // } catch (IOException ex) {
    // System.out.println("readJOB EXC readJOB false ");
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
     * 1000); //System.out.println(numberOfidleServer); if(numberOfidleServer<0)
     * return; for(int j=0;j<numberOfidleServer;j++)
     * if(webSet1.ComputeNodeList.get(j).queueLength==0) { suc++;
     * webSet1.ComputeNodeList.get(j).ready=-1;
     * webSet1.ComputeNodeList.get(j).currentCPU=0;
     * webSet1.ComputeNodeList.get(j).Mips=1; } else {System.out.println(
     * "In Coordinator and else   ");numberOfidleServer++;} //border ra jabeja
     * mikonim //if(suc==numberOfidleServer)
     * System.out.println(numberOfidleServer+"\t suc= "+suc); } public void
     * addToresponseArray(double num,int time) { responseTime t= new
     * responseTime(); t.numberOfJob=num; t.responseTime=time;
     * responseArray.add(t); return; }
     */

    public Simulator.Environment getEnvironment() {
        return environment;
    }
}
