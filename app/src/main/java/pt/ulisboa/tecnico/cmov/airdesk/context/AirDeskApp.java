package pt.ulisboa.tecnico.cmov.airdesk.context;

import android.app.Application;
import android.content.Context;


public class AirDeskApp extends Application {
    public static Context s_applicationContext = null;
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("on create");
     s_applicationContext = getApplicationContext();
    }
}
