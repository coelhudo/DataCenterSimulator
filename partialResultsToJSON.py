def racksStatsToJSON(racksStats):
    result = list()
    for rackStats in racksStats:
        result.append({
            'id' : rackStats.getID().toString(),
            'chassisStats' : chassisStatsToJSON(rackStats.getChassisStats())
        })
    return result

def chassisStatsToJSON(chassisStats):
    result = list()
    for currentChassisStats in chassisStats:
        result.append({
            'id' : currentChassisStats.getID().toString(),
            'bladeServersStats' : bladeServersStatsToJSON(currentChassisStats.getBladeServersStats())
        })
    return result

def bladeServersStatsToJSON(bladeServersStats):
    result = list()
    for bladeServerStats in bladeServersStats:
        result.append({
            'id' : bladeServerStats.getID().toString(),
            'status' : bladeServerStats.getStatus().toString()
        })
    return result
