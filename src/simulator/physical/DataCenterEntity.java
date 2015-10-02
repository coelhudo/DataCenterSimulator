package simulator.physical;

public abstract class DataCenterEntity {
    private final DataCenterEntityID id;
    
    public DataCenterEntity(DataCenterEntityID id) {
        this.id = id;
    }
    
    public DataCenterEntityID getID() {
        return id;
    }
    
    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
        
        if(!(other instanceof DataCenterEntity)) {
            return false;
        }
        
        return id.equals(((DataCenterEntity)other).id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    public abstract class DataCenterEntityStats {
        public DataCenterEntityID getID() {
            return id;
        }
    }
    
    public abstract DataCenterEntityStats getStats();
}
