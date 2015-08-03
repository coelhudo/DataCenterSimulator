package simulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

public class SLAViolationLogger {
    private static final Logger LOGGER = Logger.getLogger(SLAViolationLogger.class.getName());

    private OutputStreamWriter SLALogE = null;
    private OutputStreamWriter SLALogI = null;
    private OutputStreamWriter SLALogH = null;
    private Environment environment;

    public SLAViolationLogger(Environment environment) {
        this.environment = environment;

        try {
            SLALogE = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogE.txt")));
            SLALogI = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogI.txt")));
            SLALogH = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogH.txt")));
        } catch (IOException e) {
            LOGGER.warning("Uh oh, got an IOException error!" + e.getMessage());
        }
    }

    public void logHPCViolation(String name, Violation slaViolation) {
        try {
            SLALogH.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void logEnterpriseViolation(String name, int slaViolationNum) {
        try {
            SLALogE.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolationNum + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void logInteractiveViolation(String name, int slaViolation) {
        try {
            SLALogI.write(name + "\t" + environment.getCurrentLocalTime() + "\t" + slaViolation + "\n");
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }
    
    
    public void finish() throws IOException {
        SLALogE.close();
        SLALogH.close();
        SLALogI.close();
    }
}
