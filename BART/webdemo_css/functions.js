function set_click_listener(node) {
    var s=e.getAttribute("set-id");
    e.addEventListener('click',
		       function() {set_activeset(s);},
		       true);
}

function prepare_nodes() {
  var corefNode=dojo.byId("coref");
  var markables=dojo.query("span",corefNode);
  for (var i=0; i<markables.length; i++) {
    e=markables[i];
    if (e.hasAttribute("set-id")) {
	set_click_listener(e);
	e.setAttribute("class", "coref-inactive");
    }
  }
}

function set_activeset(setid) {
    var corefNode=dojo.byId("coref");
    var markables=dojo.query("span",corefNode);
  //alert(markables);
  var s="";
  for (var i=0; i<markables.length; i++) {
    e=markables[i];
    //alert(e);
    if (e.hasAttribute("set-id")) {
        if (e.getAttribute("set-id")==setid) {
          e.setAttribute("class","coref-active");
          s+="<div>"+e.textContent+"</div>";
        } else {
          e.setAttribute("class","coref-inactive");
        }
      }
  }
  document.getElementById("coref-chain").innerHTML=s;
}

function display_wait(s) {
  var mainPanel=dojo.byId("leftpanel");
  mainPanel.innerHTML='<div class="waitingmsg">'+s+'<br/><img src="anicat30.gif"></div>';
}
function display_newdoc() {
  var mainPanel=document.getElementById("leftpanel");
  mainPanel.innerHTML='<b>Please enter text to preprocess:<b><br>'+
'<form name="newdoc_form"><textarea name="text" rows="10" cols="80">'+
'Enter some text that BART should preprocess and do coreference resolution on. If BART has found a cluster of coreferent mentions, they will be displayed using the same number'+
'</textarea><br/>'+
'<input type="button" value="Preprocess" onClick="submit_newtext()"/> </form>';
}
function display_newdoc_de() {
  var mainPanel=document.getElementById("leftpanel");
  mainPanel.innerHTML='<b>Please enter text to preprocess:<b><br>'+
'<form name="newdoc_form"><textarea name="text" rows="10" cols="80">'+
'Hier kommt Text hin, den BART parsen und auf Koreferenz hin untersuchen soll. Wenn BART eine Koreferenzkette findet, werden deren Nennungen farbig unterlegt.'+
'</textarea><br/>'+
'<input type="button" value="Preprocess" onClick="submit_newtext()"/> </form>';
}

function updateRight() {
    dojo.xhrGet({url:"/BARTDemo/ShowText/listDocs",
		load:function(result) {
		dojo.byId('rightpanel').innerHTML=result+'<hr>'+
		    '<a href="#" onclick="display_newdoc()">Create new document...</a>';
	    }});
}

function submit_newtext() {
    var txt=document.forms.newdoc_form.text.value;
    display_wait("Preprocessing Text...");
    dojo.xhrPost({url:"/BARTDemo/ShowText/addDoc/",
		postData:txt,
		load:function(result) {
		dojo.byId('leftpanel').innerHTML=result;
		updateRight();
	    }});
}

function renderDoc(docid,fmt) {
    dojo.xhrGet({url:"/BARTDemo/ShowText/renderDoc?docId="+docid+
		"&fmt="+fmt,
		load:function(result) {
		dojo.byId('leftpanel').innerHTML=result;
	    }});
}

function renderCoref(docid) {
    dojo.xhrGet({url:"/BARTDemo/ShowText/renderCoref?docId="+docid,
		load:function(result) {
		dojo.byId('leftpanel').innerHTML=result;
		prepare_nodes();
	    }});
}