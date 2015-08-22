package simulator;

import simulator.physical.DataCenter;
import simulator.system.Systems;

public class SimulatorPOD {
    private Systems systems;
    private DataCenter dataCenter;
    
    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }
    
    public DataCenter getDataCenter() {
        return dataCenter;
    }
    
    public void setSystems(Systems systems) {
        this.systems = systems;
    }

    public Systems getSystems() {
        return systems;
    }

}
