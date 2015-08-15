package simulator.physical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.am.DataCenterAM;
import simulator.system.Systems;

public class DataCenter {

    private static final Logger LOGGER = Logger.getLogger(DataCenter.class.getName());

    private int overRed = 0;
    private double totalPowerConsumption = 0;
    private Cooler cooler1 = new Cooler();
    private List<Chassis> chassisSet;
    private int redTemperature;
    private FileOutputStream fos;
    private OutputStreamWriter oos;
    private double[][] D;
    private DataCenterAM am;

    private Environment environment;

    public DataCenter(DataCenterPOD dataCenterPOD, Environment environment, Systems systems) {
        // output file for writing total DC power consumption
        am = new DataCenterAM(environment, systems);
        String s = "out_W.txt";
        File destinationFile = new File(s);
        try {
            fos = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException ex) {
            LOGGER.severe(ex.getMessage());
        }
        this.environment = environment;
        oos = new OutputStreamWriter(fos);
        // reading config file to set the parameters
        chassisSet = dataCenterPOD.getChassis();
        redTemperature = dataCenterPOD.getRedTemperature();
        D = dataCenterPOD.getD();
    }

    private int getServerIndex(int i) {
        return i % chassisSet.get(0).getServers().size();
    }

    private int getChasisIndex(int i) {
        return i / chassisSet.get(0).getServers().size();
    }

    public void calculatePower() {
        int m = chassisSet.size();
        double computingPower = 0;
        double[] temprature = new double[m];
        for (int i = 0; i < m; i++) {
            double temp = chassisSet.get(i).power();
            try {
                oos.write((int) temp + "\t");
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
            computingPower = computingPower + temp;
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temprature[i] = temprature[i] + D[i][j] * chassisSet.get(j).power();
            }
        }
        
        double maxTemp = temprature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temprature[i]) {
                maxTemp = temprature[i];
            }
        }

        maxTemp = redTemperature - maxTemp;

        if (maxTemp <= 0) {
            am.setSlowDownFromCooler(true);
            overRed++;

        } else {
            am.setSlowDownFromCooler(false);
        }

        final double cop = cooler1.getCOP(maxTemp);

        try {
            oos.write(((int) (computingPower * (1 + 1.0 / cop))) + "\t" + (int) computingPower + "\t"
                    + environment.getCurrentLocalTime() + "\n");
            totalPowerConsumption = totalPowerConsumption + computingPower * (1 + 1.0 / cop);
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void shutDownDC() throws FileNotFoundException, IOException {
        oos.close();
        fos.close();
    }

    public DataCenterAM getAM() {
        return am;
    }

    public int getOverRed() {
        return overRed;
    }

    public BladeServer getServer(int i) {
        return chassisSet.get(getChasisIndex(i)).getServers().get(getServerIndex(i));
    }

    public BladeServer getServer(int indexChassis, int indexServer) {
        return chassisSet.get(indexChassis).getServers().get(indexServer);
    }

    public List<Chassis> getChassisSet() {
        return chassisSet;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }
}
