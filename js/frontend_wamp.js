try {
    var autobahn = require('autobahn');
} catch (e) {
    // when running in browser, AutobahnJS will
    // be included without a module system
    console.log(e);
}

var connection = new autobahn.Connection({
    url: 'ws://127.0.0.1:8888/ws',
    realm: 'crossbardemo'
});

var currentSession = null;


connection.onopen = function (session) {
    'use strict';
    currentSession = session;
};

connection.onclose = function (reason, details) {
    'use strict';
    if(reason === "closed") {
        document.getElementById('simulation_status').innerHTML = "Done";
    } else {
        document.getElementById('simulation_status').innerHTML = "Error: Connection " + reason;
    }
};

serverElements = {};
var counter = 0;

function makeSimulation() {
    'use strict';
    return {
        configure : function() {

            if(!currentSession) {
                return;
            }

            currentSession.call('digs.sim.topology').then(
                function(res) {
                    var topology = JSON.parse(res);
                    var racks = topology.dataCenter.sort(function(a,b) {
                        return parseInt(a.id.replace(/_/g, "")) > parseInt(b.id.replace(/_/g, ""));
                    });

                    updateRacksView(racks);

                    document.getElementById('configure').style.display = 'none';
                    document.getElementById('execute').style.display = '';
                    document.getElementById('simulation_status').innerHTML = "Configured";
                }
            );

            var updateRacksView = function (racks) {
                var simulation = document.getElementById('simulation');
                racks.forEach(function(currentRack) {
                    simulation.appendChild(createDataCenterElement('div', currentRack.id, 'rack'));
                    var rackElement = document.getElementById(currentRack.id);
                    rackElement.innerHTML = currentRack.id;
                    updateChassisView(currentRack.chassis, rackElement);
                });
            };

            var updateChassisView = function (chassis, rackSelector) {
                chassis.forEach(function(currentChassis) {
                    rackSelector.appendChild(createDataCenterElement('div', currentChassis.id, 'chassis'));
                    var chassisElement = document.getElementById(currentChassis.id);
                    chassisElement.innerHTML = currentChassis.id;
                    chassisElement.style[ 'padding-left'] = '25px';
                    updateServersView(currentChassis.servers, chassisElement);
                });
            };

            var updateServersView = function(servers, chassisSelector) {
                servers.forEach(function(currentServer) {
                    chassisSelector.appendChild(createDataCenterElement('div', currentServer.id, 'server'));
                    var serverElement = document.getElementById(currentServer.id);
                    serverElement.style['padding-left'] = '50px';

                    serverElements[currentServer.id] = {};
                    createServerAttribute(serverElement, currentServer.id, 'id');
                    serverElements[currentServer.id]['id'].innerHTML = currentServer.id;
                    createServerAttribute(serverElement, currentServer.id, 'status');
                    createServerAttribute(serverElement, currentServer.id, 'cpu');
                    createServerAttribute(serverElement, currentServer.id, 'mips');
                    createServerAttribute(serverElement, currentServer.id, 'batchJobs');
                    createServerAttribute(serverElement, currentServer.id, 'enterpriseJobs');
                });
            };

            var createServerAttribute = function(serverElement, id, label) {
                serverElement.appendChild(createDataCenterElement('span', id + '_' + label + '_value', 'server_item'));
                var serverIDElement = document.getElementById(id + '_' + label + '_value');
                serverIDElement.innerHTML = label + ': '
                serverIDElement.style['font-weight'] = 'bold';

                serverElement.appendChild(createDataCenterElement('span', id + '_' + label, 'server_item'));
                var serverAttributeElement = document.getElementById(id + '_' + label)
                serverAttributeElement.innerHTML = 'NOT INITIALIZED';
                serverElement.appendChild(document.createElement('br'));
                serverElements[id][label] = serverAttributeElement;
            };

            var createDataCenterElement = function(type, elementUID, dataCenterClass) {
                var HTMLDataCenterElementFormatted = document.createElement(type);
                HTMLDataCenterElementFormatted.setAttribute('id', elementUID);
                HTMLDataCenterElementFormatted.setAttribute('class', dataCenterClass);
                return HTMLDataCenterElementFormatted;
            };
        },

        execute : function() {
            if(!currentSession) {
                return;
            }

            document.getElementById('execute').style.display = 'none';
            document.getElementById('results').style.display = '';
            document.getElementById('simulation_status').innerHTML  = "Running ...";

            function receivePartialResults(partialResults) {
                var results = JSON.parse(partialResults).results;
                counter = counter + 1;
                results.forEach(function(currentResult) {
                    updateRacksStats(currentResult.racksStats);
                });
            }

            var updateRacksStats = function(racksStats) {
                racksStats.forEach(function(currentRack) {
                    updateChassisStats(currentRack.chassis);
                });
            };

            var updateChassisStats = function(chassisStats) {
                chassisStats.forEach(function(currentChassis) {
                    updateServersStats(currentChassis.bladeServers);
                });
            };

            var updateServersStats = function(bladeServersStats) {
                var status = ['NOT ASSIGNED TO ANY SYSTEM', 'NOT ASSIGNED TO ANY APPLICATION', 'IDLE', 'RUNNING NORMAL', 'RUNNING BUSY'];
                bladeServersStats.forEach(function(currentServer) {
                    serverElements[currentServer.id]['status'].textContent = status[currentServer.status[0]];
                    serverElements[currentServer.id]['cpu'].textContent = currentServer.status[1];
                    serverElements[currentServer.id]['mips'].textContent = currentServer.status[2];
                    serverElements[currentServer.id]['batchJobs'].textContent = currentServer.status[3];
                    serverElements[currentServer.id]['enterpriseJobs'].textContent = currentServer.status[4];
                });
            };

            currentSession.subscribe('digs.sim.partialResult', receivePartialResults);

            currentSession.call('digs.sim.execute');
        },

        results : function() {
            if(!currentSession) {
                return;
            }

            document.getElementById('results').disabled = true;

            currentSession.call('digs.sim.results').then(
                function(results) {

                    appendResult('Local time: ', 'localTime', results.LocalTime);
                    appendResult('Total Energy Consumption: ', 'totalEnergy', results['Total energy Consumption']);
                    appendResult('Mean Power Consumption: ', 'meanPower', results['Mean Power Consumption']);
                    appendResult('# of Messages Data Center Manager to System Managers: ', 'messages_dc_to_sys', results.Messages['# of Messages DC to sys']);
                    appendResult('# of Messages System Managers to Nodes: ', 'messages_sys_to_nodes', results.Messages['# of Messages sys to nodes']);

		    var resultLabels = document.getElementsByClassName('result_label')
		    for(var i = 0; i < resultLabels.length; ++i) {
			resultLabels[i].style['font-weight'] = 'bold';
			resultLabels[i].style['font-style'] = 'italic';
		    }
                    console.log('received ' + counter + ' messages');
                    connection.close();
                });

            var appendResult = function(elementLabel, elementValue, value) {
                var HTMLElementLabel = document.createElement('span');
                HTMLElementLabel.setAttribute('id', elementValue + '_label');
                HTMLElementLabel.setAttribute('class', 'result_label');
                HTMLElementLabel.innerHTML = elementLabel;

                var HTMLElementValue = document.createElement('span');
                HTMLElementValue.setAttribute('id', elementValue);
                HTMLElementValue.setAttribute('class', 'result_value');
                HTMLElementValue.innerHTML = value;

		document.getElementById('simulationResults').appendChild(HTMLElementLabel);
                document.getElementById('simulationResults').appendChild(HTMLElementValue);
		document.getElementById('simulationResults').appendChild(document.createElement('br'));
            };
        }
    };
}

var simulation = makeSimulation();

connection.open();
