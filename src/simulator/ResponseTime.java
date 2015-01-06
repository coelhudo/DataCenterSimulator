/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

/**
 *
 * @author fnorouz
 */
public class ResponseTime {

    private double numberOfJob;
    private  int responseTime;

    public double getNumberOfJob() {
        return numberOfJob;
    }

    public void setNumberOfJob(double numberOfJob) {
        this.numberOfJob = numberOfJob;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    ResponseTime() {
        numberOfJob = 0;
        responseTime = 0;
    }
}
