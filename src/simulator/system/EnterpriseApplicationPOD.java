package simulator.system;

import java.io.BufferedReader;

public class EnterpriseApplicationPOD {
    
    private int id = 0;
    private int maxProc = 0;
    private int minProc = 0;
    private int timeTreshold = 0;
    private int SLAPercentage;
    private int MaxNumberOfRequest = 0; // # of Request can be handled by number
    private int numberofBasicNode = 0;
    private int maxExpectedResTime = 0;
    private BufferedReader bis = null;

    public int getMaxProc() {
        return maxProc;
    }

    void setMaxProc(int maxProc) {
        this.maxProc = maxProc;
    }

    int getMinProc() {
        return minProc;
    }

    void setMinProc(int minProc) {
        this.minProc = minProc;
    }

    public int getTimeTreshold() {
        return timeTreshold;
    }

    void setTimeTreshold(int timeTreshold) {
        this.timeTreshold = timeTreshold;
    }

    public int getSLAPercentage() {
        return SLAPercentage;
    }

    void setSLAPercentage(int sLAPercentage) {
        SLAPercentage = sLAPercentage;
    }

    public int getMaxNumberOfRequest() {
        return MaxNumberOfRequest;
    }

    void setMaxNumberOfRequest(int maxNumberOfRequest) {
        MaxNumberOfRequest = maxNumberOfRequest;
    }

    public int getID() {
        return id;
    }
    
    void setID(int id) {
        this.id = id;
    }

    public int getNumberofBasicNode() {
        return numberofBasicNode;
    }

    public void setNumberofBasicNode(int numberofBasicNode) {
        this.numberofBasicNode = numberofBasicNode;
    }

    public int getMaxExpectedResTime() {
        return maxExpectedResTime;
    }

    void setMaxExpectedResTime(int maxExpectedResTime) {
        this.maxExpectedResTime = maxExpectedResTime;
    }

    public BufferedReader getBIS() {
        return bis;
    }

    public void setBIS(BufferedReader bis) {
        this.bis = bis;
    }
}
