import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class Credentials {
    private static AWSCredentialsProvider credentialsProvider;

    public static void setCredentials() {
        credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
    }

    public static AWSCredentialsProvider getCredentials() {
        return credentialsProvider;
    }
}