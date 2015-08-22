package simulator.system;

import java.util.ArrayList;
import java.util.List;

public class SystemsPOD {

    private List<EnterpriseSystemPOD> enterpriseSystemsPOD = new ArrayList<EnterpriseSystemPOD>();
    private List<ComputeSystemPOD> computeSystemsPOD = new ArrayList<ComputeSystemPOD>();
    private List<InteractiveSystemPOD> interactiveSystemsPOD = new ArrayList<InteractiveSystemPOD>();
    
    public void appendEnterprisePOD(EnterpriseSystemPOD enterpriseSystemPOD) {
        enterpriseSystemsPOD.add(enterpriseSystemPOD);
    }
    
    public void appendComputeSystemPOD(ComputeSystemPOD computeSystemPOD) {
        computeSystemsPOD.add(computeSystemPOD);
    }
    
    public void appendInteractivePOD(InteractiveSystemPOD interactiveSystemPOD) {
        interactiveSystemsPOD.add(interactiveSystemPOD);
    }
        
    public List<EnterpriseSystemPOD> getEnterpriseSystemsPOD() {
        return enterpriseSystemsPOD;
    }
    
    public List<ComputeSystemPOD> getComputeSystemsPOD() {
        return computeSystemsPOD;
    }
    
    public List<InteractiveSystemPOD> getInteractiveSystemsPOD() {
        return interactiveSystemsPOD;
    }
}
