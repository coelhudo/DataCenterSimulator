from __future__ import print_function
from os import environ

import sys
sys.path.append('./target/ADCMSimulator-adcmsim-0.0.1.jar')
from simulator import *

from java.util.concurrent import ArrayBlockingQueue, TimeUnit;
import json

from twisted.internet.defer import inlineCallbacks
from twisted.python.failure import Failure

from autobahn.twisted.util import sleep
from autobahn import wamp
from autobahn.twisted.wamp import ApplicationSession, ApplicationRunner

class Sim():
    def __init__(self):
        self.dataCenterBuilder = SimulatorBuilder("configs/DC_Logic.xml")
        self.simulatorPOD = self.dataCenterBuilder.build()
        self.environment = Environment()
        self.partialResults = ArrayBlockingQueue(5)
        self.simulator = Simulator(self.simulatorPOD, self.environment, self.partialResults)

    @wamp.register(u'digs.sim.topology')
    def topology(self):
        print('topology called\n')
        dataCenter = self.simulator.getDatacenter()
        dataCenterSpecificationPayload = json.dumps(dataCenterToJSON(dataCenter), ensure_ascii = False).encode('utf8')
        return dataCenterSpecificationPayload

def dataCenterToJSON(dataCenter):
    dataCenterSpecification = {}
    dataCenterSpecification['dataCenter'] = racksToJSON(dataCenter.getRacks())
    return dataCenterSpecification

def racksToJSON(racks):
    racksSpecification = list()
    for currentRack in racks:
        singleRackSpecification = dict()
        singleRackSpecification['id'] = currentRack.getID().toString()
        singleRackSpecification['chassis'] = chassisToJSON(currentRack.getChassis())
        racksSpecification.append(singleRackSpecification)
    return racksSpecification

def chassisToJSON(chassis):
    chassisSpecification = list()
    for currentChassis in chassis:
        singleChassisSpecification = dict()
        singleChassisSpecification['id'] = currentChassis.getID().toString()
        singleChassisSpecification['type'] = currentChassis.getChassisType()
        singleChassisSpecification['servers'] = serversToJSON(currentChassis.getServers())
        chassisSpecification.append(singleChassisSpecification)
    return chassisSpecification

def serversToJSON(servers):
    serversSpecification = list()
    for server in servers:
        serverSpecification = dict()
        serverSpecification['id'] = server.getID().toString()
        serversSpecification.append(serverSpecification)
    return serversSpecification

class Component(ApplicationSession):
    """
    An to execute AMDCSimulator.
    """

    @inlineCallbacks
    def onJoin(self, details):
        print("session attached")
        results = []

        sim = Sim()

        res = yield self.register(sim)
        results.extend(res)

        for res in results:
            if isinstance(res, Failure):
                print("Failed to register procedure: {}".format(res.value))
            else:
                print("registration ID {}: {}".format(res.id, res.procedure))

        #counter = 0
        #while True:
        #    print('backend publishing com.myapp.topic1', counter)
        #    self.publish(u'com.myapp.topic1', counter)
        #    counter += 1
        #    yield sleep(1)

if __name__ == '__main__':
    runner = ApplicationRunner(
        environ.get("AUTOBAHN_DEMO_ROUTER", u"ws://127.0.0.1:8888/ws"),
        u"crossbardemo",
        debug_wamp=False,  # optional; log many WAMP details
        debug=False,  # optional; log even more details
    )
    runner.run(Component)
