package simulator.ra;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;

/**
 *
 * @author fnorouz
 */
public class MHR extends ResourceAllocation {

	private static final Logger LOGGER = Logger.getLogger(MHR.class.getName());

	@Inject
	public MHR(Environment environment, DataCenter dataCenter) {
		super(environment, dataCenter);
	}

	int[] powIndex = { 15, 31, 16, 11, 36, 10, 30, 6, 20, 21, 35, 32, 17, 26, 25, 7, 27, 12, 42, 37, 41, 5, 2, 1, 0, 22,
			40, 47, 46, 13, 45, 29, 23, 8, 28, 43, 48, 9, 38, 33, 18, 3, 34, 44, 24, 14, 49, 19, 39, 4 };

	public int nextServer(List<BladeServer> bs) {
		int i = 0, j = 0;
		for (i = 0; i < powIndex.length; i++) {
			for (j = 0; j < bs.size(); j++) {
				if (bs.get(j).isRunningNormal() && powIndex[i] == bs.get(j).getID().getChassisID()) {
					return j;
				}
			}

		}
		return -2;
	}

	@Override
	public int nextServerInSys(List<BladeServer> bs) {
		int i = 0, j = 0;
		for (i = 0; i < powIndex.length; i++) {
			for (j = 0; j < bs.size(); j++) {
				if (bs.get(j).isNotApplicationAssigned() && powIndex[i] == bs.get(j).getID().getChassisID()) {
					return j;
				}
			}

		}
		return -2;
	}

	public BladeServer nextServerSys(List<Chassis> chassis) {
		for (int j = powIndex.length - 1; j >= 0; j--) {
			Chassis foundChassis = null;
			for (Chassis currentChassis : chassis) {
				if (powIndex[j] == currentChassis.getID().getChassisID()) {
					foundChassis = currentChassis;
					break;
				}
			}

			if (foundChassis == null) {
				continue;
			}

			BladeServer result = foundChassis.getNextNotAssignedBladeServer();
			if (result == null) {
				continue;
			}

			return result;
		}
		return null;
	}
	// this funtion is used in ComputeSystem for allocating resources
	// List is array of compute nodes

	public List<BladeServer> allocateSystemLevelServer(List<BladeServer> availableBladeServers,
			int numberOfRequestedServers) {
		List<BladeServer> requestedBladeServers = null;

		final int numberOfAvailableServers = BladeServerCollectionOperations.countRunningNormal(availableBladeServers);
		if (numberOfAvailableServers < numberOfRequestedServers) {
			LOGGER.fine(String.format("Not enough server available (Available: %d, Requested: %d)",
					numberOfAvailableServers, numberOfRequestedServers));
			return null;
		}

		LOGGER.fine("Allocating requested servers");
		requestedBladeServers = new ArrayList<BladeServer>();

		int j = 0;
		for (int k = powIndex.length - 1; k >= 0 && j < numberOfRequestedServers; k--) {
			for (int i = 0; i < availableBladeServers.size(); i++) {
				BladeServer current = availableBladeServers.get(i);
				if (current.isRunningNormal() && powIndex[k] == current.getID().getChassisID()) {
					requestedBladeServers.add(current);
					j++;
					if (j == numberOfRequestedServers) {
						break;
					}
				}
			}
		}

		return requestedBladeServers;
	}
}
