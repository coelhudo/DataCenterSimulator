package simulator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.system.SystemsPOD;
import simulator.physical.DataCenterPOD;
import simulator.utils.ActivitiesLogger;

public class MainModule extends AbstractModule {

    @Override
    public void configure() {
        bind(Environment.class).to(SimulatorEnvironment.class);
        
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        SimulatorPOD simulatorPOD = dataCenterBuilder.build();
        bind(SimulatorPOD.class).toInstance(simulatorPOD);
        bind(DataCenterPOD.class).toInstance(simulatorPOD.getDataCenterPOD());
        bind(SystemsPOD.class).toInstance(simulatorPOD.getSystemsPOD());
        
        BlockingQueue<DataCenterStats> partialResults = new ArrayBlockingQueue<DataCenterStats>(1000);
        bind(new TypeLiteral<BlockingQueue<DataCenterStats>>(){}).toInstance(partialResults);
        bind(SLAViolationLogger.class);
        bind(DataCenter.class);
        bind(ActivitiesLogger.class);
        bind(String.class).annotatedWith(Names.named("ActivitiesLoggerParameter")).toInstance("out_W.txt");
    }
}
