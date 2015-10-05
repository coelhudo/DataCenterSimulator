from __future__ import print_function
from os import environ

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

    def f(self, x):
        print(x)

    @wamp.register(u'digs.sim.execute')
    def execute(self):
        print('Execute called')
        self.simulatorThread = Thread(target=self.simulator.run)
        self.simulatorThread.start()
        self.partialResultsThread = Thread(target=self.partialResultsPublisher)
        self.partialResultsThread.start()

    def partialResultsPublisher(self):
        counter = 0
        while(True):
            partialResult = self.partialResults.poll(50, TimeUnit.MILLISECONDS)
            if partialResult != None:
                racksStats = {'racksStats' : racksStatsToJSON(partialResult.getRacksStats()) }
                self.payload = json.dumps(racksStats, ensure_ascii = False).encode('utf8')
                reactor.callFromThread(self.publish, u'digs.sim.partialResult', self.payload)
                counter = 0
            else:
                counter += 1

            if counter > 50:
                break;

    @wamp.register(u'digs.sim.results')
    def results(self):
        print('Results called')
        self.partialResultsThread.join()
        self.simulatorThread.join()
        self.results = SimulationResults(self.simulator)
        return {'Total energy Consumption' : self.results.getTotalPowerConsumption(),
                'LocalTime' : self.results.getLocalTime(),
                'Mean Power Consumption' : self.results.getMeanPowerConsumption(),
                'Over RED' : self.results.getOverRedTemperatureNumber(),
                'Messages' : {
                    '# of Messages DC to sys' : self.results.getNumberOfMessagesFromDataCenterToSystem(),
                    '# of Messages sys to nodes' : self.results.getNumberOfMessagesFromSystemToNodes()
                }
        }

if __name__ == '__main__':
    runner = ApplicationRunner(
        environ.get("AUTOBAHN_DEMO_ROUTER", u"ws://127.0.0.1:8888/ws"),
        u"crossbardemo",
        debug_wamp=False,  # optional; log many WAMP details
        debug=False,  # optional; log even more details
    )
    runner.run(Sim)
