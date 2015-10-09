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

	    function receivePartialResults(partialResults) {
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
	},

	results : function() {
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
    };
}

var simulation = makeSimulation()

connection.open();
