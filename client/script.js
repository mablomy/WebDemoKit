// loads JSON status update via AJAX call from webserver and displays status on web page

"use strict";

var pid=0;

function startStop() {
    var data;
    
    if(pid!== 0) {
        $("#startbutton").text("Load WebDemoKit");
        clearInterval(pid);
        pid=0;
        return;
    }
    $("#main").empty();
   $.ajax({
    type: "GET",
    dataType: "json",
    url: "getStarted",
    data: data,
    success: function (data) {
        if (data["result"]) {
            $("#startbutton").text("Stop WebDemoKit");
            pid = setInterval(getServerState,1000);
        } else {
            $("#startbutton").text("Load WebDemoKit");
            if (pid !== 0) { 
                clearInterval(pid);
                pid = 0;
            }
        }
    },
    error: function (jqXHR, textStatus, errorThrown) {
    $("#startbutton").text("Load WebDemoKit");
        if (pid !== 0) { 
            clearInterval(pid);
            pid = 0;
        }
      console.log("Error in getServer->ajax()");
      console.log(jqXHR);
      console.log (textStatus);
      console.log (errorThrown);
    }
  }); 
}

function updateConnections(cis) {
    $("[id^=conn_]").remove();
    // Add connections
    for (var i=0; i<cis.length; ++i) {
        displayConnection(cis[i]);
    }
}

function getServerState() {
    // called 1/s to load and display config status
    var data; // JSON object, result from ajax cal
    
  $.ajax({
    type: "GET",
    dataType: "json",
    url: "getStatus",
    data: data,
    success: function (data) {
//        console.log("Anzahl Elements: "+data["elements"].length);
        for (var i=0; i<data["elements"].length; ++i) {
            displayElement(data["elements"][i]);
        }
        // Need to wait for elements to show up in DOM so that connections can be calculated
        window.setTimeout(updateConnections, 3000, data["connections"]);
    },
    error: function (jqXHR, textStatus, errorThrown) {
        $("#startbutton").text("Load WebDemoKit");
        if (pid !== 0) { 
            clearInterval(pid);
            pid = 0;
        }
      console.log("Error in getServer->ajax()");
      console.log(jqXHR);
      console.log (textStatus);
      console.log (errorThrown);
    }
  });
}

function displayElement(el) {
    
    var domItem = document.getElementById(el["id"]);
    switch(el["ObjType"]) {
    case "server":
        updateServer(domItem, el);
        break;
    case "router":
        updateRouter(domItem, el);
        break;
    case "GroupReplication":
        updateGR(domItem, el);
        break;
    case "LoadGenerator":
        updateLoadGenerator(domItem, el);
        break;
    default:
        console.log("Unknown element type: '"+el["ObjType"]+"'");
    }
}


function onClickLoadGenerator(id, url) {
    var data;
    var item = document.getElementById(id);

    if (item.style.background == "darkgray")
        item.style.background = "lightgray";
    else
        item.style.background = "darkgray";

  $.ajax({
    type: "GET",
    dataType: "json",
    url: url,
    data: data,
//    success: function (data) {   },
    error: function (jqXHR, textStatus, errorThrown) {
      console.log("Error in onClickLoadGenerator->ajax()");
      console.log(jqXHR);
      console.log (textStatus);
      console.log (errorThrown);
    }
  });  
}

function createLoadGenerator(el) {
    var filename;
    var id = el["id"];
    switch (el["color"]) {
//        case 1: filename="pen_black.png"; break;
        case 2: filename="pen_blue.png"; break;
        case 5: filename="pen_green.png"; break;
        case 9: filename="pen_red.png";break;
        default: filename=""; break;
    }
    var root = document.getElementById("main");
    var newButton = document.createElement("button");
    newButton.setAttribute("id", id);
    newButton.setAttribute("onclick","onClickLoadGenerator(\""+id+"\",\""+el["url"]+"\")");
    newButton.setAttribute("style", "position: absolute; left: "+el["xPos"]+"px; top: "+el["yPos"]+"px; background: lightgray");
    
    var newImgOn = document.createElement("img");
    newImgOn.setAttribute("id", id+"_img_on");
    newImgOn.setAttribute("src", filename);
    newImgOn.setAttribute("alt", filename+" is missing");
       
    newButton.appendChild(newImgOn);
    root.appendChild(newButton);
    return newButton;
//    console.log("button width="+newButton.style.width)
}

function updateLoadGenerator(domItem, el) {
    if (domItem === null)
        domItem = createLoadGenerator(el);
    if (el["running"]) {
        domItem.style.background = "darkgray";
//        document.getElementById(el["id"]+"_img_on").hidden = false;
//        document.getElementById(el["id"]+"_img_off").hidden = true;
    } else {
        domItem.style.background = "lightgray";
//        document.getElementById(el["id"]+"_img_on").hidden = true;
//        document.getElementById(el["id"]+"_img_off").hidden = false;
    }

}

function createGrNode(el) {
    var id = el["id"];
    var newDiv = createServer(el);    

    var labelTrxToRecover = document.createElement("div");
    labelTrxToRecover.setAttribute("id", id+"_trx_to_recover");
    labelTrxToRecover.setAttribute("style", "position: absolute; left: 0px; top: -5px; " +
                                   "background: red; color: white; font-size: 10px; font-family: Arial;");
    newDiv.appendChild(labelTrxToRecover);

    var imgReadOnly = document.createElement("img");
    imgReadOnly.setAttribute("id", id+"_read_only");
    imgReadOnly.setAttribute("src", "gr_readonly.png");
    imgReadOnly.setAttribute("style", "position: absolute; left: 0px; top: 0px");
    imgReadOnly.setAttribute("alt", "read_only.png is missing");
    newDiv.appendChild(imgReadOnly);
    
    return (newDiv);
}

function updateGrNode(el) {
    var RECOVER_OFFSET = 100;
    var OFFLINE_OFFSET = 200;
    var id = el["id"];
    var div = document.getElementById(id);
    updateServer(div, el);
    
    div.style.left = el["xPos"]+"px"; // y pos is set later. maybe animation
    
 
    var ttr = document.getElementById(id+"_trx_to_recover");
    if (el["trxToRecover"] === 0)
        ttr.hidden = true;
    else {
        ttr.innerHTML=el["trxToRecover"]+" trx";
        ttr.hidden = false;
    }
    
    if (el["superReadOnly"] && el["connected"])
        document.getElementById(id+"_read_only").hidden = false;
    else
        document.getElementById(id+"_read_only").hidden = true;
    
    if (el["nodeState"]==="ONLINE") {
        div.style.top = el["yPos"]+"px";
    } else if (el["nodeState"]==="RECOVERING") {
        div.style.top = (el["yPos"]+RECOVER_OFFSET)+"px";
    } else {
        div.style.top = (el["yPos"]+OFFLINE_OFFSET)+"px"; 
    }
    
}


function createGR(el) {
    var nodeCount = el["nodes"].length;
    var id = el["id"];
    var root = document.getElementById("main");

    var newDiv = document.createElement("div");
    newDiv.setAttribute("id", id);
    newDiv.setAttribute("style", "position: absolute; top: 10px; left: 500px; "+
                        "width: "+((nodeCount-1)*150+220)+"px; height: 180px; border: 4px solid blue; border-radius: 20px;");

    var labelGroupId = document.createElement("div");
    labelGroupId.setAttribute("class", "label");
    labelGroupId.setAttribute("id", id+"_label_group_id");
    labelGroupId.setAttribute("style", "width: 300px; position: relative; top: +5px; left: +5px");
    newDiv.appendChild(labelGroupId);
    
    var labelResilience = document.createElement("div");
    labelResilience.setAttribute("class", "label");
    labelResilience.setAttribute("id", id+"_label_resilience");
    labelResilience.setAttribute("style", "width: 300px; position: relative; top: +5px; left: +5px");
    newDiv.appendChild(labelResilience);
    
    for (var i=0; i<nodeCount; ++i) {
        newDiv.appendChild(createGrNode(el["nodes"][i]));
    }
    
    root.appendChild(newDiv);
    return newDiv;
}

function updateGR(div, el) {
    if (div === null) div=createGR(el);
    div.style.left = el["xPos"]+"px";
    div.style.top = el["yPos"]+"px";
    document.getElementById(el["id"]+"_label_resilience").innerHTML = el["resilience"];
    document.getElementById(el["id"]+"_label_group_id").innerHTML = el["uUID"];
    for(var i=0; i<el["nodes"].length; ++i) {
        updateGrNode(el["nodes"][i]);
    }
}

function createServer(el) {
    var id = el["id"];
    
    var newDiv = document.createElement("div");
    newDiv.setAttribute("id", id);
    newDiv.setAttribute("style", "position: absolute; top: 10px; left: 500px");
    var newImg1 = document.createElement("img");
    newImg1.setAttribute("id", id+"_server");
    newImg1.setAttribute("src", "mysql-server.png");
    newImg1.setAttribute("alt", "Server image missing");
    var newImg2 = document.createElement("img");
    newImg2.setAttribute("id", id+"_down");
    newImg2.setAttribute("src", "down.png");
    newImg2.setAttribute("style", "position: absolute; left: 0px; top: 0px");
    var newImg3 = document.createElement("img");
    newImg3.setAttribute("id", id+"_clone");
    newImg3.setAttribute("src", "clone-server.png");
    newImg3.setAttribute("alt", "Clone image missing");
    var newDiv1 = document.createElement("div");
    newDiv1.setAttribute("class", "label");
    newDiv1.setAttribute("id", id+"_label");
    newDiv1.setAttribute("style", "width: 100px");
    
    newDiv.appendChild(newImg1);
    newDiv.appendChild(newImg2);
    newDiv.appendChild(newImg3);
    newDiv.appendChild(newDiv1);
    
    return newDiv;
}


function updateServer(div, el) {
    if (div === null) {
        div=createServer(el);
        document.getElementById("main").appendChild(div);
    }
    div.style.left = el["xPos"]+"px";
    div.style.top = el["yPos"]+"px";
    if (el["connected"] && !el["cloning"]) {
        document.getElementById(el["id"]+"_clone").hidden = true;
        document.getElementById(el["id"]+"_down").hidden = true;
        document.getElementById(el["id"]+"_server").hidden = false;
//        console.log(document.getElementById(el["id"]+"_label").innerHtml);
        document.getElementById(el["id"]+"_label").innerHTML = el["hostname"]+":"+el["port"]+"<br>"+el["version"];
//        console.log(document.getElementById(el["id"]+"_label").innerHTML);

    } else if (el["connected"] && el["cloning"]) {
        document.getElementById(el["id"]+"_clone").hidden = false;
        document.getElementById(el["id"]+"_down").hidden = true;
        document.getElementById(el["id"]+"_server").hidden = true;
        document.getElementById(el["id"]+"_label").innerHTML = el["clonestate"];
    } else {
        // Server is down
        document.getElementById(el["id"]+"_clone").hidden = true;
        document.getElementById(el["id"]+"_down").hidden = false;
        document.getElementById(el["id"]+"_server").hidden = false;
        document.getElementById(el["id"]+"_label").innerHTML = el["hostname"]+":"+el["port"]+"<br>"+el["version"];
    }
}
        

function createRouter(el) {
    var id= el["id"];
    var root=document.getElementById("main");
    var newDiv = document.createElement("div");
    newDiv.setAttribute("id", id);
    newDiv.setAttribute("style", "position: absolute; top: 10px; left: 200px");
    var newImg1 = document.createElement("img");
    newImg1.setAttribute("id", id+"_router");
    newImg1.setAttribute("src", "router.png");
    newImg1.setAttribute("alt", "Router image missing");
    newImg1.setAttribute("title", "If router is connected,<br>you will see connection details here");
    var newImg2 = document.createElement("img");
    newImg2.setAttribute("id", id+"_down");
    newImg2.setAttribute("src", "down.png");
    newImg2.setAttribute("alt", "Down image missing");
    newImg2.setAttribute("style", "position: absolute; left: 12px; top: 0px");
    var newDiv1 = document.createElement("div");
    newDiv1.setAttribute("class", "label");
    newDiv1.setAttribute("id", id+"_label");
    newDiv1.setAttribute("style", "width: 100px; position: absolute; left: 100px; top: 0px");
    
    newDiv.appendChild(newImg1);
    newDiv.appendChild(newImg2);
    newDiv.appendChild(newDiv1);
    root.appendChild(newDiv);
    return newDiv;
}


function updateRouter(div, el) {
    if (div == null) div = createRouter(el);
    div.style.left = el["xPos"]+"px";
    div.style.top = el["yPos"]+"px";
    if (!el["connected"])
        document.getElementById(el["id"]+"_down").hidden=false;
    else
        document.getElementById(el["id"]+"_down").hidden=true;
    document.getElementById(el["id"]+"_router").title = el["routerstatus"];
    document.getElementById(el["id"]+"_label").innerHTML = "host: "+el["hostname"] + "<br>version: "+el["version"] + "<br>" + el["cxnstatus"];
}
