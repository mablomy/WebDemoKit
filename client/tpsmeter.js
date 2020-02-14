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
    tpsdiv.setAttribute("style", "position: absolute; left: "+el["xPos"]+"px; top: "+el["yPos"]+"px; width: 104px; height: 104px");
    var box = document.createElementNS('http://www.w3.org/2000/svg','rect');
    box.setAttribute("style", "fill:rgb(230,230,250);stroke-width:2;stroke:rgb(0,0,0)");
    box.setAttribute("x", "0");
    box.setAttribute("y", "0");
    box.setAttribute("width", "104");
    box.setAttribute("height", "104");
    tpsdiv.append(box);
    var label1 = document.createElementNS('http://www.w3.org/2000/svg','text');
    label1.setAttribute("style", "fill:rgb(0,0,0); font-size: 12px; font-family: sans-serif");
    label1.setAttribute("x", "5");
    label1.setAttribute("y", "15");
    label1.setAttribute("width", "104");
    label1.setAttribute("height", "10");
    label1.setAttribute("id", id+"_tpslabel");
    tpsdiv.append(label1);
    var label2 = document.createElementNS('http://www.w3.org/2000/svg','text');
    label2.setAttribute("style", "fill:rgb(0,0,0); font-size: 12px; font-family: sans-serif");
    label2.setAttribute("x", "5");
    label2.setAttribute("y", "98");
    label2.setAttribute("width", "104");
    label2.setAttribute("height", "10");
    label2.setAttribute("id", id+"_hostlabel");
    tpsdiv.append(label2);
    var title=document.createElementNS('http://www.w3.org/2000/svg','title');
    title.setAttribute("id", id+"_title");
    tpsdiv.append(title);

    var line;
    for(var i=0; i<BUFSIZE; ++i){
        line = document.createElementNS('http://www.w3.org/2000/svg','line');
        line.setAttribute("x1",i+2);
        line.setAttribute("y1",102);
        line.setAttribute("x2",i+2);
        line.setAttribute("y2",102);
        line.setAttribute("id", id+"_"+i)
        line.setAttribute("style","stroke:rgb(120,120,120);stroke-width:1");
        tpsdiv.append(line);
    }
    return tpsdiv;
}

function updateTpsMeter(domItem, el) {
    var BUFSIZE=100; // Failed to define it const
    
    if (domItem === null) {
        domItem=createTpsMeter(el);
        document.getElementById("main").appendChild(domItem);
    }
    var line;
    var id = el["id"]+"_";

    document.getElementById(id+"title").textContent = "Last error: "+el["lastSqlError"]; 
    document.getElementById(id+"tpslabel").textContent = "max "+el["maxTps"]+"tps";   
    if (el["connected"]) 
        document.getElementById(id+"hostlabel").textContent = el["hostname"];
    else
        document.getElementById(id+"hostlabel").textContent = "N/C for "+el["waittime"]+"s";
    for (var i=0; i<BUFSIZE; ++i) {
        line = document.getElementById(id+i);
        line.setAttribute("y2",100-(el["tps"][i]*100/el["maxTps"]));
//        if(i===99)
//          console.log("tps(i)="+el["tps"][i]+" "+line.getAttribute("id")+": "+line.getAttribute("y2"));
    }
}
