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

connection.onopen = function (session) {
    currentSession = session
};

function configure() {
    if(!currentSession) {
	return
    }

    createDataCenterElement = function(type, elementUID, dataCenterClass) {
	var HTMLDataCenterElementFormatted = document.createElement(type)
	HTMLDataCenterElementFormatted.setAttribute('id', elementUID)
	HTMLDataCenterElementFormatted.setAttribute('class', dataCenterClass)
	return HTMLDataCenterElementFormatted
    };

    currentSession.call('digs.sim.topology').then(
	function(res) {
	    var topology = JSON.parse(res)
	    var racks = topology.dataCenter.sort(function(a,b) {
		return parseInt(a.id.replace(/\./g, "")) > parseInt(b.id.replace(/\./g, ""))
	    })
	    for(i = 0; i < racks.length; i++) {
		var HTMLDataCenterRackFormatted = createDataCenterElement('div', racks[i].id, 'rack')
		$("#simulation").append(HTMLDataCenterRackFormatted)
		var rackSelector = "#" + racks[i].id.replace(/\./g, "\\.")
		$(rackSelector).html(racks[i].id)
		$(rackSelector).css({"padding-left":"50px"});
		var chassis = racks[i].chassis
		for(j = 0; j < chassis.length; j++) {
		    var HTMLDataCenterChassisFormatted = createDataCenterElement('div', chassis[j].id, 'chassis')
		    $(rackSelector).append(HTMLDataCenterChassisFormatted)
		    var chassisSelector = "#" + chassis[j].id.replace(/\./g, "\\.")
		    $(chassisSelector).html(chassis[j].id)
		    $(chassisSelector).css({"padding-left":"75px"});

		    servers = chassis[j].servers;
		    for(k = 0; k < servers.length; k++) {
			var HTMLDataCenterBladeServerFormatted = createDataCenterElement('div', servers[k].id, 'server')
			$(chassisSelector).append(HTMLDataCenterBladeServerFormatted)
			var serverSelector = "#" + servers[k].id.replace(/\./g, "\\.")
			$(serverSelector).html(servers[k].id + ': ')
			$(serverSelector).css({"padding-left":"100px"});

			var HTMLServerStatusFormatted = createDataCenterElement('span', servers[k].id + '\.status', 'server')
			$(serverSelector).append(HTMLServerStatusFormatted)
			$(servers[k].id + '\\.status').html("NOT INITIALIZED")
		    }
		}
	    }
	}
    );
}

function execute() {
    if(!currentSession) {
	return
    }

    function receivePartialResults(partialResult) {
	racksStats = JSON.parse(partialResult).racksStats
	for(i = 0; i < racksStats.length; i++) {
	    rackStats = racksStats[i]
	    chassisStats = rackStats.chassisStats
	    for(j = 0; j < chassisStats.length; j++) {
		bladeServersStats = chassisStats[j].bladeServersStats
		for(k = 0; k < bladeServersStats.length; k++) {
		    var bladeServerStatsSelector = "#" + bladeServersStats[k].id.replace(/\./g, "\\.")
		    $(bladeServerStatsSelector + '\\.status').html(bladeServersStats[k].status)
		}
	    }
	}
    }

    currentSession.subscribe('digs.sim.partialResult', receivePartialResults);

    currentSession.call('digs.sim.execute')
}

function results() {
    if(!currentSession) {
	return
    }

    currentSession.call('digs.sim.results').then(
	function(results) {
	    var totalEnergy = results['Total energy Consumption']
	    var HTMLTotalEnergy = document.createElement("div")
	    HTMLTotalEnergy.setAttribute('id', 'totalEnergy')
	    HTMLTotalEnergy.setAttribute('class', 'result')
	    $("#simulationResults").append(HTMLTotalEnergy)
	    $("#totalEnergy").html(totalEnergy)
	    var meanPowerConsumption = results['Mean Power Consumption']
	    var HTMLMeanPower = document.createElement("div")
	    HTMLMeanPower.setAttribute('id', 'meanPower')
	    HTMLMeanPower.setAttribute('class', 'result')
	    $("#simulationResults").append(HTMLMeanPower)
	    $("#meanPower").html(meanPowerConsumption)
	}
    );
}

connection.open();
