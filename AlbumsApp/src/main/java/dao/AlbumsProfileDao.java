package dao;

import io.swagger.client.model.AlbumsProfile;

import java.sql.*;

public class AlbumsProfileDao {
//    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/Assignment2";
    private static final String JDBC_URL = "jdbc:mysql://54.202.149.227:3306/Assignment2";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "password";
    private static AlbumsProfileDao instance = null;
    public AlbumsProfileDao() {

    }

    public static AlbumsProfileDao getInstance() {
        if (instance == null) {
            instance = new AlbumsProfileDao();
        }
        return instance;
    }
    public AlbumsProfile create(AlbumsProfile albumsProfile) throws SQLException, ClassNotFoundException {
        String insertAlbumInfo =
                "INSERT INTO Albums(Artist,Title,Year) " +
                        "VALUES(?,?,?);";
        Connection connection = null;
        PreparedStatement insertStmt = null;
        ResultSet resultKey = null;
        try {
            //make a connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL,JDBC_USER,JDBC_PASSWORD);
            insertStmt = connection.prepareStatement(insertAlbumInfo, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, albumsProfile.getArtist());
            insertStmt.setString(2, albumsProfile.getTitle());
            insertStmt.setString(3, albumsProfile.getYear());
            insertStmt.executeUpdate();

            resultKey = insertStmt.getGeneratedKeys();
            int albumId = -1;
            if (resultKey.next()) {
                albumId = resultKey.getInt(1);
            } else {
                throw new SQLException("Unable to retrieve auto-generated key");
            }
            //albumInfo.setAlbumId(albumId);
            return albumsProfile;

            //sql statement
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } finally {
            //close connection
            if (connection != null) connection.close();
            if (insertStmt != null) insertStmt.close();
            if (resultKey != null) resultKey.close();
        }
    }

    public AlbumsProfile getAlbumInfoById(int albumId) throws SQLException, ClassNotFoundException {
        String selectAlbumsProfile =
                "SELECT AlbumId,Artist,Title,Year " +
                        "FROM Albums " +
                        "WHERE albumId=?;";
        Connection connection = null;
        PreparedStatement selectStmt = null;
        ResultSet results = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL,JDBC_USER,JDBC_PASSWORD);
            selectStmt = connection.prepareStatement(selectAlbumsProfile);
            selectStmt.setInt(1, albumId);
            results = selectStmt.executeQuery();

            if (results.next()) {
                int resultAlbumId = results.getInt("AlbumId");
                String resultArtist = results.getString("Artist");
                String resultTitle = results.getString("Title");
                String resultYear = results.getString("Year");

                return new AlbumsProfile().artist(resultArtist).title(resultTitle).year(resultYear);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(connection != null) {
                connection.close();
            }
            if(selectStmt != null) {
                selectStmt.close();
            }
            if(results != null) {
                results.close();
            }
        }
        return null;
    }
}
