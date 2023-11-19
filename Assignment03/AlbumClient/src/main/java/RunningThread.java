import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.ArrayList;
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

    public RunningThread(String url, AtomicInteger success, AtomicInteger failure, ArrayList<Long> getLatencies, ArrayList<Long> postLatencies) {
        this.apiInstance = new DefaultApi();
        this.apiInstance2 = new LikeApi();
        this.url = url;
        apiInstance.getApiClient().setBasePath(url);
        this.success = success;
        this.failure = failure;
        this.getLatencies = getLatencies;
        this.postLatencies = postLatencies;
    }
    public void run() {
        for (int i = 0; i < 1000; i++) {
            try {
                performPostLikeReviewRequest();
                performPostLikeReviewRequest();
                performPostDisLikeReviewRequest();
                performPostAlbumRequest();
                System.out.println(success);
            } catch (Exception e) {
                System.err.println("Request failed");
            }
        }
    }
    private void performPostLikeReviewRequest() throws ApiException {
        long start = System.nanoTime();
        String albumID = "1"; // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                int response = apiInstance2.reviewWithHttpInfo("like", albumID).getStatusCode();
//                System.out.println(response);
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

    private void performPostDisLikeReviewRequest() throws ApiException {
        long start = System.nanoTime();
        String albumID = "1"; // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                int response = apiInstance2.reviewWithHttpInfo("not", albumID).getStatusCode();
//                System.out.println(response);
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
                int response = apiInstance.newAlbumWithHttpInfo(image, profile).getStatusCode();
//                System.out.println(response);
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
