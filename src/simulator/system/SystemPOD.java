package simulator.system;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public abstract class SystemPOD {

    private List<Integer> rackIDs = new ArrayList<Integer>();
    private BufferedReader bis = null;
    private int numberOfNode;
    
    public void setNumberofNode(int n) {
        numberOfNode = n;
    }
    
    public int getNumberOfNode() {
        return numberOfNode;
    }
    
    public void appendRackID(Integer rackID) {
        rackIDs.add(rackID);
    }

    public BufferedReader getBis() {
        return bis;
    }

    public void setBis(BufferedReader bis) {
        this.bis = bis;
    }
    
    public List<Integer> getRackIDs() {
        return rackIDs;
    }
}
