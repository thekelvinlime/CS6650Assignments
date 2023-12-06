import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtraThread implements Runnable {
    private static final int MAX_TRIES = 5;
    private LikeApi apiInstance;
    private AtomicInteger success;
    private AtomicInteger failure;
    private ArrayList<Long> getLatencies;
    private String url;

    public ExtraThread(String url, AtomicInteger success, AtomicInteger failure, ArrayList<Long> getLatencies) throws IOException, TimeoutException {
        this.apiInstance = new LikeApi();
        this.url = url;
        apiInstance.getApiClient().setBasePath(url);
        this.success = success;
        this.failure = failure;
        this.getLatencies = getLatencies;

    }

    @Override
    public void run() {

    }

    private int performGetAlbumRequest() throws ApiException {
        long start = System.nanoTime();
        String albumIdString = String.valueOf(albumId); // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                int response = apiInstance.reviewWithHttpInfo("like", albumIdString).getStatusCode();
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
}
