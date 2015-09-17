package simulator.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

import simulator.Environment;

public class Systems extends Observable {

    private static final Logger LOGGER = Logger.getLogger(Systems.class.getName());

    private List<InteractiveSystem> interactiveSystems = new ArrayList<InteractiveSystem>();
    private List<EnterpriseSystem> enterpriseSystems = new ArrayList<EnterpriseSystem>();
    private List<ComputeSystem> computeSystems = new ArrayList<ComputeSystem>();

    private Environment environment;

    public Systems(Environment environment) {
        this.environment = environment;
    }

    public List<InteractiveSystem> getInteractiveSystems() {
        return interactiveSystems;
    }

    public void setInteractiveSystems(List<InteractiveSystem> interactiveSystems) {
        this.interactiveSystems = interactiveSystems;
    }

    public List<EnterpriseSystem> getEnterpriseSystems() {
        return enterpriseSystems;
    }

    public void setEnterpriseSystems(List<EnterpriseSystem> enterpriseSystems) {
        this.enterpriseSystems = enterpriseSystems;
    }

    public List<ComputeSystem> getComputeSystems() {
        return computeSystems;
    }

    public void setComputeSystems(List<ComputeSystem> computeSystems) {
        this.computeSystems = computeSystems;
    }

    public boolean allJobsDone() {
        return allJobsDone(enterpriseSystems) && allJobsDone(interactiveSystems) && allJobsDone(computeSystems);
    }

    private boolean allJobsDone(List<? extends GeneralSystem> systems) {
        for (GeneralSystem system : systems) {
            if (!system.isDone()) {
                return false;
            }
        }

        return true;
    }

    public boolean removeJobsThatAreDone() {
        boolean retValue = true;
        Iterator<EnterpriseSystem> it = enterpriseSystems.iterator();
        while (it.hasNext()) {
            EnterpriseSystem current = it.next();
            if (!current.isDone()) {
                retValue = false;
            } else {
                LOGGER.info("finishing Time EnterSys: " + current.getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info("Computing Power Consumed by  " + current.getName() + " is: " + current.getPower());
                it.remove();
            }
        }

        Iterator<InteractiveSystem> interactiveIt = interactiveSystems.iterator();
        while (it.hasNext()) {
            InteractiveSystem current = interactiveIt.next();
            if (!current.isDone()) {
                retValue = false;
            } else {
                LOGGER.info("finishing Time Interactive sys:  " + current.getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info("Interactive sys: Number of violation: " + current.getAccumolatedViolation());
                LOGGER.info("Computing Power Consumed by  " + current.getName() + " is: " + current.getPower());
                interactiveIt.remove();

                // opps !! hardcoded policy
                // datacenter.getAM().resetBlockTimer();
                // FIXME: did this to avoid another
                // dependency. After I figure out how
                // everything works, them it will be removed
                // (i guess)
                notifyObservers();
            }
        }

        Iterator<ComputeSystem> computeIt = computeSystems.iterator();
        while (computeIt.hasNext()) {
            ComputeSystem current = computeIt.next();
            if (!current.isDone()) {
                retValue = false;
            } else {
                LOGGER.info("finishing Time HPC_Sys:  " + current.getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info("Total Response Time= " + current.finalized());
                LOGGER.info("Number of violation HPC : " + current.getAccumolatedViolation());
                LOGGER.info("Computing Power Consumed by  " + current.getName() + " is: " + current.getPower());
                computeIt.remove();
            }
        }

        return retValue;
    }

    public void runACycle() {
        runACycle(enterpriseSystems);
        runACycle(computeSystems);
        runACycle(interactiveSystems);
    }

    private void runACycle(List<? extends GeneralSystem> systems) {
        for (GeneralSystem system : systems) {
            if (!system.isDone()) {
                system.runAcycle();
            }
        }
    }

    public void calculatePower() {
        calculatePower(enterpriseSystems);
        calculatePower(computeSystems);
        calculatePower(interactiveSystems);
    }

    private void calculatePower(List<? extends GeneralSystem> systems) {
        for (GeneralSystem system : systems) {
            system.calculatePower();
        }
    }

    public void logTotalResponseTimeComputeSystem() {
        for (int i = 0; i < computeSystems.size(); i++) {
            LOGGER.info("Total Response Time in CS " + i + "th CS = " + computeSystems.get(i).finalized());
        }
    }

    public void addEnterpriseSystem(EnterpriseSystem eS1) {
        this.enterpriseSystems.add(eS1);
    }

    public void addInteractiveSystem(InteractiveSystem wb1) {
        this.interactiveSystems.add(wb1);

    }

    public void addComputeSystem(ComputeSystem cP) {
        this.computeSystems.add(cP);
    }

}
