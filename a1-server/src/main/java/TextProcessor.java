import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "TextProcessor", value = "/TextProcessor")
public class TextProcessor extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
          throws ServletException, IOException {
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write("Servlet received GET request!");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
          throws ServletException, IOException {

    res.setContentType("application/json");

    String reqPath = req.getPathInfo();

    if (reqPath == null || reqPath.isEmpty()) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
      return;
    }

    // TODO: validate request (should be /textbody/{function} - what are valid functions?
    if (reqPath.equals("/wordcount") &&
            req.getContentType().startsWith("application/json")) {

      Gson gson = new Gson();
      BufferedReader reqBody = req.getReader();
      Map<String, String> msg = gson.fromJson(reqBody,
              new TypeToken<Map<String, String>>(){}.getType());

      if (!msg.get("message").isEmpty()) {
        int val = msg.get("message").split(" ").length;
        ResultVal resVal = new ResultVal(val);
        String jsonRes = gson.toJson(resVal);

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(jsonRes);
        return;
      }
    }

    // TODO: delete debug prints
    System.out.println(req.getContentType());
    System.out.println(reqPath);
    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
  }
}
