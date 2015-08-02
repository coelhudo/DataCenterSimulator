package simulator.am;

import simulator.Simulator;

public abstract class GeneralAM {

    // FIXME: find what this means and rename it
    private double[] compPwrApps = new double[256];
    protected double[] SlaApps = new double[256];
    // FIXME: find what this means and rename it
    private int[] recForCoop;
    private int SLAViolationGen;
    Simulator.StrategyEnum strategy;

    public abstract void monitor();

    public abstract void analysis(Object vilation);

    public abstract void planning();

    public abstract void execution();

    public int[] getRecForCoop() {
        return recForCoop;
    }

    public void setRecForCoop(int[] recForCoop) {
        this.recForCoop = recForCoop;
    }

    public double[] getCompPwrApps() {
        return compPwrApps;
    }

    public void setCompPwrApps(double[] compPwrApps) {
        this.compPwrApps = compPwrApps;
    }

    protected int getSLAViolationGen() {
        return SLAViolationGen;
    }

    protected void setSLAViolationGen(int sLAViolationGen) {
        SLAViolationGen = sLAViolationGen;
    }
}
