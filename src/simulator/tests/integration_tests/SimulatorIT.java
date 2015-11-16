package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import simulator.Environment;
import simulator.SimulationResults;
import simulator.Simulator;
import simulator.SimulatorBuilder;
import simulator.SimulatorEnvironment;
import simulator.SimulatorPOD;
import simulator.physical.DataCenter.DataCenterStats;

public class SimulatorIT {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        SimulatorPOD simulatorPOD = dataCenterBuilder.build();

        Environment environment = new SimulatorEnvironment();

        class SkeletonBlockingQueue<E> implements BlockingQueue<E> {

            @Override
            public E remove() {
                return null;
            }

            @Override
            public E poll() {
                return null;
            }

            @Override
            public E element() {
                return null;
            }

            @Override
            public E peek() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public Iterator<E> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return null;
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public boolean add(E e) {
                return false;
            }

            @Override
            public boolean offer(E e) {
                return false;
            }

            @Override
            public void put(E e) throws InterruptedException {

            }

            @Override
            public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            @Override
            public E take() throws InterruptedException {
                return null;
            }

            @Override
            public E poll(long timeout, TimeUnit unit) throws InterruptedException {
                return null;
            }

            @Override
            public int remainingCapacity() {
                return 0;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public int drainTo(Collection<? super E> c) {
                return 0;
            }

            @Override
            public int drainTo(Collection<? super E> c, int maxElements) {
                return 0;
            }

        }

        BlockingQueue<DataCenterStats> partialResults = new SkeletonBlockingQueue<DataCenterStats>();
        Simulator simulator = new Simulator(simulatorPOD, environment, partialResults);

        simulator.run();
        SimulationResults results = new SimulationResults(simulator);
        final double expectedTotalPowerConsumption = 7.555028576875995E9;
        assertEquals(expectedTotalPowerConsumption, results.getTotalPowerConsumption(), 1.0E-5);
        final double expectedLocalTime = 686293.0;
        assertEquals(expectedLocalTime, results.getLocalTime(), 0.01);
        final double meanPowerConsumption = 11008.459326;
        assertEquals(meanPowerConsumption, results.getMeanPowerConsumption(), 1.0E5);
        final int expectedOverRedTemperatureNumber = 0;
        assertEquals(expectedOverRedTemperatureNumber, results.getOverRedTemperatureNumber());
        final int expectedNumberMessagesFromDataCenterToSystem = 11508;
        assertEquals(expectedNumberMessagesFromDataCenterToSystem, results.getNumberOfMessagesFromDataCenterToSystem());
        final int expectedNumberMessagesFromSystemToNodes = 198253;
        assertEquals(expectedNumberMessagesFromSystemToNodes, results.getNumberOfMessagesFromSystemToNodes());
    }

}
