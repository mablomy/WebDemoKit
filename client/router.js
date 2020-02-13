/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

