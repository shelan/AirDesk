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

    public static void createFolderStructureOnForeignWSAddition(String workspaceName, String ownerId, String[] fileNames) throws Exception {
        if (!new File(Constants.FOREIGN_WS_DIR).exists()) {
            throw new FileNotFoundException("No directory found: " + Constants.FOREIGN_WS_DIR);
        }

        String dirPath = Constants.FOREIGN_WS_DIR + "/" + ownerId;
        File dir = new File(dirPath);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String wsPath = dirPath + "/" + ownerId;
        boolean wsDirCreated = false;
        File workspace = new File(wsPath);
        if(workspace.exists()) {
            throw new Exception("Foreign workspace " + workspaceName + " of owner " + ownerId +
                    " already exists");
        }

        wsDirCreated = workspace.mkdirs();
        if(!wsDirCreated) {
            throw new Exception("Could not create foreign workspace " + workspaceName + " of owner " + ownerId);
        } else {
           /////TODO create metadata object
        }

    }
}
