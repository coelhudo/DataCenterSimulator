package simulator.physical;

public class DataCenterEntity {
    private final DataCenterEntityID id;
    
    public DataCenterEntity(DataCenterEntityID id) {
        this.id = id;
    }
    
    public DataCenterEntityID getID() {
        return id;
    }
}
