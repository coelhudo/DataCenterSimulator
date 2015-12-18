package simulator.am;

import com.google.inject.Inject;

import simulator.Environment;
import simulator.ManagedResource;

public class DummyAM extends GeneralAM {

	@Inject
	public DummyAM(Environment environment) {
		super(environment);
	}

	@Override
	public void monitor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void analysis() {
		// TODO Auto-generated method stub

	}

	@Override
	public void planning() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execution() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setManagedResource(ManagedResource mananagedResource) {
		// TODO Auto-generated method stub

	}

}
