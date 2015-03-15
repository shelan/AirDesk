package pt.ulisboa.tecnico.cmov.airdesk.storage;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.Utils;

/**
 * Created by Chathuri on 3/14/2015.
 */
public class StorageManager {

    public String readFromInternalFile()
    {
        FileInputStream fis = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fis = appContext.openFileInput("user.jons");
            String jsonString = readStreamAsString(fis);
            return jsonString;
        }
        catch(IOException x)
        {
            return null;
        }

    }

    private String readStreamAsString(InputStream is)
            throws FileNotFoundException, IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            copy(is, baos);
            baos.close();
            return baos.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private void copy(InputStream reader, OutputStream writer)
            throws IOException
    {
        byte byteArray[] = new byte[4092];
        while(true) {
            int numOfBytesRead = reader.read(byteArray,0,4092);
            if (numOfBytesRead == -1) {
                break;
            }
            writer.write(byteArray,0,numOfBytesRead);
        }
        return;
    }
    public void saveToInternalFile(String ins)
    {
        FileOutputStream fos = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fos = appContext.openFileOutput("user.jons"
                    ,Context.MODE_PRIVATE);
            fos.write(ins.getBytes());
            fos.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private static HashMap<String, Boolean> fileWriteLock = new HashMap<String, Boolean>();
    //TODO read from property file
    private String workspaceDir = Constants.WS_DIR;

    public boolean createDataFile(String workspaceName, String fileName) throws IOException {
        String path =  workspaceDir+ workspaceName + "/" + fileName;
        return new File(path).createNewFile();
    }

    public synchronized FileInputStream getDataFile(String workspaceName, String fileName, boolean writeMode) throws WriteLockedException, IOException {
        String path =  workspaceDir+ workspaceName + "/" + fileName;

        if(writeMode) {
            if(isWriteLocked(workspaceName, fileName))
                throw new WriteLockedException("File is already write locked. Cannot open in write mode");
            else {
                addWriteLock(workspaceName, fileName);
                return Utils.readFile(path);
            }
        }

        return Utils.readFile(path);
    }

    public void updateDataFile(String workspaceName, String fileName, FileInputStream inputStream) throws IOException {
        String path =  workspaceDir+ workspaceName + "/" + fileName;
        Utils.writeToFile(path, inputStream);
    }

    public boolean deleteDataFile(String workspaceName, String fileName) throws IOException {
        String path =  workspaceDir+ workspaceName + "/" + fileName;
        return new File(path).delete();
    }

    private boolean isWriteLocked(String workspaceName, String fileName) {
        return fileWriteLock.get(workspaceName + "/" + fileName);
    }

    private void addWriteLock(String workspaceName, String fileName) {
        fileWriteLock.put(workspaceName + "/" + fileName, new Boolean(true));
    }

}
