package simulator;

import com.google.inject.AbstractModule;

public class EnvironmentModule extends AbstractModule {

    @Override
    public void configure() {
	bind(Simulator.Environment.class);
    }
}
