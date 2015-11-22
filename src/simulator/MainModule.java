package simulator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.physical.DataCenterPOD;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.LeastRemainFirstScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystemFactory;
import simulator.system.InteractiveSystemFactory;
import simulator.system.SystemsPOD;
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
        bind(new TypeLiteral<BlockingQueue<DataCenterStats>>() {
        }).toInstance(partialResults);
        bind(SLAViolationLogger.class);
        bind(DataCenter.class);
        bind(ActivitiesLogger.class);
        bind(String.class).annotatedWith(Names.named("ActivitiesLoggerParameter")).toInstance("out_W.txt");

        bind(Scheduler.class).annotatedWith(Names.named("ComputeSystem")).to(LeastRemainFirstScheduler.class);
        bind(ResourceAllocation.class).annotatedWith(Names.named("ComputeSystem")).to(MHR.class);
        
        bind(Scheduler.class).annotatedWith(Names.named("InteractiveSystem")).to(FIFOScheduler.class);
        bind(ResourceAllocation.class).annotatedWith(Names.named("InteractiveSystem")).to(MHR.class);
        
        //bind(Scheduler.class).annotatedWith(Names.named("EnterpriseSystem")).to(FIFOScheduler.class);
        //bind(ResourceAllocation.class).annotatedWith(Names.named("EnterpriseSystem")).to(MHR.class);
        
        install(new FactoryModuleBuilder().build(ComputeSystemFactory.class));
        install(new FactoryModuleBuilder().build(InteractiveSystemFactory.class));
        //install(new FactoryModuleBuilder().build(EnterpriseSystemFactory.class));

        //install(new FactoryModuleBuilder().build(EnterpriseApplicationFactory.class));
    }
}
