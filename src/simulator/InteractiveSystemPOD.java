package simulator;

public class InteractiveSystemPOD extends SystemPOD {
    private int numberofIdleNode = 0; // idle is change in allocation function

    public void setNumberofIdleNode(int numberofNode) {
        this.numberofIdleNode = numberofNode;
    }
    
    public int getNumberofIdleNode() {
        return numberofIdleNode;
    }
}
