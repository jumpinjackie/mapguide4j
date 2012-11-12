mapguide4j-rest
===============

Java-based REST extension for MapGuide

Mostly inspired by discussions on a RESTful web service for MapGuide: http://trac.osgeo.org/mapguide/wiki/Future/RESTfulWebServices

Requirements
============
 - Java 6 SDK (bitness must match your MapGuide Server bitness)
 - Play! Framework 2.0.4
 - MapGuide Open Source 2.4 (with Tomcat option)

Setup
=====

1. Copy the following MapGuide dlls into this directory:

 - ACE.dll
 - GEOS.dll
 - lib_json.dll
 - MapGuideJavaApi.dll
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

2. Copy MapGuideApi.jar into the lib directory

3. Start the play framework on this directory

4. The mapguide4j-rest endpoint will be available under http://localhost:9000/mapguide/rest

