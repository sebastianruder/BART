<%-- 
    Document   : index
    Created on : Apr 12, 2008, 10:11:43 AM
    Author     : yannick
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>BART Demo</title>
<link rel="stylesheet" type="text/css" href="styles.css">
<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojo/dojo.xd.js" type="text/javascript"></script>
<script type="text/javascript" src="functions.js"></script>
</head>
<body onLoad="updateRight()">
<h1>BART Demonstrator</h1>
<!-- right panel -->
<div id="rightpanel">
    
<hr>
<a href="#" onClick="display_newdoc()">Create new document...</a>
</div>
<!-- main content -->
<div id="leftpanel">
    <h3>Welcome to the BART demo!</h3>
    You can do the following things:
    <ul>
        <li>choose a document on the right side to show preprocessing
        and coreference results
        <li>use some text of your own to
          <a href="#" onClick="display_newdoc()">import raw text</a>
          and perform coreference resolution on it
      </ul>
</div>
    </body>
</html>
