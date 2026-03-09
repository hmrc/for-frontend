Rent And Lease Details Frontend
===============================

Supplying business rental information is an important activity that allows the VO (Valuation Office) to calculate the rateable value of a property. 
Rateable value represents the open market annual rental value of a business/ non-domestic property. 

This repository contains the RALD (rent and lease details) form (aka FOR - Form Of Return) application - it is the website that allows users to submit their rental information to the VO.

Requirements
------------
* This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), 
so needs at least a [JRE](https://www.java.com/en/download/) to run.

* FOR-HOD-ADAPTER (you will need to provide a stub for this or you can stub method calls in HODConnector.scala)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
    
# Installation

### Cloning:

SSH
```
git@github.com:hmrc/for-frontend.git
```
HTTPS
```
https://github.com/hmrc/for-frontend.git
```

### Running RALD on your local machine

https://confluence.tools.tax.service.gov.uk/display/VOAO/Running+RALD+-+Rent+And+Lease+Details+on+your+local+machine


## first start service manager
virtualenv --python=/usr/bin/python2.7 servicemanager
source ~/servicemanager/bin/activate

sm --start VOA_FOR_ACCEPTANCE -r 
#this also starts FOR_FRONTEND so when running it from the source you need to stop it

## this shows all services that are now running
sm -s

#to stop FOR_FRONTEND so that you can run it from the source code. 
sm --stop FOR_FRONTEND

# to run FOR_FRONTEND from source
sbt 'run 9521'

#to test 
sbt test
