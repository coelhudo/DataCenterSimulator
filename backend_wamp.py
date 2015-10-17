from __future__ import print_function
from os import environ
import time

import sys
sys.path.append('./target/ADCMSimulator-adcmsim-0.0.1.jar')
from simulator import *

from java.util.concurrent import ArrayBlockingQueue, TimeUnit;
import json
from dataCenterTopologyToJSON import *
from partialResultsToJSON import *

from threading import Thread

from twisted.internet import reactor
from twisted.internet.defer import inlineCallbacks
from twisted.python.failure import Failure

from autobahn.twisted.util import sleep
from autobahn import wamp
from autobahn.twisted.wamp import ApplicationSession, ApplicationRunner
from autobahn.wamp.types import PublishOptions

class Sim(ApplicationSession):

    @inlineCallbacks
    def onJoin(self, details):
        print("session attached")
        results = []
        res = yield self.register(self)
        results.extend(res)

        for res in results:
            if isinstance(res, Failure):
                print("Failed to register procedure: {}".format(res.value))
            else:
                print("registration ID {}: {}".format(res.id, res.procedure))

    @wamp.register(u'digs.sim.topology')
    def topology(self):
        self.configure()
        print('topology called\n')
        dataCenter = self.simulator.getDatacenter()
        dataCenterSpecificationPayload = json.dumps(dataCenterToJSON(dataCenter), ensure_ascii = False).encode('utf8')
        return dataCenterSpecificationPayload

    def configure(self):
        self.dataCenterBuilder = SimulatorBuilder("configs/DC_Logic.xml")
        self.simulatorPOD = self.dataCenterBuilder.build()
        self.environment = Environment()
        self.partialResults = ArrayBlockingQueue(5)
        self.simulator = Simulator(self.simulatorPOD, self.environment, self.partialResults)
        #all racks contain the same number of chassis and all chassis have the same amount of servers.
        #it is a limitation, unless this becomes a requirement it will remain as it is.
        self.racks = self.simulator.getDatacenter().getRacks()
        self.chassis = self.racks.toArray()[0].getChassis()
        self.servers = self.chassis.toArray()[0].getServers()

    @wamp.register(u'digs.sim.execute')
    def execute(self):
        print('Execute called')
        self.simulatorThread = Thread(target=self.simulator.run)
        self.simulatorThread.start()
        self.partialResultsThread = Thread(target=self.partialResultsPublisher)
        self.partialResultsThread.start()

    def partialResultsPublisher(self):
        counter = 0
        amountOfDataToBeSent = 0
        bundle = list()
        partial = PartialResults(len(self.racks), len(self.chassis), len(self.servers))
        while(True):
            partialResult = self.partialResults.poll(50, TimeUnit.MILLISECONDS)
            if partialResult != None:

                counter = 0
                amountOfDataToBeSent += 1
                if amountOfDataToBeSent == 10:
                    amountOfDataToBeSent = 0
                    racksStats = {'racksStats' : partial.toJSON(partialResult.getRacksStats()) }
                    bundle.append(racksStats)

                if len(bundle) == 7:
                    payload = json.dumps({'results' : bundle }, ensure_ascii = False, separators=(',',':')).encode('utf8')
                    #print('Payload size {0}'.format(len(payload)))
                    reactor.callFromThread(self.publish, u'digs.sim.partialResult', payload)
                    del bundle[:]
            else:
                counter += 1
            if counter > 50:
                break;
        print('Simulation ended')

    @wamp.register(u'digs.sim.results')
    def results(self):
        print('Results called')
        self.partialResultsThread.join()
        self.simulatorThread.join()
        results = SimulationResults(self.simulator)
        print('Results collected')
        return {'Total energy Consumption' : results.getTotalPowerConsumption(),
                'LocalTime' : results.getLocalTime(),
                'Mean Power Consumption' : results.getMeanPowerConsumption(),
                'Over RED' : results.getOverRedTemperatureNumber(),
                'Messages' : {
                    '# of Messages DC to sys' : results.getNumberOfMessagesFromDataCenterToSystem(),
                    '# of Messages sys to nodes' : results.getNumberOfMessagesFromSystemToNodes()
                }
        }

if __name__ == '__main__':
    runner = ApplicationRunner(
        environ.get("AUTOBAHN_DEMO_ROUTER", u"ws://127.0.0.1:8888/ws"),
        u"crossbardemo",
        debug_wamp=False,  # optional; log many WAMP details
        debug=False,  # optional; log even more details
    )
    start = time.clock()
    try:
        runner.run(Sim)
    except:
        print('time {0}'.format(time.clock()))
