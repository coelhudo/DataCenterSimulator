from autobahn.twisted.websocket import WebSocketServerProtocol, \
    WebSocketServerFactory
import sys
sys.path.append('./target/ADCMSimulator-adcmsim-0.1.jar')
from simulator import *
import json

class MyServerProtocol(WebSocketServerProtocol):

    def onConnect(self, request):
        print("Client connecting: {0}".format(request.peer))

    def onOpen(self):
        print("WebSocket connection open.")

    def onMessage(self, payload, isBinary):
        if isBinary:
            print("Binary message received: {0} bytes".format(len(payload)))
        else:
            print("Text message received: {0}".format(payload.decode('utf8')))

            if payload.decode('utf8') == "execute":
                dataCenterBuilder = SimulatorBuilder("configs/DC_Logic.xml")
                simulatorPOD = dataCenterBuilder.build()
                environment = Environment()
                simulator = Simulator(simulatorPOD, environment)

                dataCenter = simulator.getDatacenter()
                dataCenterSpecificationPayload = json.dumps(dataCenterToJSON(dataCenter), ensure_ascii = False).encode('utf8')
                self.sendMessage(dataCenterSpecificationPayload)
                simulator.run()
                results = SimulationResults(simulator)

                self.sendMessage(json.dumps(resultAsJSON(results), ensure_ascii = False).encode('utf8'))

    def onClose(self, wasClean, code, reason):
        print("WebSocket connection closed: {0}".format(reason))

def resultAsJSON(results):
    return {'Total energy Consumption' : results.getTotalPowerConsumption(),
            'LocalTime' : results.getLocalTime(),
            'Mean Power Consumption' : results.getMeanPowerConsumption(),
            'Over RED' : results.getOverRedTemperatureNumber(),
            'Messages' : {
                '# of Messages DC to sys' : results.getNumberOfMessagesFromDataCenterToSystem(),
                '# of Messages sys to nodes' : results.getNumberOfMessagesFromSystemToNodes()
            }
    }

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

if __name__ == '__main__':

    import sys

    from twisted.python import log
    from twisted.internet import reactor

    log.startLogging(sys.stdout)

    factory = WebSocketServerFactory(u"ws://127.0.0.1:8888", debug=False)
    factory.protocol = MyServerProtocol
    # factory.setProtocolOptions(maxConnections=2)

    reactor.listenTCP(8888, factory)
    reactor.run()
