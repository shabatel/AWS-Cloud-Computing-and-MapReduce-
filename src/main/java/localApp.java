import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class localApp {

    public static void main(String[] args) {

        String id = UUID.randomUUID().toString();
        File inputFile = new File(args[0]);

        String n = args[2];
        // check for terminate message
        boolean terminate = false;
        if(args.length > 3){
            terminate = true;
        }

        //Manager check, start if not active.
        if (!managerCheck()) {
            EC2.runMachines(); //TODO
        }

        // upload input file to S3
        S3.createBucket();
        String inputFileKey = S3.uploadFile(inputFile);

        //Send message, stating the location of the inputFile on S3
        SQS queues = new SQS();
        String qurl = queues.createUserQ(id);

        queues.sendMessage("M", "new task\n"+ n +"\n"+inputFileKey+"\n"+id+"\n"+qurl);

        // send terminate message if needed
        if(terminate){
            queues.sendMessage("M", "terminate\n"+id);
        }

    }

    //Checks if a Manager node is active
    private static boolean managerCheck() {
        List<Instance> activeInstances = EC2.getActiveInstances();
        for (Instance i : activeInstances) {
            List<Tag> tags = i.getTags();
            for (Tag t : tags) {
                if (t.getKey().equals("manager")) {
                    return true;
                }
            }
        }
        return false;
    }

}
