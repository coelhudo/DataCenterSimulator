package simulator.am;

import simulator.Environment;
import simulator.system.GeneralSystem;

public abstract class SystemAM extends GeneralAM {

    private GeneralSystem managedSystem;
    
    public SystemAM(Environment environment) {
        super(environment);
    }

    public GeneralSystem getManagedSystem() {
        return managedSystem;
    }

    public void setManagedSystem(GeneralSystem managedSystem) {
        this.managedSystem = managedSystem;
    }
}
