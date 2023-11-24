import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class AlbumsServlet extends HttpServlet {
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
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing params");
            return;
        } else if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("invalid id");
            return;
        }

//        response.setStatus(HttpServletResponse.SC_OK);
//        String json = gson.toJson(new AlbumsProfile().artist("Artist").title("Title").year("Year"));
//        response.getWriter().write(json);
        String albumIdString = urlPath.split("/")[1];
        String selectAlbumsProfile =
                "SELECT AlbumId,Artist,Title,Year " +
                        "FROM Albums " +
                        "WHERE albumId=?;";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try (Connection connection = this.connectionPool.getConnection()) {
            int albumId = Integer.parseInt(albumIdString);
            preparedStatement = connection.prepareStatement(selectAlbumsProfile);
            preparedStatement.setInt(1, albumId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int resultAlbumId = resultSet.getInt("AlbumId");
                String resultArtist = resultSet.getString("Artist");
                String resultTitle = resultSet.getString("Title");
                String resultYear = resultSet.getString("Year");

                response.setStatus(HttpServletResponse.SC_OK);
                String json = gson.toJson(new AlbumsProfile().artist(resultArtist).title(resultTitle).year(resultYear));
                response.getWriter().write(json);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        try {
            Part imagePart = request.getPart("image");
            long imageSize = imagePart.getSize();
//            response.setStatus(HttpServletResponse.SC_OK);
//            String json = gson.toJson(new ImageMetaData().albumID(albumIdString).imageSize(String.valueOf(imageSize)));
//            response.getWriter().write(json);
            String insertImageMetaData =
                            "INSERT INTO ImageMetaData(ImageSize,AlbumId) " +
                            "VALUES(?,?);";

            PreparedStatement preparedStatement = null;
            ResultSet resultKey = null;
            try (Connection connection = this.connectionPool.getConnection()) {
                int albumId = createAlbumId();
//                int albumId = 1;
                preparedStatement = connection.prepareStatement(insertImageMetaData, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, String.valueOf(imageSize));
//            insertStmt.setString(2, imageMetaData.getAlbumID());
                preparedStatement.setInt(2, albumId);
                preparedStatement.executeUpdate();

                resultKey = preparedStatement.getGeneratedKeys();
                int imageId = -1;
                if(resultKey.next()) {
                    imageId = resultKey.getInt(1);
                } else {
                    throw new SQLException("Unable to retrieve auto-generated key.");
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getOutputStream().flush();
        }
    }

    private int createAlbumId() {
        String insertAlbumInfo =
                "INSERT INTO Albums(Artist,Title,Year) " + "VALUES(?,?,?);";
        PreparedStatement preparedStatement = null;
        ResultSet resultKey = null;
        try (Connection connection = this.connectionPool.getConnection()) {
            preparedStatement = connection.prepareStatement(insertAlbumInfo, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, "Artist");
            preparedStatement.setString(2, "Title");
            preparedStatement.setString(3, "Year");
            preparedStatement.executeUpdate();
            resultKey = preparedStatement.getGeneratedKeys();
            int albumId = -1;
            if (resultKey.next()) {
                albumId = resultKey.getInt(1);
                return albumId;
            } else {
                throw new SQLException("Unable to retrieve auto-generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUrlValid(String urlPath) {
        for (Endpoint endpoint : Endpoint.values()) {
            Pattern pattern = endpoint.pattern;

            if (pattern.matcher(urlPath).matches()) {
                return true;
            }
        }

        return false;
    }

    private enum Endpoint {
        POST_NEW_ALBUM(Pattern.compile("/albums")),
        GET_ALBUM_BY_KEY(Pattern.compile("^/\\d+$")); // Atm expects an int ID, will change in later assignments

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }
}

