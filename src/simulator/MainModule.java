package simulator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import simulator.am.AutonomicManager;
import simulator.am.ComputeSystemAM;
import simulator.am.EnterpriseSystemAM;
import simulator.am.InteractiveSystemAM;
import simulator.physical.DataCenter;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.physical.DataCenterPOD;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.LeastRemainFirstScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystemFactory;
import simulator.system.EnterpriseSystemFactory;
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
        bind(AutonomicManager.class).annotatedWith(Names.named("ComputeSystem")).to(ComputeSystemAM.class);

        bind(Scheduler.class).annotatedWith(Names.named("InteractiveSystem")).to(FIFOScheduler.class);
        bind(ResourceAllocation.class).annotatedWith(Names.named("InteractiveSystem")).to(MHR.class);
        bind(AutonomicManager.class).annotatedWith(Names.named("InteractiveSystem")).to(InteractiveSystemAM.class);

        bind(Scheduler.class).annotatedWith(Names.named("EnterpriseSystem")).to(FIFOScheduler.class);
        bind(ResourceAllocation.class).annotatedWith(Names.named("EnterpriseSystem")).to(MHR.class);
        bind(AutonomicManager.class).annotatedWith(Names.named("EnterpriseSystem")).to(EnterpriseSystemAM.class);

        install(new FactoryModuleBuilder().build(ComputeSystemFactory.class));
        install(new FactoryModuleBuilder().build(InteractiveSystemFactory.class));
        //No need to .implement(Class.class), using default values
        install(new FactoryModuleBuilder().build(EnterpriseSystemFactory.class));
    }
}
