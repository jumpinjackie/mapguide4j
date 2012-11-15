mapguide4j
==========

mapguide4j is a Java-based suite of web services and applications for MapGuide using the Java version of the MapGuide Web Extensions API

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
 - Play! Framework 2.0.4
 - MapGuide Open Source 2.4 (with Tomcat option, you don't need to actually have the Apache/Tomcat installed and running, we just need the MapGuideApi.jar and its dlls)

Known Limitations
=================

 ~~It is currently not possible to create a map viewer that communicates with this REST endpoint as the MapGuide Web Extensions API does not offer the ability to manipulate the display parameters of a MgMap (this is what RENDERDYNAMICOVERLAYIMAGE in the mapagent does, as it is native and has full access to internal/un-published APIs). The currently published version of RenderDynamicOverlay does not support manipulation of MgMap display parameters.

 We work around this with a specially built version of the MapGuide Java API that exposes some extra internal C++ classes needed for this to work.~~

Setup
=====

1. Install the Play! Framework (eg. C:\play)

2. Clone this repository into a subdirectory in the Play framework (eg. C:\play\mapguide4j)

3. Copy the following MapGuide dlls from your MapGuide install into your clone (must use the correct bitness):

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

4. Copy MapGuideApi.jar into the lib directory of your clone

5. Start the play framework (play run) on the clone directory. You may need to set the PATH and JAVA_HOME environment variables to ensure you are using the correct Java SDK

6. You can now access the following services and applications

 - mapagent: http://localhost:9000/mapguide/mapagent/mapagent.fcgi
 - REST endpoint: http://localhost:9000/mapguide/rest
 - AJAX Viewer: http://localhost:9000/mapguide/mapviewerajax

Example URLs for REST 
=====================

Resource Service APIs
---------------------

Getting the resource content of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content

Getting the resource header of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header

List the resource data of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data
    
List the resources that reference Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references
    
Feature Service APIs
--------------------

List spatial contexts of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/spatialcontexts

List schema names of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schemas
    
Describe Feature Schema (SHP_Default) of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Default
    
List class names under the Feature Schema (SHP_Default) of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Default/classes
    
Describe Class Definition (SHP_Default:Parcels) of Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Default/Parcels

List registered FDO providers

    GET http://localhost:9000/mapguide/rest/providers

Describe the capabilities of the SDF provider

    GET http://localhost:9000/mapguide/rest/providers/OSGeo.SDF/capabilities

List the available data stores for the SQL Server provider based on partial connection parameters (specified in the query string)

    GET http://localhost:9000/mapguide/rest/providers/OSGeo.SQLServerSpatial/datastores?Service=(local)\SQLEXPRESS&Username=myuser&Password=mypass

Get the available connection values for the SDF connection property (ReadOnly)

    GET http://localhost:9000/mapguide/rest/providers/OSGeo.SDF/connectvalues/ReadOnly

Feature Service APIs - CRUD
---------------------------

Return first 1000 features from (SHP_Default:Parcels) in Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Default/Parcels?maxfeatures=1000

Return parcels whose owner starts with "SCHMITT"

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Default/Parcels?filter=RNAME LIKE 'SCHMITT%'

Return first 1000 features with restricted list of properties from (SHP_Default:Parcels) in Library://Samples/Sheboygan/Data/Parcels.FeatureSource

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Default/Parcels?properties=Autogenerated_SDF_ID,RNAME,SHPGEOM&maxfeatures=1000

Return first 1000 features from (SHP_Default:Parcels) in Library://Samples/Sheboygan/Data/Parcels.FeatureSource transformed to WGS84.PseudoMercator (the Google/Bing/OSM coordinate syste,)

    GET http://localhost:9000/mapguide/rest/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Default/Parcels?maxfeatures=1000&transformto=WGS84.PseudoMercator

Coordinate System APIs
----------------------

List coordinate system categories

    GET http://localhost:9000/mapguide/rest/coordsys/categories
    
List coordinate systems under category of Australia

    GET http://localhost:9000/mapguide/rest/coordsys/category/Australia

Get the EPSG code of the coordinate system (mentor code: LL84)

    GET http://localhost:9000/mapguide/rest/coordsys/mentor/LL84/epsg
    
Get the WKT of the coordinate system (mentor code: LL84)

    GET http://localhost:9000/mapguide/rest/coordsys/mentor/LL84/wkt
    
Get the mentor code of the coordinate system (EPSG:4326)

    GET http://localhost:9000/mapguide/rest/coordsys/epsg/4326/mentor
    
Get the WKT of the coordinate system (EPSG:4326)

    GET http://localhost:9000/mapguide/rest/coordsys/epsg/4326/wkt
    
Get the mentor code of the coordinate system wkt

    GET http://localhost:9000/mapguide/rest/coordsys/tomentor/GEOGCS["LL84",DATUM["WGS84",SPHEROID["WGS84",6378137.000,298.25722293]],PRIMEM["Greenwich",0],UNIT["Degree",0.01745329251994]]
    
Get the epsg code of the coordinate system wkt

    GET http://localhost:9000/mapguide/rest/coordsys/toepsg/GEOGCS["LL84",DATUM["WGS84",SPHEROID["WGS84",6378137.000,298.25722293]],PRIMEM["Greenwich",0],UNIT["Degree",0.01745329251994]]


Site APIs
---------

The following requests require a session id created from an Administrator account. Anonymous MapGuide session ids are denied access.

Get current site status

    GET http://localhost:9000/mapguide/rest/site/status

Get current site version

    GET http://localhost:9000/mapguide/rest/site/version

List user groups

    GET http://localhost:9000/mapguide/rest/site/groups

List roles for WfsUser

    GET http://localhost:9000/mapguide/rest/site/user/WfsUser/roles

List groups for WfsUser

    GET http://localhost:9000/mapguide/rest/site/user/WfsUser/groups

List users under group "Everyone"

    GET http://localhost:9000/mapguide/rest/site/groups/Everyone/users