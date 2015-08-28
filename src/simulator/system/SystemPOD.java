package simulator.system;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import simulator.jobs.JobProducer;

public abstract class SystemPOD {

    private List<Integer> rackIDs = new ArrayList<Integer>();
    private BufferedReader bis = null;
    private int numberOfNode;
    private String name; 
    private JobProducer jobProducer;
    
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
    
    public void setJobProducer(JobProducer jobProducer) {
        this.jobProducer = jobProducer;
    }
    
    public JobProducer getJobProducer() {
        return jobProducer;
    }
}
