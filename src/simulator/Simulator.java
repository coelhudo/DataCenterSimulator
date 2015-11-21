package simulator;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.system.Systems;

public class Simulator implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    // private int epochSys = 120, epochSideApp = 120;
    private DataCenter dataCenter;
    private Environment environment;
    private Systems systems;
    private BlockingQueue<DataCenterStats> partialResults;

    @Inject
    public Simulator(Environment environment, BlockingQueue<DataCenterStats> partialResults, DataCenter dataCenter,
            Systems systems) {
        this.environment = environment;
        this.partialResults = partialResults;
        this.dataCenter = dataCenter;
        this.systems = systems;
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
