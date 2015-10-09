Data Center Simulator [![Build Status](https://travis-ci.org/coelhudo/DataCenterSimulator.svg?branch=master)](https://travis-ci.org/coelhudo/DataCenterSimulator)
=====================

# Introduction

The ADCMSim (Autonomic Data Center Management Simulator) provides a
way to develop, evaluate, and validate Autonomic Computing\[1\] based
solutions in a Data Center environment.

The main ADCMSim components are:
* Physical layer to represent data center infrastructure from the IT perspective
* Computational layer to represent systems that will be running in the
IT infrastructure
* Autonomic Computing layer to manage Autonomic Managers and its
configuration and interactions

# Architecture

Currently three kinds of systems are available for evaluation:
* Interactive Systems (low latency jobs).
* Enterprise Systems (low latency jobs mixed with high latency jobs).
* HPC (High-performance Computing) Systems (high latency jobs).

Each system has a workload based on its characteristics. HPC for
example use batch jobs that will mimic intensive workload. Also, for
this initial version, there are two schedulers that can be configured.
    * First Come First Served (the classical one)
    * Mininum Heat Recirculation

The goals of the Autonomic Managers are described using high-level
policies. Currently two action policies are available:
* Green: its objective is maximize energy efficiency in the data center
* SLA (Service Level Agreement) Violation: its objective is minimize SLA Violations

The thermal model is based on the one from Arizona State University
\[2\]. Also, the cooler and its Coefficient of Performance (COP)
currently used in ADCMSim is one described by Hewlett-Packard (HP)
laboratories \[3\].

# Configuration

This project is using [Apache Maven](https://maven.apache.org/) for management.

Current dependencies are listed in the pom.xml file.

## Instruction

* *mvn compile* to compile :)
* *mvn exec:java* to execute Simulator Main class
* *mvn test* to execute only units tests
* *mvn verify* to execute units tests and integration tests

## Using web frontend

* The main idea is use websocket to communicate with the simulation
  and collect intermediate results as well as final results. This is
  done using [WAMP](http://wamp.ws/).

* **Dependencies:** [jython (2.7.0)](http://www.jython.org/),
[crossbar.io](http://crossbar.io), [autobahn Python /JS (0.10.7 and its
dependencies)](http://autobahn.ws/).
    * Obs.: Some code from autobahn need to be commented (the method
      that contains "yield from") because jython 2.7, that's why (¯\(°_o)/¯).

* *mvn package* : to generate the jar that contains the simulator
* *jython backend_wamp.py* (a lot of information will appear in the console)
* *open index.html on your favorite browser* (tested on Firefox 40 and
41)

# Publications

The following publications provide more information about the
principles of the ADCMSim:

* Norouzi, Forough, and Michael Bauer. "Toward an Autonomic Energy
  Efficient Data Center." Green Computing and Communications
  (GreenCom), 2012 IEEE International Conference on. IEEE, 2012.

* Norouzi, Forough, and Michael Bauer. "Autonomic Management for
Energy Efficient Data Centers." CLOUD COMPUTING 2015 (2015): 153.


References
==========

1. [Autonomic Computing](https://en.wikipedia.org/wiki/Autonomic_computing
   "Autonomic Computing")

2. T. Mukherjee, A. Banerjee, G. Varsamopoulos, S. K. S. Gupta, and
S. Rungta, Spatio-Temporal Thermal-Aware Job Scheduling to Minimize
Energy Consumption in Virtualized Heterogeneous Data Centers. Elsevier
Computer Networks (ComNet), Vol. 53, Issue 17, Pages 2888-2904,
December, 2009.

3. J. Moore, J. Chase, P. Ranganathan, and R. Sharma. Making
scheduling "cool,": Temperature-aware workload placement in data
centers. In USENIX Annual Technical Conference, pages 61-75,
April 2005.
