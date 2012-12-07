package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

/**
 * Controller for the mapguide4j REST client test suite
 */
public class MgTestController extends Controller {
    public static Result index() {
        return ok(test.render());
    }
}