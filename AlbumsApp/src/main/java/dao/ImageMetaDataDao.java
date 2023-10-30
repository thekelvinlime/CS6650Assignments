package dao;

import io.swagger.client.model.ImageMetaData;

import java.sql.*;
public class ImageMetaDataDao {
//    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/Assignment2";
        private static final String JDBC_URL = "jdbc:mysql://54.202.149.227:3306/Assignment2";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "password";
    private static ImageMetaDataDao instance = null;

    public ImageMetaDataDao() {

    }

    public static ImageMetaDataDao getInstance() {
        if (instance == null) {
            instance = new ImageMetaDataDao();
        }
        return instance;
    }
    public ImageMetaData create(ImageMetaData imageMetaData) throws SQLException, ClassNotFoundException {
        String insertImageMetaData =
                "INSERT INTO ImageMetaData(ImageSize,AlbumId) " +
                        "VALUES(?,?);";
        Connection connection = null;
        PreparedStatement insertStmt = null;
        ResultSet resultKey = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL,JDBC_USER,JDBC_PASSWORD);
            insertStmt = connection.prepareStatement(insertImageMetaData, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, imageMetaData.getImageSize());
//            insertStmt.setString(2, imageMetaData.getAlbumID());
            insertStmt.setInt(2, Integer.parseInt(imageMetaData.getAlbumID()));
            insertStmt.executeUpdate();

            resultKey = insertStmt.getGeneratedKeys();
            int imageId = -1;
            if(resultKey.next()) {
                imageId = resultKey.getInt(1);
            } else {
                throw new SQLException("Unable to retrieve auto-generated key.");
            }
            return imageMetaData;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(connection != null) {
                connection.close();
            }
            if(insertStmt != null) {
                insertStmt.close();
            }
            if(resultKey != null) {
                resultKey.close();
            }
        }
    }
}
