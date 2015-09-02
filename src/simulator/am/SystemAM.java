package simulator.am;

import simulator.Environment;
import simulator.system.GeneralSystem;

public abstract class SystemAM extends GeneralAM {

    public SystemAM(Environment environment) {
        super(environment);
    }

    public abstract void setManagedSystem(GeneralSystem managedSystem);
}
