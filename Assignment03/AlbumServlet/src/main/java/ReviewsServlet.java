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
    private HikariDataSource connectionPool;


    public void init() {
        connectionPool =  SQLConnectionPool.createDataSource();
    }

    public void destroy() {
        connectionPool.close();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        // Consume message in servlet, Publish message in client
        String action = urlPath.split("/")[1];
        String albumIdString = urlPath.split("/")[2];
        int albumId = Integer.parseInt(albumIdString);

        if (action.equals("like")) insertLike(albumId);
        else insertDisLike(albumId);
    }

    private void insertLike(int albumId) {
        String updateLikesAndDislikes =
                "UPDATE AlbumLikesDislikes SET likes = likes + 1" +
                        "WHERE AlbumId=?";
        PreparedStatement preparedStatement = null;
        ResultSet resultKey = null;
        try (java.sql.Connection connection = this.connectionPool.getConnection()) {
            preparedStatement = connection.prepareStatement(updateLikesAndDislikes);
            preparedStatement.setInt(1, albumId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertDisLike(int albumId) {
        String updateLikesAndDislikes =
                "UPDATE AlbumLikesDislikes SET dislikes = dislikes + 1 " +
                        "WHERE AlbumId=?";
        PreparedStatement preparedStatement = null;
        ResultSet resultKey = null;
        try (java.sql.Connection connection = this.connectionPool.getConnection()) {
            preparedStatement = connection.prepareStatement(updateLikesAndDislikes);
            preparedStatement.setInt(1, albumId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

