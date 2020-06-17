import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;

import java.util.concurrent.Callable;

public class TaskHandleInputFile implements Callable<TitleReviews[]> {

    private String bucketName;
    private String inputFileKey;

    public TaskHandleInputFile(String bucketName, String inputFileKey) {
        this.bucketName = bucketName;
        this.inputFileKey = inputFileKey;
    }

    @Override
    public TitleReviews[] call() throws Exception {
        Gson gson = new Gson();
        S3Object inputFile = S3.downloadFile(bucketName, inputFileKey); //download from bucket
        String inputData = IOUtils.toString(inputFile.getObjectContent());
        return gson.fromJson(inputData, TitleReviews[].class);
    }
}
