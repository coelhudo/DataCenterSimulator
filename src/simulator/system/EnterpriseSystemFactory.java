package simulator.system;

import java.util.List;

import simulator.am.ApplicationAM;
import simulator.am.GeneralAM;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public interface EnterpriseSystemFactory {
    EnterpriseSystem create(SystemPOD systemPOD, List<EnterpriseApp> applications);
    EnterpriseApp create(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler, ResourceAllocation resourceAllocation);
    ApplicationAM create(List<EnterpriseApp> applications, GeneralAM am);
}
