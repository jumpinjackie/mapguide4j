//NOTES:
//
//This test suite requires the Sheboygan dataset to be loaded first (yeah yeah ... let's not wax lyrical about semantics of unit testing)

//TODO:
//
// - Add tests for JSON and other representations once implemented
// - Do actual content verification in addition to response status verification

var rest_root_url = "/mapguide/rest";

// based heavily off:
// https://sites.google.com/a/van-steenbeek.net/archive/explorer_domparser_parsefromstring
if( typeof window.DOMParser === "undefined" ){
    window.DOMParser = function(){};

    window.DOMParser.prototype.parseFromString = function(str, contentType){
        if(typeof ActiveXObject !== 'undefined'){
            var xmldata = new ActiveXObject('MSXML.DomDocument');
            xmldata.async = false;
            xmldata.loadXML(str);
            return xmldata;
        } else if(typeof XMLHttpRequest !== 'undefined'){
            var xmldata = new XMLHttpRequest;

            if(!contentType){
                contentType = 'application/xml';
            }

            xmldata.open('GET', 'data:' + contentType + ';charset=utf-8,' + encodeURIComponent(str), false);

            if(xmldata.overrideMimeType) {
                xmldata.overrideMimeType(contentType);
            }

            xmldata.send(null);
            return xmldata.responseXML;
        }
    };
}

function api_test(url, type, data, callback) {
    $.ajax({
        url: url,
        type: type,
        processData: false,
        data: data,
        dataType: 'xml',
        async: false,
        beforeSend: function(xhr) {
            xhr.setRequestHeader("x-mapguide4j-test-harness", true);
        },
        complete: function(result) {
            if(result.status == 0) {
                callback(result.status, null);
            } else if(result.status == 404) {
                callback(result.status, null);
            } else if(result.status == 401) {
                callback(result.status, null);
            } else {
                callback(result.status, result.responseText);
            }
        }
    });
}

function api_test_with_credentials(url, type, data, username, password, callback) {
    $.ajax({
        url: url,
        type: type,
        processData: false,
        data: data,
        dataType: 'xml',
        beforeSend: function(xhr) {
            xhr.setRequestHeader("x-mapguide4j-test-harness", true);
            xhr.setRequestHeader("Authorization", "Basic " + btoa(username + ":" + password));
        },
        async: false,
        complete: function(result) {
            if(result.status == 0) {
                callback(result.status, null);
            } else if(result.status == 404) {
                callback(result.status, null);
            } else if(result.status == 401) {
                callback(result.status, null);
            } else {
                callback(result.status, result.responseText);
            }
        }
    });
}

function api_test_anon(url, type, data, callback) {
    return api_test_with_credentials(url, type, data, "Anonymous", "", callback);
}

function api_test_admin(url, type, data, callback) {
    return api_test_with_credentials(url, type, data, "Administrator", "admin", callback);
}

module("REST Session");
test("/session", function() {
    api_test(rest_root_url + "/session", "GET", {}, function(status, result) {
        ok(result == null, "Non-null result");
        ok(status == 404, "(" + status+ ") - Route should not be legal");
    });
    api_test(rest_root_url + "/session", "PUT", {}, function(status, result) {
        ok(result == null, "Non-null result");
        ok(status == 404, "(" + status+ ") - Route should not be legal");
    });
    api_test_with_credentials(rest_root_url + "/session", "POST", {}, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status+ ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/session", "POST", {}, "Anonymous", "", function(status, result) {
        ok(status != 401, "(" + status+ ") - Request should've been authenticated");
        ok(result.match(/^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}_[A-Za-z]{2}_\w+[A-Fa-f0-9]{12}$/g) != null, "Expected session id pattern");
    });
    api_test_with_credentials(rest_root_url + "/session", "POST", {}, "Administrator", "admin", function(status, result) {
        ok(status != 401, "(" + status+ ") - Request should've been authenticated");
        ok(result.match(/^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}_[A-Za-z]{2}_\w+[A-Fa-f0-9]{12}$/g) != null, "Expected session id pattern");
    });
});

module("Resource Service - Library", {
    setup: function() {
        /*
        api_test_with_credentials(rest_root_url + "/session", "POST", {}, "Anonymous", "", function(status, result) {
            ok(status != 401, "(" + status+ ") - Request should've been authenticated");
            this.anonymousSessionId = result;
        });
        api_test_with_credentials(rest_root_url + "/session", "POST", {}, "Administrator", "admin", function(status, result) {
            ok(status != 401, "(" + status+ ") - Request should've been authenticated");
            this.adminSessionId = result;
        });*/
    },
    teardown: function() {
        /*
        delete this.anonymousSessionId;
        delete this.adminSessionId;
        */
    }
});
test("Enumerate Resources", function() {
    api_test(rest_root_url + "/library/Samples/", "GET", null, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test(rest_root_url + "/library/Samples/list", "POST", {}, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test(rest_root_url + "/library/Samples/list", "POST", { depth: -1, type: "FeatureSource" }, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/list", "GET", null, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/list", "GET", { depth: -1, type: "FeatureSource" }, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/list", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got a list back");
    });
    api_test_anon(rest_root_url + "/library/Samples/list", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got a list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/list", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got a list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/list", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got a list back");
    });
});
test("Get Resource Content", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", null, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", { depth: -1, type: "FeatureSource" }, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource content back");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource content back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource content back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/content", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource content back");
    });
});
test("Get Resource Header", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", null, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", "depth=-1&type=FeatureSource", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource header back");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", "depth=-1&type=LayerDefinition", function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource header back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource header back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/header", "GET", "depth=-1&type=LayerDefinition", function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource header back");
    });
});
test("Enumerate Resource Data", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "POST", null, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "PUT", null, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", null, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", "depth=-1&type=LayerDefinition", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", "depth=-1&type=LayerDefinition", function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/data", "GET", "depth=-1&type=LayerDefinition", function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
});
test("Enumerate Resource References", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", null, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", { depth: -1, type: "FeatureSource" }, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/references", "GET", { depth: -1, type: "LayerDefinition" }, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've got resource data list back");
    });
});
/*
test("Set/Get/Delete resource", function() {
    var xml = '<?xml version="1.0" encoding="UTF-8"?><FeatureSource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="FeatureSource-1.0.0.xsd"><Provider>OSGeo.SDF</Provider><Parameter><Name>File</Name><Value>%MG_DATA_FILE_PATH%Empty.sdf</Value></Parameter></FeatureSource>';
    api_test_with_credentials(rest_root_url + "/library/UnitTests/Empty.FeatureSource/content", "POST", {}, "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/UnitTests/Empty.FeatureSource/content", "POST", xml, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous shouldn't be able to save to library repo");
    });
    api_test_admin(rest_root_url + "/library/UnitTests/Empty.FeatureSource/content", "POST", xml, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've saved resource");
    });
    api_test_anon(rest_root_url + "/library/UnitTests/Empty.FeatureSource/content", "DELETE", xml, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous shouldn't be able to delete library resources");
    });
    api_test_admin(rest_root_url + "/library/UnitTests/Empty.FeatureSource/content", "DELETE", xml, function(status, result) {
        ok(status == 200, "(" + status + ") - Should've deleted resource");
    });
});
*/

module("Resource Service - Session", {
    setup: function() {

    },
    teardown: function() {

    }
});
test("Bad Routes", function() {
    api_test(rest_root_url + "/library/UnitTests/", "GET", null, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test(rest_root_url + "/library/UnitTests/list", "POST", {}, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
    api_test(rest_root_url + "/library/UnitTests/list", "POST", { depth: -1, type: "FeatureSource" }, function(status, result) {
        ok(status == 404, "(" + status + ") - Route should not be legal");
    });
});

module("Feature Service - Library", {
    setup: function() {

    },
    teardown: function() {

    }
});
test("Get Spatial Contexts", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/spatialcontexts", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/spatialcontexts", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/spatialcontexts", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Get Schemas", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schemas", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schemas", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schemas", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Get Classes - SHP_Schema", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/classes", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/classes", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/classes", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Get Class Definition - SHP_Schema:Parcels", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/class/Parcels", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/class/Parcels", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/schema/SHP_Schema/class/Parcels", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Get FDO Providers", function() {
    api_test(rest_root_url + "/providers", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/providers", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/providers", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("SDF Provider Capabilities", function() {
    api_test(rest_root_url + "/providers/OSGeo.SDF/capabilities", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/providers/OSGeo.SDF/capabilities", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/providers/OSGeo.SDF/capabilities", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
//List Data Stores test case excluded as that requires a SQL Server feature source set up. Can always manually verify
test("SDF Provider - Connection Property Values for ReadOnly", function() {
    api_test(rest_root_url + "/providers/OSGeo.SDF/connectvalues/ReadOnly", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/providers/OSGeo.SDF/connectvalues/ReadOnly", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/providers/OSGeo.SDF/connectvalues/ReadOnly", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Select 100 Parcels", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=100", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=100", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=100", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Parcels owned by SCHMITT", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "filter=RNAME LIKE 'SCHMITT%25'", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "filter=RNAME LIKE 'SCHMITT%25'", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "filter=RNAME LIKE 'SCHMITT%25'", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Select 100 Parcels with projected property list", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "properties=Autogenerated_SDF_ID,RNAME,SHPGEOM&maxfeatures=100", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "properties=Autogenerated_SDF_ID,RNAME,SHPGEOM&maxfeatures=100", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "properties=Autogenerated_SDF_ID,RNAME,SHPGEOM&maxfeatures=100", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Select 100 Parcels (xformed to WGS84.PseudoMercator)", function() {
    api_test_with_credentials(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=1000&transformto=WGS84.PseudoMercator", "Foo", "Bar", function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=100&transformto=WGS84.PseudoMercator", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/Data/Parcels.FeatureSource/features/SHP_Schema/Parcels", "GET", "maxfeatures=100&transformto=WGS84.PseudoMercator", function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});

module("Feature Service - Session", {
    setup: function() {

    },
    teardown: function() {

    }
});

module("Site Service", {
    setup: function() {

    },
    teardown: function() {

    }
});
test("Get Status", function() {
    api_test(rest_root_url + "/site/status", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/status", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/site/status", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Get Version", function() {
    api_test(rest_root_url + "/site/version", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/version", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/site/version", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List Groups", function() {
    api_test(rest_root_url + "/site/groups", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/groups", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous access denied");
    });
    api_test_admin(rest_root_url + "/site/groups", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List Roles - Anonymous", function() {
    api_test(rest_root_url + "/site/user/Anonymous/roles", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/user/Anonymous/roles", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/site/user/Anonymous/roles", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List Roles - Administrator", function() {
    api_test(rest_root_url + "/site/user/Administrator/roles", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/user/Administrator/roles", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous access denied");
    });
    api_test_admin(rest_root_url + "/site/user/Administrator/roles", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List Groups - Anonymous", function() {
    api_test(rest_root_url + "/site/user/Anonymous/groups", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/user/Anonymous/groups", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/site/user/Anonymous/groups", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List Groups - Administrator", function() {
    api_test(rest_root_url + "/site/user/Administrator/groups", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/user/Administrator/groups", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous access denied");
    });
    api_test_admin(rest_root_url + "/site/user/Administrator/groups", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("List users under everyone", function() {
    api_test(rest_root_url + "/site/groups/Everyone/users", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/site/groups/Everyone/users", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Anonymous access denied");
    });
    api_test_admin(rest_root_url + "/site/groups/Everyone/users", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});

module("Tile Service", {
    setup: function() {

    },
    teardown: function() {

    }
});
test("GetTile", function() {
    api_test(rest_root_url + "/library/Samples/Sheboygan/MapsTiled/Sheboygan.MapDefinition/basetileimage/Base Layer Group/6/1,0", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_anon(rest_root_url + "/library/Samples/Sheboygan/MapsTiled/Sheboygan.MapDefinition/basetileimage/Base Layer Group/6/1,0", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/library/Samples/Sheboygan/MapsTiled/Sheboygan.MapDefinition/basetileimage/Base Layer Group/6/1,0", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});

module("Coordinate System", {
    setup: function() {

    },
    teardown: function() {

    }
});
test("Enum categories", function() {
    api_test(rest_root_url + "/coordsys/categories", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/categories", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/coordsys/categories", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("Enum categories - Australia", function() {
    api_test(rest_root_url + "/coordsys/category/Australia", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/category/Australia", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
    api_test_admin(rest_root_url + "/coordsys/category/Australia", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
    });
});
test("EPSG for LL84", function() {
    api_test(rest_root_url + "/coordsys/mentor/LL84/epsg", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/mentor/LL84/epsg", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == "4326", "Expected EPSG of 4326. Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/mentor/LL84/epsg", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == "4326", "Expected EPSG of 4326. Got: " + result);
    });
});
test("WKT for LL84", function() {
    var expect = "GEOGCS[\"LL84\",DATUM[\"WGS84\",SPHEROID[\"WGS84\",6378137.000,298.25722293]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.01745329251994]]";
    api_test(rest_root_url + "/coordsys/mentor/LL84/wkt", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/mentor/LL84/wkt", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected WKT of " + expect + ". Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/mentor/LL84/wkt", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected WKT of " + expect + ". Got: " + result);
    });
});
test("Mentor code for EPSG:4326", function() {
    var expect = "LL84";
    api_test(rest_root_url + "/coordsys/epsg/4326/mentor", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/epsg/4326/mentor", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected code of " + expect + ". Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/epsg/4326/mentor", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected code of " + expect + ". Got: " + result);
    });
});
test("WKT for EPSG:4326", function() {
    var expect = "GEOGCS[\"LL84\",DATUM[\"WGS84\",SPHEROID[\"WGS84\",6378137.000,298.25722293]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.01745329251994]]";
    api_test(rest_root_url + "/coordsys/epsg/4326/wkt", "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/epsg/4326/wkt", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected WKT of " + expect + ". Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/epsg/4326/wkt", "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected WKT of " + expect + ". Got: " + result);
    });
});
test("WKT to mentor", function() {
    var wkt = "GEOGCS[\"LL84\",DATUM[\"WGS84\",SPHEROID[\"WGS84\",6378137.000,298.25722293]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.01745329251994]]";
    var expect = "LL84";
    api_test(rest_root_url + "/coordsys/tomentor/" + wkt, "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/tomentor/" + wkt, "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected code of " + expect + ". Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/tomentor/" + wkt, "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected code of " + expect + ". Got: " + result);
    });
});
test("WKT to epsg", function() {
    var wkt = "GEOGCS[\"LL84\",DATUM[\"WGS84\",SPHEROID[\"WGS84\",6378137.000,298.25722293]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.01745329251994]]";
    var expect = "4326";
    api_test(rest_root_url + "/coordsys/toepsg/" + wkt, "GET", null, function(status, result) {
        ok(status == 401, "(" + status + ") - Request should've required authentication");
    });
    api_test_anon(rest_root_url + "/coordsys/toepsg/" + wkt, "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected EPSG of " + expect + ". Got: " + result);
    });
    api_test_admin(rest_root_url + "/coordsys/toepsg/" + wkt, "GET", null, function(status, result) {
        ok(status == 200, "(" + status + ") - Response should've been ok");
        ok(result == expect, "Expected EPSG of " + expect + ". Got: " + result);
    });
});