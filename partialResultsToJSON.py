from itertools import izip

class PartialResults:
    def __init__ (self, numberOfRacks, numberOfChassis, numberOfServers):
        self.value = list()
        for i in range(0, numberOfRacks):
            rackResults = { 'id' : '-1', 'chassis' : list() }
            for j in range(0, numberOfChassis):
                chassisResults = { 'id' : '-1', 'bladeServers' : list() }
                for k in range(0, numberOfServers):
                    serverResults = { 'id' : '-1', 'status' : 'value' }
                    chassisResults['bladeServers'].append(serverResults)
                rackResults['chassis'].append(chassisResults)
            self.value.append(rackResults)

    def toJSON(self, racksStats):
        for rackResult, rackStats in izip(self.value, racksStats):
            rackResult['id'] = rackStats.getID().toString()
            for chassisResult, chassisStats in izip(rackResult['chassis'], rackStats.getChassisStats()):
                chassisResult['id'] = chassisStats.getID().toString()
                for serverResult, serverStats in izip(chassisResult['bladeServers'], chassisStats.getBladeServersStats()):
                    serverResult['id'] = serverStats.getID().toString()
                    serverResult['status'] = [serverStats.getStatus().ordinal(),
                                              round(serverStats.getCurrentCPU(), 2),
                                              serverStats.getMIPS(),
                                              serverStats.getBatchJobsLength(),
                                              serverStats.getEnterpriseJobsLength(),
                                              serverStats.getInteractiveJobsLength()]
        return self.value
