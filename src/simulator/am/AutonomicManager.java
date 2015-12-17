package simulator.am;

import simulator.ManagedResource;

public interface AutonomicManager {

    void monitor();

    void analysis();

    void planning();

    void execution();
    
    void setManagedResource(ManagedResource mananagedResource);
}
