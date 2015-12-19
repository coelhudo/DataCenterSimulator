package simulator;

public class SimulatorOptions {

	private boolean isAutonomicManagerEnabled = false;
	
	public void enableAutonomicManager() {
		isAutonomicManagerEnabled = true;
	}
	
	public boolean isAutonomicManagerEnabled() {
		return isAutonomicManagerEnabled;
	}
}
