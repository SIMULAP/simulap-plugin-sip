![Update statistics](https://github.com/SIMULAP/simulap-plugin-sip/workflows/Update%20statistics/badge.svg)

© Copyright 2018-2020 Hewlett Packard Enterprise Development LP
Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0

## What is simulap-plugin-sip ?
simulap-plugin-sip, brings SIP ( RFC 3261) connectivity to Apache JMeter.
 


## How to get simulap-plugin-sip source code ?

__Prerequisites__ : 

* git must be installed on your __local__ system and you must be familiar with git CLI  : see https://git-scm.com/
* You must know how to fork, clone a repository on github  : see https://help.github.com/

#### __Step 1__ : fork https://github.com/SIMULAP/simulap-plugin-sip

You obtain a new public github repository  : https://github.com/MY_USERNAME/simulap-plugin-sip .
In the following , https://github.com/SIMULAP/simulap-plugin-sip is called the __upstream__ while https://github.com/MY_USERNAME/simulap-plugin-sip is called the __origin__

#### __Step 2__ : create a local clone of https://github.com/MY_USERNAME/simulap-plugin-sip

```
prompt> git clone  git@github.com:MY_USERNAME/simulap-plugin-sip.git
```

__Result__ :

A new directory has been created ( simulap-plugin-sip ) containing the source code for simulap-sip-plugin



## How to build simulap-plugin-sip  ?

__Prerequisites__ : 
* Maven must be installed on your system. see https://maven.apache.org/

__Step 1__

```
> cd simulap-plugin-sip
> mvn -X clean package 
```
__Result__

a new directory has been created ( simulap-plugin-sip/target ) containing the jar file `simulap-plugin-sip-0.0-00.jar`.



## How to start Apache JMeter with simulap-sip-plugin ?

__Prerequisites__ :
* Apache JMeter must be installed on your system : see http://jmeter.apache.org/
* In the following ,  __JMETER_HOME__ is the Apache JMeter home directory

__Step 1__

```
> cd simulap-plugin-sip
> mvn -X install -Djmeter.command=JMETER_HOME/bin/jmeter 
```


## How to create a SIP Test plan with  simulap-sip-plugin and Apache JMeter ?

Please refer to simulap-plugin-sip github wiki : https://github.com/SIMULAP/simulap-plugin-sip/wiki


