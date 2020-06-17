import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalAppHandler {

    private ConcurrentHashMap<String, AtomicInteger> localAppMsgCount = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ArrayList<ReviewFromWorker>> outputMsgs = new ConcurrentHashMap<>();
    private String userQurl;
    private String bucketName;


    public LocalAppHandler(String fileId, AtomicInteger msgCount, String userQurl, String bucketName) {
        this.localAppMsgCount.put(fileId, msgCount);
        this.outputMsgs.put(fileId, new ArrayList<ReviewFromWorker>());
        this.userQurl = userQurl;
        this.bucketName = bucketName;
    }

    public String getUserQurl() {
        return userQurl;
    }

    public String getBucketName() {
        return bucketName;
    }

    public AtomicInteger getLocalAppMsgCount(String fileId) {
        return localAppMsgCount.get(fileId);
    }

    public ArrayList<ReviewFromWorker> getOutputMsgs(String fileId) {
        return outputMsgs.get(fileId);
    }

    public void addToOutput(String fileId, ReviewFromWorker rev) {
        outputMsgs.get(fileId).add(rev);
    }

    public void addNewFile(String fileId, AtomicInteger msgCount) {
        localAppMsgCount.put(fileId, msgCount);
        outputMsgs.put(fileId, new ArrayList<ReviewFromWorker>());
    }

    // check if still working on at least 1 file
    public boolean isStillActive() {
        for (Map.Entry<String, AtomicInteger> entry : localAppMsgCount.entrySet()) {
            if (entry.getValue().get() > 0) {
                return true;
            }
        }
        return false;
    }
}