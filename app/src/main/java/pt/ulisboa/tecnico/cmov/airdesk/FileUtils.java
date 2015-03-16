package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;

/**
 * Created by ashansa on 3/15/15.
 */
public class FileUtils {

    private static Context appContext = AirDeskApp.s_applicationContext;

    public static FileInputStream readFile(String path) throws IOException {
        FileInputStream inputStream = new FileInputStream(path);
        inputStream.close();
        return inputStream;
    }

    public static void writeToFile(String path, FileInputStream inputStream) throws IOException {

        OutputStream outputStream = new FileOutputStream(path);
        int c;
        while ((c = inputStream.read()) != -1) {
            outputStream.write(c);
        }
        inputStream.close();
        outputStream.close();
    }

    public static void deleteFolder(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     *
     * @param baseDir
     * @param subDirName can be one folder or a set like folder1/folder2
     * @return
     * @throws Exception
     */
    public static boolean createFolder(File baseDir, String subDirName) throws Exception {
        File workspaceDir = new File(baseDir, subDirName);
        System.out.println("path------> " + workspaceDir.getAbsolutePath());
        if(workspaceDir.exists())
            throw new Exception("Folder already exists. " + workspaceDir.getAbsolutePath());

        return workspaceDir.mkdirs();
    }

    public static boolean createFolder(String workspaceName){
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir=appContext.getDir(Constants.WS_DIR,appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir, workspaceName);//create workspace inside WS dir
        System.out.println("parent "+parentDir.getAbsolutePath());
        System.out.println("child"+workspaceDir.getAbsolutePath());
        workspaceDir.mkdir();
        return true;
    }

    public static void createFolderForOwnedWorkSpaces(){//all owned workspaces will be here
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir=appContext.getDir(Constants.WS_DIR, appContext.MODE_PRIVATE);
        System.out.println("ws path"+parentDir.getAbsolutePath());
    }


    public static double folderSize(String workspaceName) {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir=appContext.getDir(Constants.WS_DIR,appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir.getAbsolutePath()+"/"+workspaceName);
        System.out.println("new work dir"+workspaceDir.getAbsolutePath());
        long length = 0;

        for (File file : workspaceDir.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(workspaceDir.getPath());
        }
        System.out.println("folder size "+length);
        double lengthInKB=length/Constants.bytesPerKb;
        return lengthInKB;
    }
}
