import com.rabbitmq.client.*;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RunningThread implements Runnable{
    private static final int MAX_TRIES = 5;
    private DefaultApi apiInstance;
    private LikeApi apiInstance2;
    private AtomicInteger success;
    private AtomicInteger failure;
    private ArrayList<Long> getLatencies;
    private ArrayList<Long> postLatencies;
    private String url;
    private final static String QUEUE_NAME = "ReviewQ";
    private final Connection connection;

    public RunningThread(String url, AtomicInteger success, AtomicInteger failure, ArrayList<Long> getLatencies, ArrayList<Long> postLatencies) throws IOException, TimeoutException {
        this.apiInstance = new DefaultApi();
        this.apiInstance2 = new LikeApi();
        this.url = url;
        apiInstance.getApiClient().setBasePath(url);
        this.success = success;
        this.failure = failure;
        this.getLatencies = getLatencies;
        this.postLatencies = postLatencies;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                performPostAlbumRequest();
                performPostLikeReviewRequest(1);
                performPostLikeReviewRequest(1);
                performPostDisLikeReviewRequest(1);
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void performPostLikeReviewRequest(int albumId) throws ApiException {
        long start = System.nanoTime();
        String albumIdString = String.valueOf(albumId); // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                int response = apiInstance2.reviewWithHttpInfo("like", albumIdString).getStatusCode();
                if (response == 200) {
                    this.success.incrementAndGet();
                    long finish = System.nanoTime();
                    long latency = (finish - start);
                    this.getLatencies.add(latency);
                    return;
                }
            } catch (Exception e) {
                tries++;
                System.err.println("Attempt #" + tries + " failed");
            }
        }
        this.failure.incrementAndGet();
    }

    private void performPostDisLikeReviewRequest(int albumId) throws ApiException {
        long start = System.nanoTime();
        String albumIdString = String.valueOf(albumId); // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                int response = apiInstance2.reviewWithHttpInfo("dislike", albumIdString).getStatusCode();
                if (response == 200) {
                    this.success.incrementAndGet();
                    long finish = System.nanoTime();
                    long latency = (finish - start);
                    this.getLatencies.add(latency);
                    return;
                }
            } catch (Exception e) {
                tries++;
                System.err.println("Attempt #" + tries + " failed");
            }
        }
        this.failure.incrementAndGet();
    }

    private void performPostAlbumRequest() throws ApiException {
        long start = System.nanoTime();
        File image = new File("C:\\Users\\theke\\OneDrive\\Pictures\\nmtb.png"); // File |
        AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
//                ImageMetaData imageMetaData = apiInstance.newAlbumWithHttpInfo(image, profile).getData();
//                int albumId = Integer.parseInt(imageMetaData.getAlbumID());
                int response = apiInstance.newAlbumWithHttpInfo(image, profile).getStatusCode();
                if (response == 200) {
                    this.success.incrementAndGet();
                    long finish = System.nanoTime();
                    long latency = (finish - start);
                    this.postLatencies.add(latency);
                    return;
                }
            } catch (Exception e) {
                tries++;
                System.err.println("Attempt #" + tries + " failed");
            }

        }
        this.failure.incrementAndGet();
    }

}
