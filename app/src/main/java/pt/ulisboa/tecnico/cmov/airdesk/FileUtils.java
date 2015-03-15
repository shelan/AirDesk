package pt.ulisboa.tecnico.cmov.airdesk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ashansa on 3/15/15.
 */
public class FileUtils {

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

    public static boolean createFolderStructureOnForeignWSAddition(String path) throws Exception {
        boolean wsDirCreated = false;
        File workspace = new File(path);
        if(workspace.exists()) {
            throw new Exception("Foreign workspace already exists. " + path);
        }

        wsDirCreated = workspace.mkdirs();
        if(!wsDirCreated) {
            throw new Exception("Could not create foreign workspace at " + path);
        }
        return wsDirCreated;
    }

    public static boolean createFolder(String workspaceName){
        File dir = new File(Constants.WS_DIR+"/"+workspaceName);
        return dir.mkdir();
    }

    public static double folderSize(String directoryPath) {
        File folder = new File(directoryPath);
        long length = 0;
        for (File file : folder.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(folder.getPath());
        }
        double lengthInKB=length/Constants.bytesPerKb;
        return lengthInKB;
    }
}
