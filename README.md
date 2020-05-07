![Update statistics](https://github.com/SIMULAP/simulap-plugin-sip/workflows/Update%20statistics/badge.svg): ![Generated Lines of Code Button](https://raw.githubusercontent.com/SIMULAP/simulap-plugin-sip/image-data/badge.svg)

[![License][License-Image]][License-Url]
[![Version][Version-Badge]][Version-URL]

© Copyright 2018-2020 Hewlett Packard Enterprise Development LP
Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0

## What is simulap-plugin-sip?
_simulap-plugin-sip_, brings __SIP__ ([RFC 3261](https://tools.ietf.org/html/rfc3261)) connectivity to __Apache JMeter__.

## How to get simulap-plugin-sip source code?

__Prerequisites__:

* __git__ must be installed on your __local__ system and you must be familiar with __git__ CLI: see https://git-scm.com/.
* You must know how to fork, clone a repository on __GitHub__: see https://help.github.com/.

#### __Step 1__: fork https://github.com/SIMULAP/simulap-plugin-sip

You obtain a new public __GitHub__ repository: https://github.com/MY_USERNAME/simulap-plugin-sip.
In the following, https://github.com/SIMULAP/simulap-plugin-sip is called the `upstream` while https://github.com/MY_USERNAME/simulap-plugin-sip is called the `origin`.

#### __Step 2__: create a local clone of https://github.com/MY_USERNAME/simulap-plugin-sip

```
prompt> git clone  git@github.com:MY_USERNAME/simulap-plugin-sip.git
```

__Result__:

A new directory has been created (`simulap-plugin-sip`) containing the source code for _simulap-plugin-sip_ plug-in.

## How to build simulap-plugin-sip?

__Prerequisites__:

* __Maven__ must be installed on your system. See https://maven.apache.org/.

__Step 1__:

```
> cd simulap-plugin-sip
> mvn -X clean package 
```
__Result__:

A new directory has been created (`simulap-plugin-sip/target`) containing the jar file `simulap-plugin-sip-0.0-00.jar`.

## How to start Apache JMeter with simulap-sip-plugin?

__Prerequisites__:
* __Apache JMeter__ must be installed on your system: see http://jmeter.apache.org/.
* In the following, __JMETER_HOME__ is the __Apache JMeter__ home directory.

__Step 1__:

```
> cd simulap-plugin-sip
> mvn -X install -Djmeter.command=JMETER_HOME/bin/jmeter 
```

## How to create a SIP Test plan with simulap-sip-plugin and Apache JMeter?

Please refer to [simulap-plugin-sip GitHub wiki](https://github.com/SIMULAP/simulap-plugin-sip/wiki) pages.

[License-Url]: https://www.apache.org/licenses/LICENSE-2.0
[License-Image]: https://img.shields.io/badge/License-Apache2-blue.svg

[Version-Badge]: https://d25lcipzij17d.cloudfront.net/badge.svg?id=go&type=5&v=1.11.0
[Version-URL]: https://github.com/SIMULAP/simulap-plugin-sip/releases/tag/1.11.0
