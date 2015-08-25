package simulator.tests.integration_tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers.*;

import simulator.*;
import simulator.physical.*;
import simulator.utils.ActivitiesLogger;

import simulator.system.*;

public class ComputeSystemIT {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };

    @Test
    public void testBladeWithZeroBatchJob() {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setChassisID(0);
        bladeServerPOD.setRackID(0);
        bladeServerPOD.setServerID(0);
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setIdleConsumption(5);

        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        chassisPOD.setBladeType("DummyType");
        chassisPOD.setChassisType("DummyChassisType");
        chassisPOD.setID(0);
        chassisPOD.setRackID(0);

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.setD(0, 0, 100);

        Environment mockedEnvironment = mock(Environment.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);

        ComputeSystemPOD computerSystemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        computerSystemPOD.setBis(mockedBufferedReader);
        computerSystemPOD.setName("DummyHPCSystem");
        computerSystemPOD.setNumberofNode(1);
        computerSystemPOD.appendRackID(0);

        Systems systems = new Systems(mockedEnvironment);

        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment, systems);

        SLAViolationLogger slaViolationLogger = mock(SLAViolationLogger.class);
        systems.addComputeSystem(
                ComputeSystem.Create(computerSystemPOD, mockedEnvironment, dataCenter, slaViolationLogger));

        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1\t41.07\t1\t1");
            when(mockedEnvironment.getCurrentLocalTime()).thenReturn(0, 1);
            assertFalse(systems.allJobsDone());
            systems.runACycle();
            assertTrue(systems.allJobsDone());
            
            dataCenter.calculatePower();
            
            ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

            verify(mockedActivitiesLogger, times(2)).write(argument.capture());

            List<String> values = argument.getAllValues();
            assertEquals("5\t", values.get(0));
            assertEquals("5\t5\t1\n", values.get(1));
            
            assertEquals(5.002941076127991,dataCenter.getTotalPowerConsumption(), 1.0E-8);
            assertEquals(1,dataCenter.getOverRed());
            
            verify(mockedBufferedReader).readLine();
            verify(mockedEnvironment, times(5)).getCurrentLocalTime();
            verify(mockedEnvironment).localTimeByEpoch();
            verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
            verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();
        } catch (IOException e) {
            fail("Not expect: " + e.getMessage());
        }

        verifyNoMoreInteractions(mockedActivitiesLogger, mockedBufferedReader, mockedEnvironment);
    }

}
