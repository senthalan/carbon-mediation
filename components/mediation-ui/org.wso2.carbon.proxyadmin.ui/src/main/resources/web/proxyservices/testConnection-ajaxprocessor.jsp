<%@ page import="java.net.URL" %>
<%@ page import="java.net.URI" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String url = request.getParameter("url");
    String returnValue;

    if (url != null && !url.equals("")) {
        try {
            URI connUri = new URI(url);
            URL conn = connUri.toURL();
            conn.getContent();
            returnValue = "success";

        } catch (Exception e) {
            returnValue = "Invalid URI specified for WSDL. The URI " + Encode.forHtmlContent(url) + " is " +
                    "malformed or does not exist.";
        }
    } else {
        returnValue = "Please specify a valid URL.";
    }
%>
<%=returnValue%>