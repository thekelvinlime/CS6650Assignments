
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.AlbumsProfileDao;
import dao.ImageMetaDataDao;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class AlbumsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected ImageMetaDataDao imageMetaDataDao;
    protected AlbumsProfileDao albumsProfileDao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
        }

        if(!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().print("Invalid id");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            String albumIdString = urlPath.split("/")[1];
//            String albumIdString = request.getParameter("albumId");
            int albumId = Integer.parseInt(albumIdString);
            try {
                albumsProfileDao = AlbumsProfileDao.getInstance();
                AlbumsProfile albumsProfile = albumsProfileDao.getAlbumInfoById(albumId);
                String json = gson.toJson(albumsProfile);
                response.getWriter().write(json);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                response.getOutputStream().flush();
            }
        }

    }
    /**
     * Method to return whether the path provided is an expected endpoint.
     *
     * @param urlPath - The current endpoint being evaluated.
     * @return true if the url is a valid endpoint, false otherwise.
     */
    private boolean isUrlValid(String urlPath) {
        for (Endpoint endpoint : Endpoint.values()) {
            Pattern pattern = endpoint.pattern;

            if (pattern.matcher(urlPath).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
//        String urlPath = request.getPathInfo();
//        String servletPath = request.getServletPath();

        try {
            Part imagePart = request.getPart("image");
//            String albumId = request.getParameter("albumId");
            long imageSize = imagePart.getSize();
//            ErrorMsg errorMsg;
            response.setStatus(HttpServletResponse.SC_OK);
            ImageMetaData imageMetaData = new ImageMetaData().albumID("1").imageSize(String.valueOf(imageSize));
            imageMetaDataDao = ImageMetaDataDao.getInstance();
            imageMetaData = imageMetaDataDao.create(imageMetaData);
            String json = gson.toJson(imageMetaData);
            response.getWriter().write(json);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
//            ErrorMsg errorMsg = new ErrorMsg(e.getMessage());
//            response.getOutputStream().print(errorMsg.getMsg());
            response.getOutputStream().flush();
        }
    }

    /**
     * Enum constants that represent different possible endpoints
     */
    private enum Endpoint {
        POST_NEW_ALBUM(Pattern.compile("/albums")),
        GET_ALBUM_BY_KEY(Pattern.compile("^/\\d+$")); // Atm expects an int ID, will change in later assignments

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }

}



