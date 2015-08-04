package simulator.system;

import java.util.ArrayList;
import java.util.List;

public class EnterpriseSystemPOD extends SystemPOD {
    private List<EnterpriseApplicationPOD> applicationPODs = new ArrayList<EnterpriseApplicationPOD>();

    public List<EnterpriseApplicationPOD> getApplicationPODs() {
        return applicationPODs;
    }
    
    public void appendEnterpriseApplicationPOD(EnterpriseApplicationPOD enterpriseApplicationPOD) {
        applicationPODs.add(enterpriseApplicationPOD);
    }

}
