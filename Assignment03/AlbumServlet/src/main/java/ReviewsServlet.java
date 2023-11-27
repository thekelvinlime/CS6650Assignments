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
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

@WebServlet(name = "ReviewsServlet", value = "/review/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class ReviewsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String QUEUE_NAME = "ReviewQ";
    private HikariDataSource connectionPool;
    private Connection rbmqConnection;
    private Channel channel;


    public void init() {
        connectionPool =  SQLConnectionPool.createDataSource();
        rbmqConnection = RabbitMQConnection.createConnection();
        try {
            channel = rbmqConnection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        connectionPool.close();
        try {
            rbmqConnection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        String message = null;
        if (action.equals("like")) {
            insertLike(albumId);
//            message = "AlbumID " + albumIdString + " +1 " + "like";
        }

        if (action.equals("dislike")) {
            insertDisLike(albumId);
//            message = "AlbumID " + albumIdString + " +1 " + "dislike";
        }
//        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//        System.out.println(" [x] Sent '" + message + "'");
//        try {
//            channel.close();
//        } catch (TimeoutException e) {
//            throw new RuntimeException(e);
//        }
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

