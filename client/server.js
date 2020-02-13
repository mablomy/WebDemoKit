/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    var imgReadOnly = document.createElement("img");
    imgReadOnly.setAttribute("id", id+"_read_only");
    imgReadOnly.setAttribute("src", "gr_readonly.png");
    imgReadOnly.setAttribute("style", "position: absolute; left: 0px; top: 0px");
    imgReadOnly.setAttribute("alt", "read_only.png is missing");

    newDiv.appendChild(newImg1);
    newDiv.appendChild(newImg2);
    newDiv.appendChild(newImg3);
    newDiv.appendChild(newDiv1);
    newDiv.appendChild(imgReadOnly);

    return newDiv;
}


function updateServer(div, el) {
    if (div === null) {
        div=createServer(el);
        document.getElementById("main").appendChild(div);
    }
    var id=el["id"];
    div.style.left = el["xPos"]+"px";
    div.style.top = el["yPos"]+"px";
    if (el["connected"] && !el["cloning"]) {
        document.getElementById(id+"_clone").hidden = true;
        document.getElementById(id+"_down").hidden = true;
        document.getElementById(id+"_server").hidden = false;
//        console.log(document.getElementById(el["id"]+"_label").innerHtml);
        document.getElementById(id+"_label").innerHTML = el["hostname"]+":"+el["port"]+"<br>"+el["version"];
//        console.log(document.getElementById(el["id"]+"_label").innerHTML);
        if (el["superReadOnly"])
            document.getElementById(id+"_read_only").hidden = false;
        else
            document.getElementById(id+"_read_only").hidden = true;

    } else if (el["connected"] && el["cloning"]) {
        document.getElementById(id+"_clone").hidden = false;
        document.getElementById(id+"_down").hidden = true;
        document.getElementById(id+"_server").hidden = true;
        document.getElementById(id+"_label").innerHTML = el["clonestate"];
        document.getElementById(id+"_read_only").hidden = true;
    } else {
        // Server is down
        document.getElementById(id+"_clone").hidden = true;
        document.getElementById(id+"_down").hidden = false;
        document.getElementById(id+"_server").hidden = false;
        document.getElementById(id+"_label").innerHTML = el["hostname"]+":"+el["port"]+"<br>"+el["version"];
        document.getElementById(id+"_read_only").hidden = true;
    }
}


