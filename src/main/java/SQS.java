import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

public class SQS {

    private AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withCredentials(Credentials.getCredentials())
            .withRegion("us-east-1")
            .build();
    private String managerQURL;
    private String workersQURL;

    public SQS() {

        CreateQueueRequest managerQReq = new CreateQueueRequest("managerSQS");
        CreateQueueRequest workersQReq = new CreateQueueRequest("workerSQS");

        managerQURL = sqs.createQueue(managerQReq).getQueueUrl();
        workersQURL = sqs.createQueue(workersQReq).getQueueUrl();
    }

    public String createUserQ(String userID) {
        CreateQueueRequest userQReq = new CreateQueueRequest(userID);
        return sqs.createQueue(userQReq).getQueueUrl();
    }

    // send message to q = M/W
    public void sendMessage(String q, String msg) {

        try {
            String qURL = getQURL(q); //return M
            sqs.sendMessage(new SendMessageRequest(qURL, msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // receive message, return the first one
    public Message getMessage(String q) {
        try {
            String qURL = getQURL(q);
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(qURL);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages(); //up to 10?
            if (messages.size() > 0) {
                return messages.get(0);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // delete message
    public void removeMessage(String q, Message msg) {
        try {
            String qURL = getQURL(q);
            String messageRecieptHandle = msg.getReceiptHandle();
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(qURL, messageRecieptHandle);
            sqs.deleteMessage(deleteMessageRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMsgVisibility(String q, String msgReceipt, int timeout) {
        String qUrl = getQURL(q);
        sqs.changeMessageVisibility(new ChangeMessageVisibilityRequest(qUrl, msgReceipt, timeout));
    }

    public void deleteQueues() {
        try {
            deleteQ("M");
            deleteQ("W");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteQ(String q) {
        String qURL = getQURL(q);
        sqs.deleteQueue(qURL);
    }

    // return URL of manager/workers by request
    private String getQURL(String q) {
        if (q.equals("M")) {
            return managerQURL;
        } else if (q.equals("W")) {
            return workersQURL;
        } else {
            return q;
        }
    }

}