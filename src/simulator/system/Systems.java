package simulator.system;

import java.util.ArrayList;
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
        for (EnterpriseSystem enterpriseSystem : enterpriseSystems) {
            if (!enterpriseSystem.isDone()) {
                return false;
            }
        }
        for (InteractiveSystem interactiveSytem : interactiveSystems) {
            if (!interactiveSytem.isDone()) {
                return false;
            }
        }
        for (ComputeSystem computeSystem : computeSystems) {
            if (!computeSystem.isDone()) {
                return false;
            }
        }
        return true;
    }

    public boolean removeJobsThatAreDone() {
        boolean retValue = true;
        for (int i = 0; i < enterpriseSystems.size(); i++) {
            if (!enterpriseSystems.get(i).isDone()) {
                retValue = false;
            } else {
                LOGGER.info("finishing Time EnterSys: " + enterpriseSystems.get(i).getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info("Computing Power Consumed by  " + enterpriseSystems.get(i).getName() + " is: "
                        + enterpriseSystems.get(i).getPower());
                // LOGGER.info("Number of violation:
                // "+ES.get(i).accumolatedViolation);

                enterpriseSystems.remove(i);
                i--;
            }
        }
        for (int i = 0; i < interactiveSystems.size(); i++) {
            if (!interactiveSystems.get(i).isDone()) {
                retValue = false;
            } else {
                LOGGER.info("finishing Time Interactive sys:  " + interactiveSystems.get(i).getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info(
                        "Interactive sys: Number of violation: " + interactiveSystems.get(i).getAccumolatedViolation());
                LOGGER.info("Computing Power Consumed by  " + interactiveSystems.get(i).getName() + " is: "
                        + interactiveSystems.get(i).getPower());
                interactiveSystems.remove(i);
                i--;

                // opps !! hardcoded policy
                // datacenter.getAM().resetBlockTimer();
                notifyObservers(); // FIXME: did this to avoid another
                                   // dependency. After I figure out how
                                   // everything works, them it will be removed
                                   // (i guess)
            }
        }
        for (int i = 0; i < computeSystems.size(); i++) {
            if (!computeSystems.get(i).isDone()) {
                retValue = false; // means still we have work to do
            } else {
                LOGGER.info("finishing Time HPC_Sys:  " + computeSystems.get(i).getName() + " at time: "
                        + environment.getCurrentLocalTime());
                LOGGER.info("Total Response Time= " + computeSystems.get(i).finalized());
                LOGGER.info("Number of violation HPC : " + computeSystems.get(i).getAccumolatedViolation());
                LOGGER.info("Computing Power Consumed by  " + computeSystems.get(i).getName() + " is: "
                        + computeSystems.get(i).getPower());
                computeSystems.remove(i);
                i--;
            }
        }
        return retValue; // there is no job left in all system
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
        for (EnterpriseSystem enterpriseSystem : enterpriseSystems) {
            enterpriseSystem.calculatePower();
        }
        for (ComputeSystem computeSystem : computeSystems) {
            computeSystem.calculatePower();
        }
        for (InteractiveSystem interactiveSystem : interactiveSystems) {
            interactiveSystem.calculatePower();
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
