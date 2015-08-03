package simulator.physical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.Systems;
import simulator.am.DataCenterAM;

public final class DataCenter {

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
    public DataCenter(DataCenterBuilder builder, Environment environment, Systems systems) {
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
        chassisSet = builder.getChassis();
        redTemperature = builder.getRedTemperature();
        D = builder.getD();
    }

    int getServerIndex(int i) {
        return i % chassisSet.get(0).getServers().size();
    }

    int getChasisIndex(int i) {
        return i / chassisSet.get(0).getServers().size();
    }

    public void calculatePower() {
        int m = chassisSet.size();
        double computingPower = 0;
        double[] temprature = new double[m];
        double maxTemp = 0;
        for (int i = 0; i < m; i++) {
            double temp = chassisSet.get(i).power();
            // if(chassisSet.get(i).getServers().get(0).currentCPU!=0)
            // LOGGER.info(chassisSet.get(i).servers.get(0).currentCPU +"
            // \ttime ="+Main.localTime +" \t chassi
            // id="+chassisSet.get(i).chassisID );

            try {
                oos.write((int) temp + "\t");
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
            computingPower = computingPower + temp;
        }
        // LOGGER.info("in betweeeeeen");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temprature[i] = temprature[i] + D[i][j] * chassisSet.get(j).power();
                // LOGGER.info(chassis_list[i]);
            }
        }
        maxTemp = temprature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temprature[i]) {
                maxTemp = temprature[i];
            }
        }
        // LOGGER.info(maxTepm);
        maxTemp = redTemperature - maxTemp;
        if (maxTemp <= 0) {
            // LOGGER.info("maxTem less than 0000 " + maxTemp);
            am.setSlowDownFromCooler(true);
            overRed++;

        } else {
            am.setSlowDownFromCooler(false);
        }
        double cop = cooler1.getCOP(maxTemp);
        try {
            // LOGGER.info(((int)(Pcomp*(1+1.0/COP)))+"\t"+(int)Pcomp+"\t"+localTime);
            // oos.write(Integer.toString((int)
            // (Pcomp*(1+1.0/COP)))+"\t"+Integer.toString((int)Pcomp)+"\t"+localTime+"\t"+perc[0]+"\t"+perc[1]+"\t"+perc[2]+"\n");
            oos.write(((int) (computingPower * (1 + 1.0 / cop))) + "\t" + (int) computingPower + "\t"
                    + environment.getCurrentLocalTime() + "\n");
            totalPowerConsumption = totalPowerConsumption + computingPower * (1 + 1.0 / cop);
            // LOGGER.info(totalPowerConsumption);
        } catch (IOException ex) {
            Logger.getLogger(Package.class.getName()).log(Level.SEVERE, null, ex);
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

    public void setChassisSet(List<Chassis> chassisSet) {
        this.chassisSet = chassisSet;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }
}
