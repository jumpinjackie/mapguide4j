package controllers;

import actions.*;
import play.*;
import play.mvc.*;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;

/**
 * Base class of all controllers requiring authentication. All operations are verified for any of the following
 *
 *  1. The "session" parameter containing the MapGuide session id
 *  2. A http authentication header containing the MapGuide credentials
 *  3. A "username" with an optional "password" parameter specify the MapGuide credentials
 *
 * Any request not containing at least one of the above is rejected with a 401 unauthorized
 */
@MgCheckSession
public abstract class MgAbstractAuthenticatedController extends MgAbstractController {

}