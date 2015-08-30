package simulator.system;

public class InteractiveSystemPOD extends SystemPOD {
    private int numberofIdleNode = 0;

    public void setNumberofIdleNode(int numberofNode) {
        this.numberofIdleNode = numberofNode;
    }
    
    public int getNumberofIdleNode() {
        return numberofIdleNode;
    }
}
