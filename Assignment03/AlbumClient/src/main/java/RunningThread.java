import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;

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
        for (int i = 0; i < 100; i++) {
            try {
                int albumId = performPostAlbumRequest();
                performPostLikeReviewRequest(String.valueOf(albumId));
                performPostLikeReviewRequest(String.valueOf(albumId));
                performPostDisLikeReviewRequest(String.valueOf(albumId));
                System.out.println(success);
            } catch (Exception e) {
                System.err.println("Request failed");
            }
        }
    }
    private void performPostLikeReviewRequest(String albumID) throws ApiException {
        long start = System.nanoTime();
//        String albumID = "1"; // String | path  parameter is album key to retrieve
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
                final Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                channel.basicQos(1);
                System.out.println(" [x] Receiving messages. To exit press CTRL+C");
                int response = apiInstance2.reviewWithHttpInfo("like", albumID).getStatusCode();
//                System.out.println(response);
                if (response == 200) {
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                    };
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
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

    private void performPostDisLikeReviewRequest(String albumID) throws ApiException {
        long start = System.nanoTime();
//        String albumID = "1"; // String | path  parameter is album key to retrieve
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

    private int performPostAlbumRequest() throws ApiException {
        long start = System.nanoTime();
        File image = new File("C:\\Users\\theke\\OneDrive\\Pictures\\nmtb.png"); // File |
        AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
        int tries = 0;
        while (tries < MAX_TRIES) {
            try {
//                int response = apiInstance.newAlbumWithHttpInfo(image, profile).getStatusCode();
                int albumId = Integer.parseInt(apiInstance.newAlbumWithHttpInfo(image, profile).getData().getAlbumID());
//                System.out.println(response);
                this.success.incrementAndGet();
                long finish = System.nanoTime();
                long latency = (finish - start);
                this.postLatencies.add(latency);
                return albumId;
            } catch (Exception e) {
                tries++;
                System.err.println("Attempt #" + tries + " failed");
            }

        }
        this.failure.incrementAndGet();
        return tries;
    }

}
