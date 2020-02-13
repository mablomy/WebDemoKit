/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function createGrNode(el) {
    var id = el["id"];
    var newDiv = createServer(el);    

    var labelTrxToRecover = document.createElement("div");
    labelTrxToRecover.setAttribute("id", id+"_trx_to_recover");
    labelTrxToRecover.setAttribute("style", "position: absolute; left: 0px; top: -5px; " +
                                   "background: red; color: white; font-size: 10px; font-family: Arial;");
    newDiv.appendChild(labelTrxToRecover);
    
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

