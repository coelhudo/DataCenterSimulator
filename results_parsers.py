import sys
import itertools as it
from collections import defaultdict

import matplotlib.pyplot as plt

server_status = defaultdict(list)
server_CPU = defaultdict(list)
server_MIPS = defaultdict(list)

def parse_file():
    with open(sys.argv[1],'r') as f:
        for key,group in it.groupby(f,lambda line: line.startswith('START')):
            if not key:
                group = list(group)
                for server, entry in enumerate(group):
                    entry = entry.rstrip('\n').split()
                    server_status[server].append(entry[0])
                    server_CPU[server].append(entry[1])
                    server_MIPS[server].append(entry[2])

if __name__ == "__main__":
    parse_file()

    number_of_servers = 3#len(server_status[0])
    figure, axis = plt.subplots(3,number_of_servers)
    ticks = range(0, len(server_status[0]))

    for column in range(0,number_of_servers):
        state_axis = axis[0, column]
        state_axis.plot(ticks, server_status[column])

        cpu_axis = axis[1, column]
        cpu_axis.plot(ticks, server_CPU[column])

        mips_axis = axis[2, column]
        mips_axis.plot(ticks, server_MIPS[column])

    plt.show()
