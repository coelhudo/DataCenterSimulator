try {
    var autobahn = require('autobahn');
} catch (e) {
    // when running in browser, AutobahnJS will
    // be included without a module system
}

var connection = new autobahn.Connection({
    url: 'ws://127.0.0.1:8888/ws',
    realm: 'crossbardemo'}
					);

var currentSession = null

$(document).ready(function(){
    $('#execute').hide();
    $('#results').hide();
    $('#simulation_status').html("Not running ...");
});


connection.onopen = function (session) {
    currentSession = session
};

connection.onclose = function (reason, details) {
    if(reason === "closed") {
	$('#simulation_status').html("Done");
    } else {
	$('#simulation_status').html("Error: Connection " + reason);
    }
};

function makeSimulation() {
    return {
	numberOfRacks : 0,
	numberOfChassis : 0,
	numberOfServers : 0,

	configure : function() {

	    if(!currentSession) {
		return
	    }

	    currentSession.call('digs.sim.topology').then(
		function(res) {
		    var topology = JSON.parse(res)
		    var racks = topology.dataCenter.sort(function(a,b) {
			return parseInt(a.id.replace(/\_/g, "")) > parseInt(b.id.replace(/\_/g, ""))
		    })

		    updateRacksView(racks)

		    $('#configure').hide()
		    $('#execute').show()
		    $('#simulation_status').html("Configured");
		}
	    );

	    var updateRacksView = function(racks) {
		this.numberOfRacks = racks.length
		for(var i = 0; i < this.numberOfRacks; ++i) {
		    var currentRack = racks[i]
		    $("#simulation").append(createDataCenterElement('div', currentRack.id, 'rack'))
		    var rackSelector = "#" + currentRack.id
		    $(rackSelector).html(currentRack.id)
		    $(rackSelector).css({"padding-left":"50px"});
		    updateChassisView(currentRack.chassis, rackSelector)
		}
	    }

	    var updateChassisView = function(chassis, rackSelector) {
		this.numberOfChassis = chassis.length
		for(var i = 0; i < this.numberOfChassis; ++i) {
		    var currentChassis = chassis[i]
		    $(rackSelector).append(createDataCenterElement('div', currentChassis.id, 'chassis'))
		    var chassisSelector = "#" + currentChassis.id
		    $(chassisSelector).html(currentChassis.id)
		    $(chassisSelector).css({"padding-left":"75px"});
		    updateServersView(currentChassis.servers, chassisSelector);
		}
	    }

	    var updateServersView = function(servers, chassisSelector) {
		this.numberOfServers = servers.length
		for(var i = 0; i < this.numberOfServers; ++i) {
		    var currentServer = servers[i]
		    $(chassisSelector).append(createDataCenterElement('div', currentServer.id, 'server'))
		    var serverSelector = "#" + currentServer.id
		    $(serverSelector).html(currentServer.id + ': ')
		    $(serverSelector).css({"padding-left":"100px"});

		    $(serverSelector).append(createDataCenterElement('span', currentServer.id + '_status', 'server'))
		    $(serverSelector + '_status').html("NOT INITIALIZED")
		}
	    }

	    var createDataCenterElement = function(type, elementUID, dataCenterClass) {
		var HTMLDataCenterElementFormatted = document.createElement(type)
		HTMLDataCenterElementFormatted.setAttribute('id', elementUID)
		HTMLDataCenterElementFormatted.setAttribute('class', dataCenterClass)
		return HTMLDataCenterElementFormatted
	    };
	},

	execute : function() {
	    if(!currentSession) {
		return
	    }

	    $('#simulation_status').html("Running ...");

	    function receivePartialResults(partialResults) {
		console.log('received')
		results = JSON.parse(partialResults)['results']
		for(var i = 0; i < results.length; ++i) {
		    updateRacksStats(results[i].racksStats)
		}
	    }

	    var updateRacksStats = function(racksStats) {
		for(var i = 0; i < this.numberOfRacks; ++i) {
		    updateChassisStats(racksStats[i].chassisStats)
		}
	    }

	    var updateChassisStats = function(chassisStats) {
		for(var i = 0; i < this.numberOfChassis; ++i) {
		    updateServersStats(chassisStats[i].bladeServersStats)
		}
	    }

	    var updateServersStats = function(bladeServersStats) {
		for(var i = 0; i < this.numberOfServers; ++i) {
		    $('#' + bladeServersStats[i].id + '_status').html(bladeServersStats[i].status)
		}
	    }

	    currentSession.subscribe('digs.sim.partialResult', receivePartialResults);

	    currentSession.call('digs.sim.execute')
	    $('#execute').hide()
	    $('#results').show()
	},

	results : function() {
	    if(!currentSession) {
		return
	    }

	    $('#results').prop('disabled', true);

	    currentSession.call('digs.sim.results').then(
		function(results) {

		    appendResult('Local time: ', 'localTime', results['LocalTime'])

		    createHTMLResult('Total Energy Consumption: ', 'totalEnergy')
		    $("#totalEnergy").html(results['Total energy Consumption'])

		    createHTMLResult('Mean Power Consumption: ', 'meanPower')
		    $("#meanPower").html(results['Mean Power Consumption'])

		    createHTMLResult('Over Red: ', 'overRed')
		    $("#oveRed").html(results['Over RED'])

		    createHTMLResult('# of Messages Data Center Manager to System Managers: ', 'messages_dc_to_sys')
		    $("#messages_dc_to_sys").html(results['Messages']['# of Messages DC to sys'])

		    createHTMLResult('# of Messages System Managers to Nodes: ', 'messages_sys_to_nodes')
		    $("#messages_sys_to_nodes").html(results['Messages']['# of Messages sys to nodes'])

		    connection.close()
		}
	    );

	    var appendResult = function(elementLabel, elementValue, value) {
		createHTMLResult(elementLabel, elementValue)
		$('#' + elementValue).html(value)
		$('#' + elementValue).css({"font-weight":"italic"})
	    }

	    var createHTMLResult = function(label, valueID) {
		var HTMLElementLabel = document.createElement('span')
		HTMLElementLabel.setAttribute('id', valueID + '_label')
		HTMLElementLabel.setAttribute('class', 'result')
		$('#simulationResults').append(HTMLElementLabel)
		$('#' + valueID + '_label').html(label)
		$('#' + valueID + '_label').css({"font-weight":"bold"})

		var HTMLElementValue = document.createElement('span')
		HTMLElementValue.setAttribute('id', valueID)
		HTMLElementValue.setAttribute('class', 'result')
		$('#' + valueID + '_label').append(HTMLElementValue)
		$('#' + valueID + '_label').append(document.createElement('br'))
	    };

	}
    };
}

var simulation = makeSimulation()

connection.open();
