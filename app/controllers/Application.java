package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
    public static Result index() {
        return redirect(routes.Application.landing());
    }
    public static Result landing() {
        return ok(main.render());
    }
}