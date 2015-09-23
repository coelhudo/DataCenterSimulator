package simulator.physical;

/**
 * This class exists to provide an unique id to Rack, Chassis, Server. Each one
 * of these classes is an entity in the system. The main idea was conceived
 * based on DDD ideas. Rack is identified by X.0.0; a Chassis by X.X.0; and
 * Server X.X.X
 */
public final class DataCenterEntityID implements Comparable<DataCenterEntityID> {

    private final int rackID;
    private final int chassisID;
    private final int serverID;

    public static final DataCenterEntityID INVALID_ID = new DataCenterEntityID(-1, -1, -1);

    private DataCenterEntityID(int rackID, int chassisID, int serverID) {
        this.rackID = rackID;
        this.chassisID = chassisID;
        this.serverID = serverID;
    }

    public static DataCenterEntityID createServerID(int rackID, int chassisID, int serverID) {
        final boolean validServerValue = rackID > 0 && chassisID > 0 && serverID > 0;

        if (!validServerValue) {
            throw new RuntimeException(String.format(
                    "Invalid Data Center Entity ID parameters. Received rack %d, chassis %d and server %d", rackID,
                    chassisID, serverID));
        }

        return new DataCenterEntityID(rackID, chassisID, serverID);
    }

    public static DataCenterEntityID createChassisID(int rackID, int chassisID) {
        final boolean validChassisValue = rackID > 0 && chassisID > 0;

        if (!validChassisValue) {
            throw new RuntimeException(String.format(
                    "Invalid Data Center Entity ID parameters. Received rack %d, chassis %d", rackID, chassisID));
        }

        return new DataCenterEntityID(rackID, chassisID, 0);
    }

    public static DataCenterEntityID createRackID(int rackID) {
        final boolean validRackIDValue = rackID > 0;

        if (!validRackIDValue) {
            throw new RuntimeException(
                    String.format("Invalid Data Center Entity ID parameters. Received rack %d", rackID));
        }

        return new DataCenterEntityID(rackID, 0, 0);
    }

    public static DataCenterEntityID toChassis(DataCenterEntityID id) {
        return createChassisID(id.rackID, id.chassisID);
    }

    public static DataCenterEntityID toRack(DataCenterEntityID id) {
        return createChassisID(id.rackID, id.chassisID);
    }

    @Override
    public String toString() {
        return rackID + "." + chassisID + "." + serverID;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DataCenterEntityID)) {
            return false;
        }

        DataCenterEntityID otherID = (DataCenterEntityID) other;
        return rackID == otherID.rackID && chassisID == otherID.chassisID && serverID == otherID.serverID;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + rackID;
        result = 31 * result + chassisID;
        result = 31 * result + serverID;
        return result;
    }

    public int getRackID() {
        return rackID - 1;
    }

    public int getChassisID() {
        return chassisID - 1;
    }

    public int getServerID() {
        return serverID - 1;
    }

    @Override
    public int compareTo(DataCenterEntityID o) {
        if (rackID > o.rackID) {
            return 1;
        }

        if (rackID < o.rackID) {
            return -1;
        }
        
        if (chassisID > o.chassisID) {
            return 1;
        }

        if (chassisID < o.chassisID) {
            return -1;
        }
        
        if (serverID > o.serverID) {
            return 1;
        }

        if (serverID < o.serverID) {
            return -1;
        }

        return 0;
    }
}
