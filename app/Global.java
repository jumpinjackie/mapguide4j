import java.io.File;
import play.*;
import org.osgeo.mapguide.*;

public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        File libDir = Play.application().getFile("lib/");
        System.setProperty("java.library.path", libDir.getPath() + ";" + System.getProperty("java.library.path"));
        Logger.info("java.library.path is currently: " + System.getProperty("java.library.path"));
        String webConfigPath = Play.application().configuration().getString("mapguide4j.webconfigpath");
        Logger.info("Initializing MapGuide Web Tier with: " + webConfigPath);
        MapGuideJavaApi.MgInitializeWebTier(webConfigPath);
        Logger.info("Application has started");
    }  

    @Override
    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }  
}