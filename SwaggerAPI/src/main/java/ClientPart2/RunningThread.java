package ClientPart2;

import com.opencsv.CSVWriter;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RunningThread implements Runnable{
    private static final int MAX_TRIES = 5;
    private DefaultApi apiInstance;
    private AtomicInteger success;
    private AtomicInteger failure;
    private String url;
    private String csv;
    private ArrayList<Long> getLatencies;
    private ArrayList<Long> postLatencies;


    public RunningThread(String url, String csv, ArrayList<Long> getLatencies, ArrayList<Long> postLatencies, AtomicInteger success, AtomicInteger failure) {
        this.apiInstance = new DefaultApi();
        this.url = url;
        this.csv = csv;
        apiInstance.getApiClient().setBasePath(url);
        this.success = success;
        this.failure = failure;
        this.getLatencies = getLatencies;
        this.postLatencies = postLatencies;
    }

    public void run() {
        for (int i = 0; i < 1000; i++) {
            try {
                performGetRequest();
                performPostRequest();
            } catch (Exception e) {
                System.err.println("Requests failed");
            }
        }
    }
    private void performPostRequest() throws ApiException {
        long start = System.nanoTime()/1000000;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(start));
        File image = new File("C:\\Users\\theke\\OneDrive\\Pictures\\nmtb.png"); // File |
        AlbumsProfile profile = new AlbumsProfile().artist("Artist").title("Title").year("Year"); // AlbumsProfile |
        int tries = 0;
        String failedResponse = "";
        int response = 0;
        while (tries < MAX_TRIES) {
            try {
                response = apiInstance.newAlbumWithHttpInfo(image, profile).getStatusCode();
                if (response == 200) {
                    this.success.incrementAndGet();
                    long finish = System.nanoTime() / 1000000;
                    long latency = (finish - start);
                    this.postLatencies.add(latency);
//                    writeToCsv(String.valueOf(start/1000), String.valueOf(this.success),"POST",String.valueOf(latency),String.valueOf(response));
                    return;
                }
            } catch (Exception e) {
                tries++;
                failedResponse = String.valueOf(response);
            }

        }
        this.failure.incrementAndGet();
        long finish = System.nanoTime()/1000000;
        long latency = (finish - start);
        this.postLatencies.add(latency);
//        writeToCsv(String.valueOf(start/1000), String.valueOf(this.success),"POST",String.valueOf(latency),failedResponse);
    }

    private void performGetRequest() throws ApiException {
        long start = System.nanoTime()/1000000;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(start));
        String albumID = "1"; // String | path  parameter is album key to retrieve
        int tries = 0;
        String failedResponse = "";
        int response = 0;
        while (tries < MAX_TRIES) {
            try {
                response = apiInstance.getAlbumByKeyWithHttpInfo(albumID).getStatusCode();
                if (response == 200) {
                    this.success.incrementAndGet();
                    long finish = System.nanoTime() / 1000000;
                    long latency = (finish - start);
//                    writeToCsv(String.valueOf(start/1000), String.valueOf(this.success),"GET",String.valueOf(latency),String.valueOf(response));
                    this.getLatencies.add(latency);
                    return;
                }
            } catch (Exception e) {
                tries++;
                failedResponse = String.valueOf(response);
            }
        }
        this.failure.incrementAndGet();
        long finish = System.nanoTime()/1000000;
        long latency = (finish - start);
        this.getLatencies.add(latency);
//        writeToCsv(String.valueOf(start/1000), String.valueOf(this.success),"GET",String.valueOf(latency),failedResponse);
    }

    private void writeToCsv(String timestamp, String successful, String responseType, String latency, String response) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv, true));
            String[] record = new String[] {timestamp, successful, responseType, latency, response};
            writer.writeNext(record);
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
