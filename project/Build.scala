import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "mapguide4j"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        // Add your project dependencies here,
        //"com.jayway.restassured" % "rest-assured" % "1.7" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
        // Add your own project settings here
    )

}
