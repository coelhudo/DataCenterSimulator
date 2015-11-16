package simulator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import simulator.physical.DataCenter.DataCenterStats;

public class MainModule extends AbstractModule {

    @Override
    public void configure() {
        bind(Environment.class).to(SimulatorEnvironment.class);
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        bind(SimulatorPOD.class).toInstance(dataCenterBuilder.build());
        BlockingQueue<DataCenterStats> partialResults = new ArrayBlockingQueue<DataCenterStats>(1000);
        bind(new TypeLiteral<BlockingQueue<DataCenterStats>>(){}).toInstance(partialResults);
    }
}
