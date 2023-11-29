import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

import com.rabbitmq.client.*;

@WebServlet(name = "ReviewsServlet", value = "/review/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class ReviewsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String QUEUE_NAME = "ReviewQ";
    private final int POOL_SIZE = 100;
    private BlockingQueue<Channel> channelPool;
    private ExecutorService executorService;
    private HikariDataSource connectionPool;


    public void init() {
        connectionPool =  SQLConnectionPool.createDataSource();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setHost("ec2-52-26-115-234.us-west-2.compute.amazonaws.com");
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        channelPool = new LinkedBlockingQueue<>(POOL_SIZE);
        //Added
        try {
//            connection = factory.newConnection();
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                channelPool.offer(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void destroy() {
        connectionPool.close();
        executorService.shutdownNow();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        String action = urlPath.split("/")[1];
        String albumIdString = urlPath.split("/")[2];

        int albumId = Integer.parseInt(albumIdString);
        String message = null;
        if (action.equals("like")) {
            insertLike(albumId);
            message = "AlbumID " + albumIdString + " +1 " + "like";
        }

        if (action.equals("dislike")) {
            insertDisLike(albumId);
            message = "AlbumID " + albumIdString + " +1 " + "dislike";
        }
        if (message != null && !message.isEmpty()) {
            String finalMessage = message;
            executorService.submit(() -> sendMessage(finalMessage));
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void sendMessage(String message) {
        Channel channel = null;
        try {
            channel = channelPool.poll(1, TimeUnit.SECONDS);
            if (channel != null) {
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
            } else {
                System.out.println("No channel available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channelPool.offer(channel);
            }
        }
    }
    private void insertLike(int albumId) {
        String updateLikes =
                "INSERT INTO AlbumReviews (albumId, likes, dislikes)" +
                        "VALUES (?, 1, 0)" + "ON DUPLICATE KEY UPDATE likes = likes + 1";
        PreparedStatement preparedStatement = null;
        ResultSet resultKey = null;
        try (java.sql.Connection connection = this.connectionPool.getConnection()) {
            preparedStatement = connection.prepareStatement(updateLikes);
            preparedStatement.setInt(1, albumId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertDisLike(int albumId) {
        String updateDislikes =
                "INSERT INTO AlbumReviews (albumId, likes, dislikes)" +
                        "VALUES (?, 0, 1)" + "ON DUPLICATE KEY UPDATE dislikes = dislikes + 1";
        PreparedStatement preparedStatement = null;
        ResultSet resultKey = null;
        try (java.sql.Connection connection = this.connectionPool.getConnection()) {
            preparedStatement = connection.prepareStatement(updateDislikes);
            preparedStatement.setInt(1, albumId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

