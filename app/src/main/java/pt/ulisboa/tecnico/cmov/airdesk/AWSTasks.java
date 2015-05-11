package pt.ulisboa.tecnico.cmov.airdesk;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmov.airdesk.context.AirDeskApp;

/**
 * Created by shelan on 4/26/15.
 */
public class AWSTasks {

    static AWSTasks taskInstance;
    CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
            AWSUtils.getCredProvider(AirDeskApp.s_applicationContext);

    protected AWSTasks() {

    }

    public static AWSTasks getInstance() {

        if (taskInstance == null) {
            taskInstance = new AWSTasks();
        }
        return taskInstance;
    }

    public boolean createFolder(String parentName, String folderName) throws ExecutionException, InterruptedException {
       /* return new FolderCreateAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                parentName, folderName).get();*/
        return true;
    }

    public boolean deleteFolder(String parentName, String folderName) throws ExecutionException, InterruptedException {
        /*return new FolderDeleteAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                parentName, folderName).get();*/
        return true;
    }

    public boolean createFile(String parent, String folderName, String fileName, String content) throws ExecutionException, InterruptedException {
        /*return new FileCreateAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                parent, folderName, fileName, content).get();*/
        return true;
    }

    public boolean deleteFile(String parent, String folderName, String fileName) throws ExecutionException, InterruptedException {
        /*return new FileDeleteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                parent, folderName, fileName).get();*/
        return true;
    }

    public StringBuffer getFile(String parent, String folderName, String fileName) throws ExecutionException, InterruptedException {
        /*AsyncTask getTask = new FileDownload().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                parent, folderName, fileName);
        return (StringBuffer) getTask.get();*/
        return new StringBuffer();
    }


    private static class FolderCreateAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Create metadata for your folder & set content-length to 0

            String parentFolder = params[0];
            String workspaceName = params[1];

            ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentLength(0);

            // Create empty content

            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

            // Create a PutObjectRequest passing the foldername suffixed by /

            PutObjectRequest putObjectRequest =

                    new PutObjectRequest(Constants.BUCKET_NAME, parentFolder + Constants.FOLDER_SEP +
                            workspaceName + Constants.FOLDER_SEP,
                            emptyContent, metadata);

            AWSUtils.getS3Client(AirDeskApp.s_applicationContext).putObject(putObjectRequest);

            //transferManager.upload(putObjectRequest);
            return true;
        }
    }

    private static class FileCreateAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Create metadata for your folder & set content-length to 0

            ObjectMetadata metadata = new ObjectMetadata();

            String parentFolder = params[0];
            String workspaceName = params[1];
            String fileName = params[2];
            String content = params[3];

            metadata.setContentLength(content.getBytes().length);
            metadata.setHeader("workspace", fileName);

            // Create empty content

            InputStream fileContent = new ByteArrayInputStream(content.getBytes());

            // Create a PutObjectRequest passing the foldername suffixed by /

            System.out.println("File " + parentFolder + Constants.FOLDER_SEP
                    + workspaceName +
                    Constants.FOLDER_SEP + fileName);

            PutObjectRequest putObjectRequest =

                    new PutObjectRequest(Constants.BUCKET_NAME, parentFolder + Constants.FOLDER_SEP
                            + workspaceName +
                            Constants.FOLDER_SEP + fileName,
                            fileContent, metadata);

            AWSUtils.getS3Client(AirDeskApp.s_applicationContext).putObject(putObjectRequest);

            //transferManager.upload(putObjectRequest);
            return true;
        }
    }

    private static class FolderDeleteAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            AmazonS3Client s3Client = AWSUtils.getS3Client(AirDeskApp.s_applicationContext);

            String parentFolder = params[0];
            String workspaceName = params[1];

            // Create metadata for your folder & set content-length to 0
            List<S3ObjectSummary> objData = s3Client.listObjects(Constants.BUCKET_NAME,
                    parentFolder + Constants.FOLDER_SEP + workspaceName + Constants.FOLDER_SEP)
                    .getObjectSummaries();
            if (objData.size() > 0) {
                DeleteObjectsRequest emptyBucket = new DeleteObjectsRequest(Constants.BUCKET_NAME);
                List<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
                for (S3ObjectSummary summary : objData) {
                    keyList.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
                }
                emptyBucket.withKeys(keyList);
                s3Client.deleteObjects(emptyBucket);
                // s3Client.deleteObject(Constants.BUCKET_NAME +);
            }

           /* AWSUtils.getS3Client(AirDeskApp.s_applicationContext).deleteObject(Constants.BUCKET_NAME,
                    parentFolder + Constants.FOLDER_SEP + workspaceName);*/

            //transferManager.upload(putObjectRequest);
            return true;
        }
    }

    private static class FileDeleteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // Create metadata for your folder & set content-length to 0

            String parentFolder = params[0];
            String workspaceName = params[1];
            String fileName = params[2];

            AWSUtils.getS3Client(AirDeskApp.s_applicationContext).deleteObject(Constants.BUCKET_NAME,
                    parentFolder + Constants.FOLDER_SEP + workspaceName + Constants.FOLDER_SEP +
                            fileName);

            //transferManager.upload(putObjectRequest);
            return true;
        }
    }

    private static class FileDownload extends AsyncTask<String, Boolean, StringBuffer> {

        @Override
        protected StringBuffer doInBackground(String... params) {
            // Create metadata for your folder & set content-length to 0

            String parentFolder = params[0];
            String workspaceName = params[1];
            String fileName = params[2];

            S3Object s3Object = AWSUtils.getS3Client(AirDeskApp.s_applicationContext).getObject(Constants.BUCKET_NAME,
                    parentFolder + Constants.FOLDER_SEP + workspaceName + Constants.FOLDER_SEP +
                            fileName);

            //transferManager.upload(putObjectRequest);
            return FileUtils.getStringBuffer(s3Object.getObjectContent());
        }
    }




}
