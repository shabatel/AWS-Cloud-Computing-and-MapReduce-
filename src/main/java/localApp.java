import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class localApp {

    public static void main(String[] args) {

        String id = UUID.randomUUID().toString();
        File inputFile = new File(args[0]);

        String outputFile = args[1];
        if(outputFile.contains(".html")){
            outputFile = outputFile.substring(0, outputFile.length()-5);
        }

        String n = args[2];
        // check for terminate message
        boolean terminate = false;
        if(args.length > 3){
            terminate = true;
        }

        //Manager check, start if not active.
        if (!managerCheck()) {
            EC2.runMachines(1,1,"manager","something"); //TODO
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

        Message msg;
        while(true){
            msg = queues.getMessage(qurl);
            if(msg == null)
                try {
                    sleep(1);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            // found message in queue
            String[] parts = msg.getBody().split("\n");

            // checks if the process is done
            if(parts[0].equals("done task")){
                if (parts[1].equals(id)) {
                    break;
                }
            }
        }
        S3Object summaryFile = S3.downloadFile("summary"+id+".txt");

        //TODO create an html file

        queues.removeMessage(qurl, msg);  //remove the "done task" from queue

        try {
            queues.deleteQ(qurl);
        }
        catch(Exception e){
            e.printStackTrace();
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
