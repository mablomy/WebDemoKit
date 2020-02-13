/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

