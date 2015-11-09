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

updateSelectors = {};

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
                    var serverSelector = "#" + currentServer.id;
		    $(serverSelector).css({"padding-left":"50px"});

		    updateSelectors[currentServer.id] = {};
		    createServerAttribute(serverSelector, currentServer.id, 'id');
		    updateSelectors[currentServer.id]['id'].html(currentServer.id + '');
		    createServerAttribute(serverSelector, currentServer.id, 'status');
                    createServerAttribute(serverSelector, currentServer.id, 'cpu');
                    createServerAttribute(serverSelector, currentServer.id, 'mips');
                    createServerAttribute(serverSelector, currentServer.id, 'batchJobs');
                    createServerAttribute(serverSelector, currentServer.id, 'enterpriseJobs');
                    console.log(updateSelectors)
                });
            };

            var createServerAttribute = function(serverSelector, id, label) {
		$(serverSelector).append(createDataCenterElement('span', id + '_' + label + '_value', 'server_item'));
		$('#' + id + '_' + label + '_value').html(label + ': ');
		$('#' + id + '_' + label + '_value').css({"font-weight":"bold"})
                $(serverSelector).append(createDataCenterElement('span', id + '_' + label, 'server_item'));
                $(serverSelector + '_' + label).html("NOT INITIALIZED");
                $(serverSelector).append(document.createElement('br'));
                updateSelectors[id][label] = $('#' + id + '_' + label)
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

            document.getElementById('simulation_status').innerHTML  = "Running ...";

            function receivePartialResults(partialResults) {
                var results = JSON.parse(partialResults).results;
                console.log('still receiving');
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
                    updateSelectors[currentServer.id]['status'].html(status[currentServer.status[0]]);
                    //updateSelectors[currentServer.id]['cpu'].html(currentServer.status[1]);
                    //updateSelectors[currentServer.id]['mips'].html(currentServer.status[2]);
                    //updateSelectors[currentServer.id]['batchJobs'].html(currentServer.status[3]);
                    //updateSelectors[currentServer.id]['enterpriseJobs'].html(currentServer.status[4]);
                });
            };

            currentSession.subscribe('digs.sim.partialResult', receivePartialResults);

            currentSession.call('digs.sim.execute');
            document.getElementById('execute').status.display = 'none';
	    document.getElementById('results').status.display = '';
        },

        results : function() {
            if(!currentSession) {
                return;
            }

            $('#results').prop('disabled', true);

            currentSession.call('digs.sim.results').then(
                function(results) {

                    appendResult('Local time: ', 'localTime', results.LocalTime);
                    appendResult('Total Energy Consumption: ', 'totalEnergy', results['Total energy Consumption']);
                    appendResult('Mean Power Consumption: ', 'meanPower', results['Mean Power Consumption']);
                    appendResult('# of Messages Data Center Manager to System Managers: ', 'messages_dc_to_sys', results.Messages['# of Messages DC to sys']);
                    appendResult('# of Messages System Managers to Nodes: ', 'messages_sys_to_nodes', results.Messages['# of Messages sys to nodes']);

                    $('.result_label').css({"font-weight":"bold"})
                    $('.result_value').css({"font-weight":"normal", "font-style":"italic"});

                    connection.close();
                });

            var appendResult = function(elementLabel, elementValue, value) {
                createHTMLResult(elementLabel, elementValue);
                $('#' + elementValue).html(value);
            };

            var createHTMLResult = function(label, valueID) {
                var HTMLElementLabel = document.createElement('span');
                HTMLElementLabel.setAttribute('id', valueID + '_label');
                HTMLElementLabel.setAttribute('class', 'result_label');
                $('#simulationResults').append(HTMLElementLabel);
                $('#' + valueID + '_label').html(label);

                var HTMLElementValue = document.createElement('span');
                HTMLElementValue.setAttribute('id', valueID);
                HTMLElementValue.setAttribute('class', 'result_value');
                $('#' + valueID + '_label').append(HTMLElementValue);
                $('#' + valueID + '_label').append(document.createElement('br'));
            };
        }
    };
}

var simulation = makeSimulation();

connection.open();
