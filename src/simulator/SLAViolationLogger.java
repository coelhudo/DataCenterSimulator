package simulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

public class SLAViolationLogger {
    private static final Logger LOGGER = Logger.getLogger(SLAViolationLogger.class.getName());

    private OutputStreamWriter entepriseSLALoggerViolation = null;
    private OutputStreamWriter interactiveSLALoggerViolation = null;
    private OutputStreamWriter computeSLALoggerViolation = null;
    private Environment environment;

    public SLAViolationLogger(Environment environment) {
        this.environment = environment;

        try {
            entepriseSLALoggerViolation = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogE.txt")));
            interactiveSLALoggerViolation = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogI.txt")));
            computeSLALoggerViolation = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogH.txt")));
        } catch (IOException e) {
            LOGGER.warning("Uh oh, got an IOException error!" + e.getMessage());
        }
    }

    public void logHPCViolation(String name, Violation slaViolation) {
        try {
            computeSLALoggerViolation.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void logEnterpriseViolation(String name, int slaViolationNum) {
        try {
            entepriseSLALoggerViolation.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolationNum + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void logInteractiveViolation(String name, int slaViolation) {
        try {
            interactiveSLALoggerViolation.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }
    
    
    public void finish() throws IOException {
        entepriseSLALoggerViolation.close();
        computeSLALoggerViolation.close();
        interactiveSLALoggerViolation.close();
    }
}
