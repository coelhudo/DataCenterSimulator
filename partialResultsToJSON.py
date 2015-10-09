from itertools import izip

class PartialResults:
    def __init__ (self, numberOfRacks, numberOfChassis, numberOfServers):
        self.value = list()
        for i in range(0, numberOfRacks):
            rackResults = { 'id' : '-1', 'chassisStats' : list() }
            for j in range(0, numberOfChassis):
                chassisResults = { 'id' : '-1', 'bladeServersStats' : list() }
                for k in range(0, numberOfServers):
                    serverResults = { 'id' : '-1', 'status' : 'value' }
                    chassisResults['bladeServersStats'].append(serverResults)
                rackResults['chassisStats'].append(chassisResults)
            self.value.append(rackResults)

    def toJSON(self, racksStats):
        for rackResult, rackStats in izip(self.value, racksStats):
            rackResult['id'] = rackStats.getID().toString()
            for chassisResult, chassisStats in izip(rackResult['chassisStats'], rackStats.getChassisStats()):
                chassisResult['id'] = chassisStats.getID().toString()
                for serverResult, serverStats in izip(chassisResult['bladeServersStats'], chassisStats.getBladeServersStats()):
                    serverResult['id'] = serverStats.getID().toString()
                    serverResult['status'] = serverStats.getStatus().toString()
        return self.value
