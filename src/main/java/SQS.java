import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

public class SQS {
    private AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private String managerQURL;
    private String workersQURL;

    public SQS() {
        CreateQueueRequest managerQReq = new CreateQueueRequest("managerQ3");  //TODO: change name?
        CreateQueueRequest workersQReq = new CreateQueueRequest("workersQ3");

        managerQURL = sqs.createQueue(managerQReq).getQueueUrl();
        workersQURL = sqs.createQueue(workersQReq).getQueueUrl();
    }

    public String createUserQ(String userID) {
        CreateQueueRequest userQReq = new CreateQueueRequest(userID);
        return sqs.createQueue(userQReq).getQueueUrl();
    }

    // send message to q
    public void sendMessage(String q, String msg) {

        try {
            String qURL = getQURL(q);
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
    public boolean removeMessage(String q, Message msg) {
        try {
            String qURL = getQURL(q);
            String messageRecieptHandle = msg.getReceiptHandle();
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(qURL, messageRecieptHandle);
            if (sqs.deleteMessage(deleteMessageRequest) != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public void deleteQ(String q) {  //TODO: need to use DeleteQueueRequest??
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
