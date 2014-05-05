var tok_distance=4;
var layer_distance=6;
var layer_border=1;
var layer_padding=2;

function put_tokens(parentdiv,tokens,base_y)
{
    s_prefix='<div class="tokenbox" style="left:0px; top: '
	   +base_y+'px;\" id="'+ parentdiv.id +"t";
    var tmp=new Array();    
    for (var i=0; i<tokens.length; i++) {
	tmp.push(s_prefix);
	tmp.push(''+
	   +i+'">'
	    + tokens[i][0]+"<br>"+tokens[i][1]+
	    "</div>");
    }
    parentdiv.innerHTML=tmp.join('');
    chlds=parentdiv.childElements();
    var oldx=0;
    var offsets=new Array();
    for (i=0;i<chlds.length; i++) {
	tokcnt=chlds[i];
	tokcnt.style.left=(oldx+tok_distance/2)+"px";
	offsets.push(oldx);
	oldx+=tokcnt.offsetWidth+tok_distance;
    }
    offsets.push(oldx);
    return offsets;
}

function Layered_addMarkable(markable) {
    var mk_div=document.createElement("div");
    mk_div.addClassName("markable");
    mk_div.innerHTML=markable.text;
    this.parent.appendChild(mk_div);
    var my_y=this.y_offsets[markable.span[0]];
    for (var i=markable.span[0];i<=markable.span[1];i++) {
	if (my_y>this.y_offsets[i]) my_y=this.y_offsets[i];
    }
    if (markable.min_ids==undefined)
    {
	mk_div.style.left=(this.x_offsets[markable.span[0]]+
			   tok_distance/2)+"px";
	mk_div.style.width=(this.x_offsets[markable.span[1]+1]-
			    this.x_offsets[markable.span[0]]-
			    tok_distance-
			    2*layer_padding-
			    2*layer_border)+"px";
	my_y-=mk_div.offsetHeight+layer_distance;
    } else {
	mk_div.style.left=(this.x_offsets[markable.min_ids[0]]+
			   tok_distance/2)+"px";
	mk_div.style.width=(this.x_offsets[markable.min_ids[1]+1]-
			    this.x_offsets[markable.min_ids[0]]-
			    tok_distance-
			    2*layer_padding-
			    2*layer_border)+"px";
	my_y-=mk_div.offsetHeight+layer_distance;
	var mk_ext=document.createElement("div");
	mk_ext.addClassName("markable_ext");
	this.parent.appendChild(mk_ext);
	mk_ext.style.top=my_y+"px";
	mk_ext.style.left=(this.x_offsets[markable.span[0]]+
			   tok_distance/2)+"px";
	mk_ext.style.width=(this.x_offsets[markable.span[1]+1]-
			    this.x_offsets[markable.span[0]]-
			    tok_distance-
			    2*layer_padding-
			    2*layer_border)+"px";
	mk_ext.style.minHeight=mk_div.clientHeight-
	    2*layer_padding+"px";
    }
    mk_div.style.top=my_y+"px";
    for (i=markable.span[0];i<=markable.span[1];i++) {
	this.y_offsets[i]=my_y;
    }
    var cls=markable.cls;
    if (cls) {
	for (i=0;i<cls.length;i++) {
	    mk_div.addClassName(cls[i]);
	}
    } else {
	mk_div.addClassName("default-color");
    }
    return mk_div;
}

function Layered(parentdiv,tokens,base_y)
{
    this.tokens=tokens;
    this.parent=parentdiv;
    this.x_offsets=put_tokens(this.parent,tokens,base_y);
    this.y_offsets=new Array(tokens.length);
    for (i=0;i<tokens.length;i++) {
	this.y_offsets[i]=base_y;
    }
    this.addMarkable=Layered_addMarkable;
}

function addLayered(sentid,repr) {
    var new_div=document.createElement("div");
    new_div.addClassName("display");
    new_div.style.minHeight="250px";
    new_div.id="div_"+sentid;
    document.body.appendChild(new_div);
    obj=new Layered(new_div,repr[0],200);
    markables=repr[1];
    for (var i=0; i<markables.length; i++) {
	m=obj.addMarkable(markables[i]);
    }
}

function addBarrier(funcS) {
var new_barrier=document.createElement("div");
new_barrier.id="barrier";
new_barrier.onmouseover=function() { eval(funcS); };
new_barrier.innerHTML='<a href="javascript:'+funcS+'">more...</a>';
document.body.appendChild(new_barrier);
}

function removeBarrier() {
    $('barrier').remove();
}