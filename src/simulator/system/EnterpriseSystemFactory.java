package simulator.system;

import java.util.List;

public interface EnterpriseSystemFactory {
    EnterpriseSystem create(SystemPOD systemPOD, List<EnterpriseApp> applications);
}
