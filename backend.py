from autobahn.twisted.websocket import WebSocketServerProtocol, \
    WebSocketServerFactory
import sys
sys.path.append('./target/ADCMSimulator-adcmsim-0.1.jar')
from simulator import *

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
                self.sendMessage('Executing simulator'.encode('utf8'))
                dataCenterBuilder = SimulatorBuilder("configs/DC_Logic.xml")
                simulatorPOD = dataCenterBuilder.build()
                environment = Environment()
                self.sendMessage('Environment created'.encode('utf8'))
                simulator = Simulator(simulatorPOD, environment)
                self.sendMessage('Simulator created'.encode('utf8'))
                results = simulator.execute()

                payload = lambda message_prefix,result: message_prefix.format(result).encode('utf8')

                self.sendMessage(payload('Total energy Consumption= {} \n', results.getTotalPowerConsumption()))
                self.sendMessage(payload('LocalTime= {} \n', results.getLocalTime()))
                self.sendMessage(payload('Mean Power Consumption= {} \n', results.getMeanPowerConsumption()))
                self.sendMessage(payload('Over RED\t {} \n', results.getOverRedTemperatureNumber()))
                self.sendMessage(payload('\t# of Messages DC to sys= {} \n', results.getNumberOfMessagesFromDataCenterToSystem()))
                self.sendMessage(payload('\t# of Messages sys to nodes= {} \n', results.getNumberOfMessagesFromSystemToNodes()))

                self.sendMessage('Simulation finished'.encode('utf8'))

    def onClose(self, wasClean, code, reason):
        print("WebSocket connection closed: {0}".format(reason))


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
