import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


public class Manager {

    public static ConcurrentHashMap<String, LocalAppHandler> summary = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Manager is RUNNING");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        SQS sqs = new SQS();
        int workerCount = 0;
        boolean terminate = false;
        String userIdToTerminate = "";
        ArrayList<String> rejectedUsers = new ArrayList<String>();


        try {
            while (true) {

                Message msg = sqs.getMessage("M");
                if (msg == null) {
                    System.out.println("manager is sleeping!");
                    Utills.sleepMs(20);
                    continue;
                }

                String msgBody = Objects.requireNonNull(msg).getBody();
                String taskType = msgBody.substring(0, msgBody.indexOf('\n'));

                switch (taskType) {
                    case "new task":  //first task from user
                        System.out.println("manager got msg from user");
                        msgBody = msg.getBody().substring(msgBody.indexOf('\n') + 1); //without "new task"

                        System.out.println(msgBody);

                        if (msgBody.split("\n")[0].equals("terminate")) { //terminate
                            if (!terminate) {
                                terminate = true;
                                userIdToTerminate = msgBody.split("\n")[1];
                            }

                            // DO NOT ACCEPT MORE INPUT FILES

                            // Wait for workers to finish ~ WQ=empty -> close all workers
                            // generate response message -> export summary to userApplication
                        } else { //body = n\n<key of input file>\n<user ID>

                            if (terminate && !summary.containsKey(msgBody.split("\n")[2]) &&
                                    !msgBody.split("\n")[2].equals(userIdToTerminate)) {
                                if(!rejectedUsers.contains(msgBody.split("\n")[3])) {
                                    sqs.sendMessage(msgBody.split("\n")[3], "close request\n");
                                    rejectedUsers.add(msgBody.split("\n")[3]);
                                }
                                break;
                            }

                            String[] parts = msgBody.split("\n");
                            int n = Integer.parseInt(parts[0]);
                            String inputFileKey = parts[1];
                            String userID = parts[2];
                            String userQUrl = parts[3];
                            String bucketName = parts[4];
                            String fileId = parts[5];


                            Future<TitleReviews[]> titleRev = pool.submit(new TaskHandleInputFile(bucketName, inputFileKey));
                            AtomicInteger reviewsCount = getReviewsCount(titleRev.get());
                            int neededWorkersCount = Math.max(1, (reviewsCount.getAndSet(reviewsCount.intValue()) / n) - workerCount);
                            LocalAppHandler localAppHandler = summary.get(userID); // check if already handling with this user
                            if(localAppHandler == null)
                                summary.put(userID, new LocalAppHandler(fileId, reviewsCount, userQUrl, bucketName)); //add the current local app to memory
                            else localAppHandler.addNewFile(fileId, reviewsCount); //add new file to an old user
                            while (neededWorkersCount != 0 && workerCount < 33) {
                                pool.submit(new TaskRunWorker(workerCount));
                                neededWorkersCount--;
                                workerCount++;
                            }

                            System.out.println("manager is filling jobs q of user");
                            pool.execute(new TaskFillWorkerQ(titleRev.get(), sqs, userID, fileId));
                            System.out.println("manager finished filling up user q ");
                        }

                        break;
                    case "done review":

                        pool.execute(new TaskHandleWorkerOutput(msgBody, sqs));

                        break;
                    case "user termination":
                        if (terminate) {
                            // iterate over all userapps, wait untill all active messages = 0
                            boolean over = true;
                            for (Map.Entry<String, LocalAppHandler> entry : summary.entrySet()) {
                                if (entry.getValue().isStillActive()) {
                                    over = false;
                                    break;
                                }
                            }
                            if (!over) {
                                sqs.removeMessage("M", msg);
                                continue;
                            }
                            sqs.removeMessage("M", msg);
                            System.out.println("closing workers");
                            // close all workers
                            pool.shutdown();
                            EC2.closeWorkers();
                            sqs.deleteQueues();

                            // Last queue is UserApp queue and it remains unclosed for the event which
                            // multiple users wait for an answer and  there's no guarantee which user will receive
                            // his response message last.

                            // close manager, removing message is redundant as we closed the manager Q above.
                            System.out.println("closing manager");
                            EC2.closeManager();

                            return;
                        }
                        break;
                }
                sqs.removeMessage("M", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static AtomicInteger getReviewsCount(TitleReviews[] titleRev) {

        int revCount = 0;
        for (TitleReviews titleReviews : titleRev) {
            revCount += titleReviews.getReviews().length;
        }
        return new AtomicInteger(revCount);
    }
}
