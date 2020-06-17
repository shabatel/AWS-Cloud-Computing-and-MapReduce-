import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.Base64;

import java.util.LinkedList;
import java.util.List;

public class EC2 {

    private static AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
            .withCredentials(Credentials.getCredentials())
            .withRegion("us-east-1")
            .build();

    private static List<Instance> activeInstances = new LinkedList<Instance>();

    public static List<Instance> getActiveInstances() {
        DescribeInstancesResult result = ec2.describeInstances();
        for (int i = 0; i < result.getReservations().size(); i++) {
            List<Instance> reservationInstances = result.getReservations().get(i).getInstances();
            for (Instance inst : reservationInstances) {
                if ((inst.getState().getName().equals("running") ||
                        inst.getState().getName().equals("pending"))
                        && !activeInstances.contains(inst)) {
                    activeInstances.add(inst);
                }
            }
        }
        return activeInstances;
    }


    public static void runMachines(String key, String value){

        try {
            String imageId = "ami-00eb20669e0990cb4";
            String userData = "";
            String envScript = "#!/bin/bash\r\n" +
                    "sudo yum update -y\r\n" +
                    "curl -O https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_linux-x64_bin.tar.gz\r\n" +
                    "tar zxvf openjdk-11.0.1_linux-x64_bin.tar.gz\r\n" +
                    "sudo mv jdk-11.0.1 /usr/local/\r\n" +
                    "export JAVA_HOME=/usr/local/jdk-11.0.1\r\n" +
                    "export PATH=$JAVA_HOME/bin:$PATH\r\n" +
                    "sudo yum install aws-cli -y\r\n";

            if(key.equals("manager")){
                System.out.println("starting Manager EC2");
                String managerJar= "curl -O https://jarbucketholderofec2.s3.amazonaws.com/manager.jar\r\n";
//                 //Image with java, maven, the jar file of the manager
                userData = envScript + managerJar + "java -jar /manager.jar Manager\r\n";
            }
            else if(key.equals("worker")){
                System.out.println("starting worker EC2");
                String workerJar= "curl -O https://jarbucketholderofec2.s3.amazonaws.com/worker.jar\r\n" +
                        "curl -O https://jarbucketholderofec2.s3.amazonaws.com/ejml-0.23.jar\r\n" +
                        "curl -O https://jarbucketholderofec2.s3.amazonaws.com/jollyday-0.4.7.jar\r\n" +
                        "curl -O https://jarbucketholderofec2.s3.amazonaws.com/stanford-corenlp-3.3.0.jar\r\n" +
                        "curl -O https://jarbucketholderofec2.s3.amazonaws.com/stanford-corenlp-3.3.0-models.jar\r\n";
                userData = envScript + workerJar + "java -cp .:worker.jar:stanford-corenlp-3.3.0.jar:" +
                        "stanford-corenlp-3.3.0-models.jar:ejml-0.23.jar:jollyday-0.4.7.jar -Xmx1g Worker\r\n";
            }

            RunInstancesRequest request = new RunInstancesRequest(imageId, 1, 1);
            String encoded = Base64.encodeAsString(userData.getBytes());
            request.withUserData(encoded)
                    .withIamInstanceProfile(new IamInstanceProfileSpecification().withName("NE"))
                    .withSecurityGroupIds("sg-0aca71c6b04078880")
                    .setKeyName("Admin");
            request.setInstanceType(InstanceType.T2Small.toString());

            request.setUserData(Base64.encodeAsString(userData.getBytes()));

            Tag t = new Tag(key, value);
            List<Tag> tags = new LinkedList<Tag>();
            tags.add(t);
            List<TagSpecification> specifications = new LinkedList<TagSpecification>();
            TagSpecification tagspec = new TagSpecification();
            tagspec.setTags(tags);
            tagspec.setResourceType("instance");

            specifications.add(tagspec);

            request.setTagSpecifications(specifications);
            ec2.runInstances(request);
            activeInstances = getActiveInstances();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Response Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
    }


    private static boolean closeMachinesByTag(String tag){

        List <String> machinesIdByTag = new LinkedList<String>();

        for (Instance machine : activeInstances){
            if(machine.getTags().get(0).getKey().equals(tag)) {
                machinesIdByTag.add(machine.getInstanceId());
            }
        }

        System.out.println("running instances:");
        for (Instance inst : activeInstances){
            System.out.print("<" + inst.getTags().get(0) +", "+inst.getInstanceId()+ "> ");
        }
        System.out.println("machinesIdByTag: "+ machinesIdByTag);
        for (String id : machinesIdByTag){
            System.out.print("<" + id +"> ");
        }
        if (machinesIdByTag.size() > 0) {
            return ec2.terminateInstances(new TerminateInstancesRequest(machinesIdByTag)) != null;
        }
        return true;
    }

    public static boolean closeManager(){
        System.out.println("Trying to close manager");
        if(closeMachinesByTag("manager")){
            System.out.println("\nManager closed!");
            return true;
        }
        System.out.println("Closing manager failed!");
        return false;
    }

    public static boolean closeWorkers(){
        System.out.println("Trying to close all workers");
        if(closeMachinesByTag("worker")){
            System.out.println("All workers closed!");
            return true;
        }
        System.out.println("Closing all workers failed!");
        return false;
    }

}
