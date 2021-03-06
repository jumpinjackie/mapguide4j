mapguide4j
==========

mapguide4j is a Java-based suite of web services and applications for MapGuide using the enhanced version of the MapGuide Java Web Extensions API introduced with [MapGuide Open Source 2.5](http://mapguide.osgeo.org)

mapguide4j is built on top of the [Play! Framework](http://www.playframework.org) and has no dependencies to IIS or Apache.

mapguide4j aims to become a fully capable replacement alternative for the existing default Web Tier that comes with MapGuide (and is bound to either Apache or IIS), providing the following applications and services:

 - A compatible mapagent http endpoint
 - A REST-ful http interface modeled on original discussions on a [RESTful web service for MapGuide](http://trac.osgeo.org/mapguide/wiki/Future/RESTfulWebServices)
 - The AJAX viewer ported over to run on top of Play!
 - (Eventually): A mobile/tablet friendly map viewer leveraging OpenLayers and modern HTML5 technologies.
 - (Eventually): Conversion into Play! module(s) allowing for componentization and reuse across other Play!
 applications
 - (Eventually): A better/improved Feature Source Inspector
 - (Hopefully): Support for other OGC standards (WFS-T, WMTS, etc) and improvements on what's offered out of the box by MapGuide.
 - (Hopefully): Fusion ported over to run on top of Play! with a compatibility layer for its PHP backend.
 - (Hopefully): A MapGuide Site Administrator built on top of Play! with more features and functionality

Requirements
============

 - Microsoft Windows (for now)
 - Java 6 SDK (bitness must match your MapGuide Server bitness)
 - [Play! Framework 2.0.4](http://www.playframework.org/)
 - [MapGuide Open Source 2.5](http://mapguide.osgeo.org) (with Tomcat option, you don't need to actually have the Apache/Tomcat installed and running, we just need the MapGuideApi.jar and its dlls)

Setup
=====

1. Install the Play! Framework (eg. C:\play)

2. Clone this repository into a subdirectory in the Play framework (eg. C:\play\mapguide4j)

3. Copy the following MapGuide dlls from your MapGuide install into your clone (must use the correct bitness):

 - ACE.dll
 - GEOS.dll
 - lib_json.dll
 - MapGuideJavaApiEx.dll
 - MgFoundation.dll
 - MgGeometry.dll
 - MgHttpHandler.dll
 - MgMapGuideCommon.dll
 - MgMdfModel.dll
 - MgMdfParser.dll
 - MgPlatformBase.dll
 - MgWebApp.dll
 - MgWebSupport.dll
 - xerces-c_3_1mg.dll

4. Copy MapGuideApiEx.jar into the lib directory of your clone

5. Start the play framework (play run) on the clone directory. You may need to set the PATH and JAVA_HOME environment variables to ensure you are using the correct Java SDK

6. You can now access the following services and applications

 - mapagent: http://localhost:9000/mapguide/mapagent/mapagent.fcgi
 - REST endpoint: http://localhost:9000/mapguide/rest
 - AJAX Viewer: http://localhost:9000/mapguide/mapviewerajax

License
=======

mapguide4j is licensed under the MIT license (see LICENSE.txt)

Other Useful Links
==================

 - [FAQ](https://github.com/jumpinjackie/mapguide4j/wiki/FAQ)
 - [Example REST URLs](https://github.com/jumpinjackie/mapguide4j/wiki/Example-mapguide4j-REST-URLs)