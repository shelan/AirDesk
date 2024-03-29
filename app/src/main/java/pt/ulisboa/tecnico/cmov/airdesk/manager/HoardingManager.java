package pt.ulisboa.tecnico.cmov.airdesk.manager;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cmov.airdesk.AWSTasks;
import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
import pt.ulisboa.tecnico.cmov.airdesk.storage.StorageManager;

/**
 * Created by shelan on 5/1/15.
 */
public class HoardingManager {

    ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void scheduleCleaningTask() {
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {

                        System.out.println("=====CLaming spacee ========== !!!!!!!!!");
                        claimSpace();
                    }
                }, 0, 30, TimeUnit.SECONDS);
    }

    public boolean isCleaningNeeded() {
        if(AWSTasks.offline){
            return false;
        }
        double usage = getCurrentUsage();
        double free = new WorkspaceManager().getMaximumDeviceSpace();

        //Temp since we are returning a fixed 5mb as workspace size.
        // free = 5mb
        double availableSpace = free - usage;

        //if 80% is filled we perform cleaning
        if (availableSpace / (usage) < 0.2) return true;
        return false;
    }


    private double getCurrentUsage() {

        Context appContext = AirDeskApp.s_applicationContext;
        File appContextDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);

        return dirSize(appContextDir) / Constants.BYTES_PER_MB;

    }

    private double dirSize(File dir) {

        if (!dir.exists()) {
            return 0;
        }
        long result = 0;
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            // Recursive call if it's a directory
            if (fileList[i].isDirectory()) {
                result += dirSize(fileList[i]);
            } else {
                // Sum the file size in bytes
                result += fileList[i].length();
            }
        }
        return result; // return the file size
    }

    public void claimSpace() {

        if(AWSTasks.offline){
            return;
        }

        double usage = getCurrentUsage();
        double free = new WorkspaceManager().getMaximumDeviceSpace();

        //Temp since we are returning a fixed 5mb as workspace size.
        // free = 5mb

        double availableSpace = free - usage;

       /* System.out.println(" Used space :" + usage);
        System.out.println(" Free space :" + availableSpace);*/


        double amountToBeFreed = (usage - (usage + availableSpace) * 0.7);
        double totalSizeOfDeletingFiles = 0.0;
        ArrayList<String> pathListToBeDeleted = new ArrayList<>();

        if (amountToBeFreed > 0) {
            TreeMap<Long, String> sortedMap = sortByAccessTime(new StorageManager().getAccessMap());
            for (Map.Entry<Long, String> entry : sortedMap.entrySet()) {
                String filePath = entry.getValue();
                totalSizeOfDeletingFiles += Float.valueOf(new File(filePath).length()) / Constants.BYTES_PER_MB;
                pathListToBeDeleted.add(filePath);
                if (totalSizeOfDeletingFiles > amountToBeFreed) {
                    deleteFiles(pathListToBeDeleted);
                    System.out.println("Found " + pathListToBeDeleted.size() + "No of files to delete");
                    System.out.println("Files deleted =====>");
                    for (String s : pathListToBeDeleted) {
                        System.out.println(s);
                    }
                    return;
                }
            }
        }


    }

    private TreeMap<Long, String> sortByAccessTime(HashMap<String, Long> map) {
        {
            TreeMap sortedMap = new TreeMap();

            for (Map.Entry<String, Long> entry : map.entrySet()) {
                String key = entry.getKey();
                Long value = entry.getValue();
                sortedMap.put(value, key);
            }
            return sortedMap;
        }
    }

    private TreeMap<Long, String> sortBySize(HashMap<String, Long> map) {
        TreeMap sortedMap = new TreeMap();

        for (Map.Entry<String, Long> entry : map.entrySet()) {
            String filePath = entry.getKey();
            File file = new File(filePath);

            sortedMap.put(file.length(), filePath);
        }
        return sortedMap;
    }

    public void performHoarding() {
        if (isCleaningNeeded()) {
            scheduler.schedule
                    (new Runnable() {
                        public void run() {
                            //System.out.println("=====CLaming spacee ========== !!!!!!!!!");
                            claimSpace();
                        }
                    }, 10, TimeUnit.MICROSECONDS);
        }
    }

    public void deleteFiles(ArrayList<String> toBeDeletedList) {
        for (String filePath : toBeDeletedList) {
            new File(filePath).delete();
        }
    }


}
