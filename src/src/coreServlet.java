package src;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class coreServlet extends HttpServlet{
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
		        out.println("<Form action = '/CS5300_proj1a/index' method = 'get'>");
		        out.println("<Input type = 'submit' value = 'Back'></Input>");
		        out.println("</Form>");
		        out.println("</body></html>");
		        return;
			}
		}
		
		//if op is not log out, get a new session or update current session based on the cookie
		ServerManager sm = new ServerManager();
		Session session = sm.getOrUpdate(request);
		
		//set cookie and expire time, response with new cookie
		Cookie c = sm.setCookie(response, session);
		
		//preprocess html info
		String userID = "NetID: lz376";
		String displayMsg = "displayMsg" + session.msg;
		String sessionID = "SessionID:" + session.sessionID.toString();
		String version = "VersionNum:"+ session.version[0];
		String creationDate = "Date:" + session.createDate;
		String cookie = "Cookie: " + c.getValue();
		String expireTime = "Expire Time: " + session.discardTime;
		//new feature
		String executingServerID = "Executing ServerID: " + sm.amiIndex;
		String rebootNum = "RebootNum: " + sm.rebootNum;
		String dataFoundServerID = "Data Found ServerID: ";
		String cookieMetaData = "Cookie Meta Data: " + sm.locMetaData(c);
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
					  "<span>" + cookie + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + expireTime + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + executingServerID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + rebootNum + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + dataFoundServerID + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + cookieMetaData + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
					  "<span>" + cookieDomain + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</span>\n" +
	                  "</BODY></HTML>");
	
	}
}
