package simulator.physical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.logging.Logger;

public class ActivitiesLogger {
    private FileOutputStream fos;
    private OutputStreamWriter oos;

    private static final Logger LOGGER = Logger.getLogger(ActivitiesLogger.class.getName());

    public ActivitiesLogger(String logFileName) {       
        File destinationFile = new File(logFileName);
        try {
            fos = new FileOutputStream(destinationFile);
            oos = new OutputStreamWriter(fos);
        } catch (FileNotFoundException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void close() {
        try {
            oos.close();
            fos.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void write(String string) {
        try {
            oos.write(string);
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }
}
