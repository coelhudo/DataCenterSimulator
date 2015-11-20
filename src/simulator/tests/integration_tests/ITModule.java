package simulator.tests.integration_tests;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.SimulatorBuilder;
import simulator.SimulatorEnvironment;
import simulator.SimulatorPOD;
import simulator.physical.DataCenter.DataCenterStats;

public class ITModule extends AbstractModule {
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

    @Override
    protected void configure() {
        bind(Environment.class).to(SimulatorEnvironment.class);
        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        bind(SimulatorPOD.class).toInstance(dataCenterBuilder.build());
        BlockingQueue<DataCenterStats> partialResults = new SkeletonBlockingQueue<DataCenterStats>();
        bind(new TypeLiteral<BlockingQueue<DataCenterStats>>() {
        }).toInstance(partialResults);
        bind(SLAViolationLogger.class);
    }

}
