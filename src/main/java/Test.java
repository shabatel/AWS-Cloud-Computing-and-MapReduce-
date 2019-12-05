//import com.amazonaws.services.s3.model.S3Object;

import java.io.*;

public class Test {
    public static void main(String[] args) throws IOException {
        //S3.createBucket();
        //File file = new File ("/home/shabatel/IdeaProjects/Mevuzarot_ass1/src/main/resources/0689835604.json");
       // String key = S3.uploadFile(file);
       // S3Object object = S3.downloadFile(key);
        //System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());
        //displayTextInputStream(object.getObjectContent());
        //S3.removeFile("0689835604.json");
        System.out.println(S3.getFileURL("0689835604.json"));
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }
}