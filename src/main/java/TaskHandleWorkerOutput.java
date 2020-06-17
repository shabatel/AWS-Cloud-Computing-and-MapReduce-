import com.google.gson.Gson;

import java.io.File;

public class TaskHandleWorkerOutput implements Runnable {

    private String msgBody;
    private SQS sqs;

    public TaskHandleWorkerOutput(String msgBody, SQS sqs) {
        this.msgBody = msgBody;
        this.sqs = sqs;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        String[] parts = msgBody.split("\n");
        String userId = parts[1];
        String reviewFromWorker = parts[2];
        String fileId = parts[3];
        Manager.summary.get(userId).addToOutput(fileId, gson.fromJson(reviewFromWorker, ReviewFromWorker.class)); //add to summary
        Manager.summary.get(userId).getLocalAppMsgCount(fileId).decrementAndGet(); //-1 to msgCount of fileId

        if (Manager.summary.get(userId).getLocalAppMsgCount(fileId).get() == 0) { //if did all the file's msgs
            System.out.println("THREAD IS MAKING OUTPUTFILE TO USER");
            String fileName = "summary" + userId + fileId + ".json";
            File summaryFile = new File(fileName);
            System.out.println("remained msgs from user: " + Manager.summary.get(userId).getLocalAppMsgCount(fileId));
            System.out.println("the summary is " + Manager.summary.get(userId).getOutputMsgs(fileId));
            Utills.writeToFile(summaryFile, Manager.summary.get(userId).getOutputMsgs(fileId));
            String summaryFileKey = S3.uploadFile(Manager.summary.get(userId).getBucketName(), summaryFile);
            sqs.sendMessage(userId, "finished task\n" + summaryFileKey + "\n" + fileId);
            if(!Manager.summary.get(userId).isStillActive()) {  // send user termination if there are no more active files
                sqs.sendMessage("M", "user termination\n");
            }
        }
    }
}