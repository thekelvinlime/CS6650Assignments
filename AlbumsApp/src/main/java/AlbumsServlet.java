
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.ImageMetaData;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class AlbumsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
            String json = gson.toJson(new AlbumInfo().artist("Sex Pistols").title("Never Mind The Bollocks").year("1997"));

            response.getWriter().write(json);
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
        String urlPath = request.getPathInfo();
        String servletPath = request.getServletPath();

        try {
            Part imagePart = request.getPart("image");
            String albumId = request.getParameter("albumId");
            long imageSize = imagePart.getSize();
//            ErrorMsg errorMsg;
            response.setStatus(HttpServletResponse.SC_OK);
            String json = gson.toJson(new ImageMetaData().albumID(albumId).imageSize(String.valueOf(imageSize)));
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
        POST_NEW_ALBUM(Pattern.compile("/album")),
        GET_ALBUM_BY_KEY(Pattern.compile("^/\\d+$")); // Atm expects an int ID, will change in later assignments

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }

}



