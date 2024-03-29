package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shelan on 4/25/15.
 */
public class AWSUtils {

    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;

    public static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context, // Context
                    "us-east-1:8614ddd2-36ea-45b0-9639-2a03a9be3e88", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
        }
        return sCredProvider;
    }

    public static String getPrefix(Context context) {
        return getCredProvider(context).getIdentityId() + "/";
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context));
        }
        return sS3Client;
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static boolean doesBucketExist() {
        return sS3Client.doesBucketExist(Constants.BUCKET_NAME.toLowerCase(Locale.US));
    }

    public static void createBucket() {
        sS3Client.createBucket(Constants.BUCKET_NAME.toLowerCase(Locale.US));
    }

    public static void deleteBucket() {
        String name = Constants.BUCKET_NAME.toLowerCase(Locale.US);
        List<S3ObjectSummary> objData = sS3Client.listObjects(name).getObjectSummaries();
        if (objData.size() > 0) {
            DeleteObjectsRequest emptyBucket = new DeleteObjectsRequest(name);
            List<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<DeleteObjectsRequest.KeyVersion>();
            for (S3ObjectSummary summary : objData) {
                keyList.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
            }
            emptyBucket.withKeys(keyList);
            sS3Client.deleteObjects(emptyBucket);
        }
        sS3Client.deleteBucket(name);
    }
}
