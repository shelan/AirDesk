package pt.ulisboa.tecnico.cmov.airdesk.storage;

import android.app.Application;
import android.content.Context;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class AirDeskApp extends Application {
    public static Context s_applicationContext = null;
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("on create");
     s_applicationContext = getApplicationContext();
    }
}
