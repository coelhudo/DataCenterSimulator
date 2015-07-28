Introduction
============

The ADCMSim (Autonomic Data Center Management Simulator) provides a
way to develop, evaluate, and validate Autonomic Computing\[1\] based
solutions in a Data Center environment.

The main ADCMSim components are:
* Physical layer to represent data center infrastructure from the IT perspective
* Computational layer to represent systems that will be running in the
IT infrastructure
* Autonomic Computing layer to manage Autonomic Managers and its
configuration and interactions

Architecture
===========

Currently three kinds of systems are available for evaluation:
* Interactive Systems
* Enterprise Systems
* HPC (High-performance Computing) Systems

Each system has a workload based on its characteristics. HPC for
example use batch jobs that will mimic intensive workload. Also, for
this initial version, there are two schedulers that can be configured.

The goals of the Autonomic Managers are described using high-level
policies. Currently two action policies are available:
* Green: its objective is maximize energy efficiency in the data center
* SLA (Service Level Agreement) Violation: its objective is minimize SLA Violations

The thermal model is based on the one from Arizona State University
\[2\]. Also, the cooler and its Coefficient of Performance (COP)
currently used in ADCMSim is one described by Hewlett-Packard (HP)
laboratories \[3\].

Publications
============

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
