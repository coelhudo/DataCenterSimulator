package simulator.system;

import java.util.List;

import simulator.schedulers.Scheduler;
import simulator.ra.ResourceAllocation;

public interface EnterpriseSystemFactory {
    EnterpriseSystem create(EnterpriseSystemPOD enterpriseSystemPOD, List<EnterpriseApp> applications, Scheduler scheduler, ResourceAllocation resourceAllocation);
}
