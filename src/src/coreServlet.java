package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class coreServlet extends HttpServlet{
	ServerManager sm = new ServerManager();
	
	@Override
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		//generate html
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		
		//different operations by different user inputs
		String op = request.getParameter("op");
		
		//check if op is log out first
		if(op != null)
		{
			//logout and destroy current session
			if(op.equals("Logout"))
			{
				//get a current session, destroy that session and redirect to a log out page
				//Session session = SessionManager.getSession(request);
				//SessionManager.destroySession(session);
				out.println("<!DOCTYPE html>");
		        out.println("<html><head></head><body>");
		        out.println("<h3>Session terminate!</h3>");
		        //TODO: "/testProj1b/index" change testProj1b
		        out.println("<a href = '/testProj1b/index'>Back</a>");
		        out.println("</body></html>");
		        return;
			}
		}
		
		//if op is not log out, get a new session or update current session based on the cookie
		Session session = sm.getOrUpdate(request);
		//test session
//		SessionID sid = new SessionID("0", 0, 1);
//		Session session = new Session(sid, 1, "hello user", new Date());
		
		//set cookie and expire time, response with new cookie
		Cookie c = sm.setCookie(response, session);
		
		//preprocess html info
		String userID = "NetID: lz376";
		String displayMsg = session.msg;
		String sessionID = "SessionID:" + session.sessionID.toString();
		String version = "VersionNum:"+ session.version[0];
		String creationDate = "Date:" + session.createDate;
		String cookie = "Cookie: " + c.getValue();
		String expireTime = "Expire Time: " + session.discardTime;
		//new feature
		String executingServerID = "Executing ServerID: " + sm.amiIndex;
		String rebootNum = "RebootNum: " + sm.rebootNum;
		String dataFoundServerID = "Data Found ServerID: ";
		String cookieMetaData = "Cookie Meta Data: ";
		String cookieDomain = "Cookie Domain: "; 

		
		
		//generate html file
		String title = "CS5300_Proj1b_lz376";
	    String docType =
	        "<!DOCTYPE HTML>\n";
	      out.println(docType +
	                  "<HTML>\n" +
	                  "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
	                  "<BODY BGCOLOR=\"#ffffff\">\n" +
	                  "<span>" + userID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
	                  "<span>" + sessionID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
	                  "<span>" + version + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
	                  "<span>" + creationDate + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
	                  "<h1>" + displayMsg + "</h1>\n" +
	                  "<Form method = 'get'>" +
	                  "<p><Input type = 'submit' value = 'Replace' name = 'op'></Input>\n" +
	                  "<input Type = 'text' Name = 'replaceMsg'></Input></p>\n" +
	                  "<p><Input type = 'submit' value = 'Refresh' name = 'op'></Input>\n</p>" +
	                  "<p><Input type = 'submit' value = 'Logout' name = 'op'></Input>\n</p>" +
	                  "</Form>"+
					  "<p>" + cookie + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + expireTime + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + executingServerID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + rebootNum + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + dataFoundServerID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + cookieMetaData + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
					  "<p>" + cookieDomain + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</p>\n" +
	                  "</BODY></HTML>");
	
	}
}
