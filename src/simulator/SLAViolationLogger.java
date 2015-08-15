package simulator;

import simulator.utils.ActivitiesLogger;

public class SLAViolationLogger {
    private ActivitiesLogger entepriseSLALoggerViolation = null;
    private ActivitiesLogger interactiveSLALoggerViolation = null;
    private ActivitiesLogger computeSLALoggerViolation = null;
    private Environment environment;

    public SLAViolationLogger(Environment environment) {
        this.environment = environment;

        entepriseSLALoggerViolation = new ActivitiesLogger("slaViolLogE.txt");
        interactiveSLALoggerViolation = new ActivitiesLogger("slaViolLogI.txt");
        computeSLALoggerViolation = new ActivitiesLogger("slaViolLogH.txt");
    }

    public void logHPCViolation(String name, Violation slaViolation) {
        computeSLALoggerViolation.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
    }

    public void logEnterpriseViolation(String name, int slaViolationNum) {
        entepriseSLALoggerViolation
                .write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolationNum + "\n");
    }

    public void logInteractiveViolation(String name, int slaViolation) {

        interactiveSLALoggerViolation
                .write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
    }

    public void finish() {
        entepriseSLALoggerViolation.close();
        computeSLALoggerViolation.close();
        interactiveSLALoggerViolation.close();
    }
}
