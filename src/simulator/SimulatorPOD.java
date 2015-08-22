package simulator;

import simulator.physical.DataCenterPOD;
import simulator.system.SystemsPOD;

public class SimulatorPOD {
    private SystemsPOD systemsPOD;
    private DataCenterPOD dataCenterPOD;
    
    public void setDataCenterPOD(DataCenterPOD dataCenter) {
        this.dataCenterPOD = dataCenter;
    }
    
    public DataCenterPOD getDataCenterPOD() {
        return dataCenterPOD;
    }
    
    public void setSystemsPOD(SystemsPOD systemsPOD) {
        this.systemsPOD = systemsPOD;
    }

    public SystemsPOD getSystemsPOD() {
        return systemsPOD;
    }

}
