package simulator.am;

import simulator.Simulator;

public abstract class GeneralAM {

    private double[] compPwrApps = new double[256];
    protected double[] SlaApps = new double[256];
    private int[] recForCoop;
    private int SLAViolationGen;
    private Simulator.StrategyEnum strategy;

    public abstract void monitor();

    public abstract void analysis(Object vilation);

    public abstract void planning();

    public abstract void execution();

    public int getRecForCoopAt(int index) {
        return recForCoop[index];
    }
    
    public void setRecForCoopAt(int index, int value) {
        this.recForCoop[index] = value;
    }
    
    public void setRecForCoop(int[] setRecForCoop) {
        this.recForCoop = setRecForCoop;
    }

    public double getCompPowerAppsAt(int index) {
        return compPwrApps[index];
    }
    
    public void setCompPowerAppsAt(int index, double value) {
        this.compPwrApps[index] = value;
    }

    protected int getSLAViolationGen() {
        return SLAViolationGen;
    }

    protected void setSLAViolationGen(int sLAViolationGen) {
        SLAViolationGen = sLAViolationGen;
    }

    public Simulator.StrategyEnum getStrategy() {
        return strategy;
    }

    public void setStrategy(Simulator.StrategyEnum strategy) {
        this.strategy = strategy;
    }
}
