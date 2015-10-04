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
