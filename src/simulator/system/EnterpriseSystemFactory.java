package simulator.system;

import java.util.List;

import simulator.am.AutonomicManager;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public interface EnterpriseSystemFactory {
    EnterpriseSystem create(SystemPOD systemPOD, List<EnterpriseApp> applications);
    EnterpriseApp create(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler, ResourceAllocation resourceAllocation);
    AutonomicManager create(List<EnterpriseApp> applications, AutonomicManager am);
}
