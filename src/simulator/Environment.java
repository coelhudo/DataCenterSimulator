package simulator;

public interface Environment {
    int getCurrentLocalTime();

    void updateCurrentLocalTime();

    void updateNumberOfMessagesFromDataCenterToSystem();

    int getNumberOfMessagesFromDataCenterToSystem();

    void updateNumberOfMessagesFromSystemToNodes();

    int getNumberOfMessagesFromSystemToNodes();

    boolean localTimeByEpoch();
}