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

    var received = 0;

//     function onevent1(args) {
//	console.log("Got event:", args[0]);
//	received += 1;
//	if (received > 5) {
//             console.log("Closing ..");
//             connection.close();
//	}
//     }console.log(racks[i])

    //session.subscribe('com.myapp.topic1', onevent1);
    currentSession = session
};

function configure() {
    if(!currentSession) {
	return
    }

    createDataCenterElement = function(elementUID, dataCenterClass) {
	var HTMLDataCenterElementFormatted = document.createElement("div")
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
		var HTMLDataCenterRackFormatted = createDataCenterElement(racks[i].id, 'rack')
		$("#simulation").append(HTMLDataCenterRackFormatted)
		var rackSelector = "#" + racks[i].id.replace(/\./g, "\\.")
		$(rackSelector).html(racks[i].id)
		$(rackSelector).css({"padding-left":"50px"});
		var chassis = racks[i].chassis
		for(j = 0; j < chassis.length; j++) {
		    var HTMLDataCenterChassisFormatted = createDataCenterElement(chassis[j].id, 'chassis')
		    $(rackSelector).append(HTMLDataCenterChassisFormatted)
		    var chassisSelector = "#" + chassis[j].id.replace(/\./g, "\\.")
		    $(chassisSelector).html(chassis[j].id)
		    $(chassisSelector).css({"padding-left":"75px"});
		}
	    }
	}
    );
}

function execute() {
    if(!currentSession) {
	return
    }

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
