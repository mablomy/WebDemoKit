/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";


function createTpsMeter(el) {
    var BUFSIZE=100; // I failed to define it const
    
    var id = el["id"];
    var tpsdiv = document.createElementNS('http://www.w3.org/2000/svg','svg');
    tpsdiv.setAttribute("id", id);
    tpsdiv.setAttribute("style", "position: absolute; left: "+el["xPos"]+"; top: "+el["yPos"]+"px; width: 102px; height: 102px");
    var line;
    for(var i=0; i<BUFSIZE; ++i){
        line = document.createElementNS('http://www.w3.org/2000/svg','line');
        line.setAttribute("x1",el["xPos"]+i+1);
        line.setAttribute("y1",el["yPos"]+101);
        line.setAttribute("x2",el["xPos"]+i+1);
        line.setAttribute("y2",el["yPos"]+101);
        line.setAttribute("id", id+"_"+i)
        tpsdiv.append(line);
    }
    return tpsdiv;
}

function updateTpsMeter(domItem, el) {
    var BUFSIZE=100; // Failed to define it const
    
    if (div === null) {
        div=createTpsMeter(el);
        document.getElementById("main").appendChild(div);
    }
    var line;
    for (var i=0; i<BUFSIZE; ++i) {
        line = document.getElementById(el["id"]+_i);
        line.setAttribute("y2",el["yPos"]+101-el["tps"][i]);
    }
}
