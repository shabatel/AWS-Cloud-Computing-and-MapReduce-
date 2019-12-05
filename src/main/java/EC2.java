import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.Base64;

import java.util.LinkedList;
import java.util.List;

public class EC2 {

    private static AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
    private static AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-west-2")
            .build();

    private static List<Instance> runningInstances = new LinkedList<Instance>();

    public static List<Instance> getActiveInstances() {
        DescribeInstancesResult result = ec2.describeInstances();
        for (int i = 0; i < result.getReservations().size(); i++) {
            List<Instance> reservationInstances = result.getReservations().get(i).getInstances();
            for (Instance inst : reservationInstances) {
                if ((inst.getState().getName().equals("running") ||
                        inst.getState().getName().equals("pending"))
                        && !runningInstances.contains(inst)) {
                    runningInstances.add(inst);
                }

            }
        }
        return runningInstances;
    }
    public static void runMachines(){

    }
}
