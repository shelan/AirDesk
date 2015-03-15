package pt.ulisboa.tecnico.cmov.airdesk.manager;
import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;
/**
 * Created by Chathuri on 3/14/2015.
 */
public class MetadataManager {
    public String readFromInternalFile(String fileName)
    {
        FileInputStream fis = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fis = appContext.openFileInput(fileName);
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
    public void saveToInternalFile(String jsonString,String fileName)
    {
        FileOutputStream fos = null;
        try {
            Context appContext = AirDeskApp.s_applicationContext;
            fos = appContext.openFileOutput(fileName
                    ,Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
