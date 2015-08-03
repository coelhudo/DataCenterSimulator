package simulator;

public class Environment {
    private int localTime = 1;
    private int numberOfMessagesFromDataCenterToSystem = 0;
    private int numberOfMessagesFromSystemToNodes = 0;
    private final int EPOCH_APP = 60;
    
    public int getCurrentLocalTime() {
        return localTime;
    }

    protected void updateCurrentLocalTime() {
        localTime++;
    }

    public void updateNumberOfMessagesFromDataCenterToSystem() {
        numberOfMessagesFromDataCenterToSystem++;
    }

    public int getNumberOfMessagesFromDataCenterToSystem() {
        return numberOfMessagesFromDataCenterToSystem;
    }

    public void updateNumberOfMessagesFromSystemToNodes() {
        numberOfMessagesFromSystemToNodes++;
    }

    public int getNumberOfMessagesFromSystemToNodes() {
        return numberOfMessagesFromSystemToNodes;
    }

    public boolean localTimeByEpoch() {
        return localTime % EPOCH_APP != 0;
    }
}