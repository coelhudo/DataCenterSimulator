package simulator.system;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;

import simulator.physical.DataCenterEntityID;

public abstract class SystemPOD {

    private Set<DataCenterEntityID> rackUIDs = new HashSet<DataCenterEntityID>();
    private BufferedReader bis = null;
    private int numberOfNode;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumberofNode(int n) {
        numberOfNode = n;
    }

    public int getNumberOfNode() {
        return numberOfNode;
    }

    public void appendRackID(DataCenterEntityID rackID) {
        rackUIDs.add(rackID);
    }
    
    public Set<DataCenterEntityID> getRackUIDs() {
        return rackUIDs;
    }

    public BufferedReader getBis() {
        return bis;
    }

    public void setBis(BufferedReader bis) {
        this.bis = bis;
    }
}
