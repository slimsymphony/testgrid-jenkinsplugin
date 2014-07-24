testgrid-jenkinsplugin
======================

This project is a jenkins plugin for connecting jenkins with testgrid. Users can invoke in-device testing via the plugin. 
It invoke testgrid-client to publish Task and receive results, also do some gathering and basic analysis tasks.
Please be notified that the testgrid-client won't justify whether the test tasks execution result. 
So the plugin will take responsibility to verify if the execution was succeed and if user got what they want.
