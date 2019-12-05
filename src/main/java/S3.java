import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import java.io.File;

public class S3 {

//    private static AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    private static AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
    private static AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-east-1")
            .build();


//    String directoryName = args[0];

    public static final String bucketName = credentialsProvider.getCredentials().getAWSAccessKeyId().replace('A', 'a').
            replace('B', 'b').replace('C', 'c').replace('D', 'd').replace('E', 'e')
            .replace('F', 'f').replace('G', 'g').replace('H', 'h').replace('I', 'i')
            .replace('J', 'j').replace('K', 'k').replace('L', 'l').replace('M', 'm')
            .replace('N', 'n').replace('O', 'o').replace('P', 'p').replace('Q', 'q')
            .replace('R', 'r').replace('S', 's').replace('T', 't').replace('U', 'u')
            .replace('V', 'v').replace('W', 'w').replace('X', 'x').replace('Y', 'y')
            .replace('Z', 'z');
    String key = null;

    public static void createBucket() {
        System.out.println("Creating bucket " + bucketName + "\n");
        s3.createBucket(bucketName);
    }

    // upload file to bucket
    public static String uploadFile(File file) {
        System.out.println("Uploading a new file to bucket");
        String key = file.getName().replace('\\', '-').replace('/', '-').replace(':', '-');
        PutObjectRequest req = new PutObjectRequest(bucketName, key, file);
        req.setCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(req);
        return key;
    }

    //download file from bucket
    public static S3Object downloadFile(String key) {
        System.out.println("Downloading an object");
        return s3.getObject(new GetObjectRequest(bucketName, key));
    }

    // get file URL
    public static String getFileURL(String key) {
        return s3.getUrl(bucketName, key).toString();
    }

    // remove file from bucket
    public static void removeFile(String key) {
        s3.deleteObject(bucketName, key);
    }

    // list files in bucket (debugging)
    public static void listFiles(String bucketName) {
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName));
        System.out.println("Listing files in " + bucketName + ":");
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
        }
        System.out.println();
    }
}