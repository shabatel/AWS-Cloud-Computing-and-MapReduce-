import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class LocalApplication {
    public static void main(String[] args) {
        /*
         input from terminal can be as follows:
        * java -jar yourjar.jar inputFileName1 ... inputFileNameM outputFileName1 ... outputFileNameM nReviewPerWorker n
        * or:
        * java  -jar yourjar.jar inputFileName1 ... inputFileNameM outputFileName1 ... outputFileNameM nReviewPerWorker terminate
        */
        String userAppID = UUID.randomUUID().toString();
        int userInterface = 0;
        int filesCount;
        String nReviewPerWorker;
        Credentials.setCredentials();
        String bucketName = Utills.uncapitalizeChars(Credentials.getCredentials().getCredentials().getAWSAccessKeyId());
        SQS localAppQ = new SQS();
        String localAppQUrl = localAppQ.createUserQ(userAppID);
        if (args[args.length - 1].equals("terminate")) {
            localAppQ.sendMessage("M", "new task\nterminate\n" + userAppID);
            filesCount = (args.length - 2) / 2;
            nReviewPerWorker = args[args.length - 2];
        }
        else {
            filesCount = (args.length -1) / 2;
            nReviewPerWorker = args[args.length - 1];
        }
        String[] inputFilesNames = new String[filesCount];
        File[] inputFiles = new File[filesCount];
        String[] outputFilesNames = new String[filesCount];
        System.arraycopy(args, 0, inputFilesNames, 0, filesCount);
        System.arraycopy(args, (filesCount), outputFilesNames, 0, filesCount);
        for (int i = 0; i < filesCount; i++) {
            inputFiles[i] = new File(inputFilesNames[i]);
        }
        if (!managerIsUp()) {
            EC2.runMachines("manager", "manager");
        }

        //upload data file to S3 bucket and return its key
        S3.createBucket(bucketName);
        for (int i = 0; i < filesCount; i++) {
            String inputFileKey = S3.uploadFile(bucketName, inputFiles[i]);
            localAppQ.sendMessage("M", "new task\n" + nReviewPerWorker + "\n" + inputFileKey + "\n" + userAppID + "\n"
                    + localAppQUrl + "\n" + bucketName + "\n" + i);
        }


        Message msg;
        String[] parsedMsg;
        while (true) {
            msg = localAppQ.getMessage(localAppQUrl);
            if (msg == null) {
                Utills.sleepMs(1000);
                inPRogressMsg(userInterface);
                userInterface++;
                continue;
            }
            parsedMsg = Objects.requireNonNull(msg).getBody().split("\n");
            if (parsedMsg[0].equals("finished task")) {
                filesCount--;
                String key = parsedMsg[1];
                String i = parsedMsg[2];
                S3Object summaryFile = S3.downloadFile(bucketName, key);
                System.out.println("downloaded summary from manager");
                try {
                    System.out.println("user creating HTML file");
                    Utills.stringToHTML(outputFilesNames[Integer.parseInt(i)], IOUtils.toString(summaryFile.getObjectContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                localAppQ.removeMessage(localAppQUrl, msg);
                if (filesCount == 0) {
                    break;
                }
            } else if (parsedMsg[0].equals("close request")) { //terminate due to other user termination
                System.out.println("system has been terminated. Can't handle request");
                localAppQ.deleteQ(localAppQUrl);
                return;
            }
        }
        try {
            localAppQ.deleteQ(localAppQUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done!");
    }

    private static void inPRogressMsg(int userInterfaceRequest) {
        if (userInterfaceRequest % 6 ==  0) System.out.println("request in progress");
        else if (userInterfaceRequest % 6 == 1) System.out.println("request in progress..");
        else if (userInterfaceRequest % 6 == 2) System.out.println("request in progress....");
        else if (userInterfaceRequest % 6 == 3) System.out.println("request in progress......");
        else if (userInterfaceRequest % 6 == 4) System.out.println("request in progress........");
        else if (userInterfaceRequest % 6 == 5) System.out.println("request in progress..........");
    }

    private static boolean managerIsUp() {
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