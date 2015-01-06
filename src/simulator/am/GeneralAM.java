package simulator.am;

import simulator.Simulator;

abstract public class GeneralAM {

    public double[] compPwrApps = new double[256];
    double[] SlaApps = new double[256];
    public int[] recForCoop;
    int SLAViolationGen;
    Simulator.StrategyEnum strategy;

    abstract public void monitor();

    abstract public void analysis(Object vilation);

    abstract public void planning();

    abstract public void execution();
}
