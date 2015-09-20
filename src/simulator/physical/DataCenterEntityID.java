package simulator.physical;

/**
 * This class exists to provide an unique id to Rack, Chassis, Server. Each one
 * of these classes is an entity in the system. The main idea was conceived
 * based on DDD ideas. Rack is identified by X.0.0; a Chassis by X.X.0; and
 * Server X.X.X
 */
public class DataCenterEntityID implements Comparable<DataCenterEntityID> {

    private final int rackID;
    private final int chassisID;
    private final int serverID;

    private DataCenterEntityID(int rackID, int chassisID, int serverID) {
        this.rackID = rackID;
        this.chassisID = chassisID;
        this.serverID = serverID;
    }

    @Override
    public String toString() {
        return rackID + "." + chassisID + "." + serverID;
    }

    @Override
    public int compareTo(DataCenterEntityID other) {
        if (rackID > other.rackID)
            return 1;
        if (rackID < other.rackID)
            return -1;
        if (chassisID > other.chassisID)
            return 1;
        if (chassisID < other.chassisID)
            return -1;
        if (serverID > other.serverID)
            return 1;
        if (serverID < other.serverID)
            return -1;
        return 0;
    }

    public static DataCenterEntityID create(int rackID, int chassisID, int serverID) {
        final boolean validRackIDValue = rackID > 0 && chassisID == 0 && serverID == 0;
        final boolean validChassisValue = rackID > 0 && chassisID > 0 && serverID == 0;
        final boolean validServerValue = rackID > 0 && chassisID > 0 && serverID > 0;
        
        if (!(validRackIDValue || validChassisValue || validServerValue)) {
            throw new RuntimeException("Invalid Data Center Entity ID creating. Only values >= 1 are accepted"
                    + "for rackID or >= 1 for chassis ID and server ID.");
        }

        return new DataCenterEntityID(rackID, chassisID, serverID);
    }
}
