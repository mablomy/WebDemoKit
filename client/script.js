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
    case "TPSMeter":
        updateTpsMeter(domItem, el);
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



        

