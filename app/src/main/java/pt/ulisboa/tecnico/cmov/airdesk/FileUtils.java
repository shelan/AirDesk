package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;

public class FileUtils {

    private static Context appContext = AirDeskApp.s_applicationContext;

    public static FileInputStream readFile(String path) throws IOException {
        if (new File(path).exists()) {
            FileInputStream inputStream = new FileInputStream(path);
            return inputStream;
        }
        return null;
    }

    public static void writeToFile(String path, String content) throws IOException {

        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream outputStream = new FileOutputStream(path);
        outputStream.write(content.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    public static boolean deleteFolder(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }

    /**
     * @param baseDir
     * @param subDirName can be one folder or a set like folder1/folder2
     * @return
     * @throws Exception
     */
    public static File createFolder(File baseDir, String subDirName) throws Exception {
        File createdDir = new File(baseDir, subDirName);
        System.out.println("path------> " + createdDir.getAbsolutePath());
        if (createdDir.exists())
            throw new Exception("Folder already exists. " + createdDir.getAbsolutePath());

        boolean isSuccessful = createdDir.mkdirs();
        if (isSuccessful)
            return createdDir;
        else
            return null;
    }

    public static boolean createWSFolder(String workspaceName, String userName) {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir.getAbsolutePath() + "/" + workspaceName);//create workspace inside WS dir
        boolean status = workspaceDir.mkdir();

        try {
            AWSTasks.getInstance().createFolder(getFileNameForUserId(userName),
                    workspaceName);
        } catch (ExecutionException e) {
            Log.e("AWS_ERROR", "Error while executing", e);
        } catch (InterruptedException e) {
            Log.e("AWS_ERROR", "Interrupted", e);
        }

        System.out.println("child folder created " + status);
        return true;
    }

   /* public static void createFolderForOwnedWorkSpaces() {//all owned workspaces will be here
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        System.out.println("ws path" + parentDir.getAbsolutePath());
    }
*/

    public static boolean deleteOwnedWorkspaceFolder(String workspaceName, String userName) {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir.getAbsolutePath() + "/" + workspaceName);
        File[] files = workspaceDir.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }


        try {
            AWSTasks.getInstance().deleteFolder(getFileNameForUserId(userName), workspaceName);


        } catch (ExecutionException e) {
            Log.e("AWS_ERROR", "Error while executing", e);
        } catch (InterruptedException e) {
            Log.e("AWS_ERROR", "Interrupted", e);
        }


        return workspaceDir.delete();
    }

    public static double folderSize(String workspaceName) {
        double lengthInKB = getCurrentFolderSize(workspaceName);
        return lengthInKB;
    }

    public static boolean deleteForeignWorkspaceFolder(String workspaceName, String userId) {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.FOREIGN_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        String fileName = parentDir.getAbsolutePath() + "/" + userId + "/" + workspaceName;
        File workspaceDir = new File(fileName);
        File[] files = workspaceDir.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        return workspaceDir.delete();
    }

    public static double getCurrentFolderSize(String workspaceName) {
        Context appContext = AirDeskApp.s_applicationContext;
        File parentDir = appContext.getDir(Constants.OWNED_WORKSPACE_DIR, appContext.MODE_PRIVATE);
        File workspaceDir = new File(parentDir.getAbsolutePath() + "/" + workspaceName);
        System.out.println("new work dir" + workspaceDir.getAbsolutePath());
        long length = 0;
        File[] files = workspaceDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getCurrentFolderSize(workspaceDir.getPath());
            }
        }
        double lengthInKB = length / (double) Constants.BYTES_PER_MB;
        return lengthInKB;
    }


    public static String getFileNameForUserId(String userId) {
        userId = userId.replace("@", "__");
        userId = userId.replace(".", "__");
        return userId;
    }

    /**
     * @param inputStream
     * @return String content of the input stream return null if the file is not available to detect
     * missing files.
     */
    public static StringBuffer getStringBuffer(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputStreamReader);

        try {
            String readString = buffreader.readLine();
            while (readString != null) {
                stringBuffer.append(readString);
                readString = buffreader.readLine();
            }
            inputStreamReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return stringBuffer;
    }
}
