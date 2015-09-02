package simulator.am;

import simulator.system.GeneralSystem;

public abstract class SystemAM extends GeneralAM {

    private GeneralSystem managedSystem;

    public GeneralSystem getManagedSystem() {
        return managedSystem;
    }

    public void setManagedSystem(GeneralSystem managedSystem) {
        this.managedSystem = managedSystem;
    }
}
