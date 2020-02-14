/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";


function getMidPoint (id) {
    var elem = document.getElementById(id);
    
    if (id.startsWith("server")) {
        return { "left": elem.getBoundingClientRect().left + window.pageXOffset + 30,
                 "top" : elem.getBoundingClientRect().top  + window.pageYOffset + 10 };
    } else if (id.startsWith("group_replication"))  {
	console.log ("Connection to a group_replication should not happen! See "+id);
        return { "left": elem.getBoundingClientRect().left + window.pageXOffset + (elem.scrollWidth /2),
                 "top" : elem.getBoundingClientRect().top  + window.pageYOffset + (elem.scrollHeight/2)};
    } else if (id.startsWith("load_generator")) {
	return { "left": elem.getBoundingClientRect().left + window.pageXOffset + 20,
                 "top" : elem.getBoundingClientRect().top  + window.pageYOffset + 3 };
    } else if (id.startsWith("router")) {
        return {"left": elem.getBoundingClientRect().left + window.pageXOffset + 40,
                "top" : elem.getBoundingClientRect().top  + window.pageYOffset + 5 };
    } else if (id.startsWith("tpsmeter")) {
        return {"left": elem.getBoundingClientRect().left + window.pageXOffset + 40,
                "top" : elem.getBoundingClientRect().top  + window.pageYOffset + 30 };
    }
    console.log("Connection to unknown element: "+id);
    return { "left": elem.getBoundingClientRect().left + window.pageXOffset + (elem.scrollWidth /2),
             "top" : elem.getBoundingClientRect().top  + window.pageYOffset + (elem.scrollHeight/2)};
}


function displayConnection(ci) {
    // only display connection if not yet present
    if (document.getElementById(ci["id"]) !== null) return;
    
//    var from = $("#"+ci["from"]);
//    var to   = $("#"+ci["to"]);
    var mid1 = getMidPoint(ci["from"]);
    var mid2 = getMidPoint(ci["to"]);
    var root = $("#main");
   
    var angle;
    if (mid1.left === mid2.left && mid1.top < mid2.top)
        angle=90;
    else if (mid1.left === mid2.left && mid1.top >= mid2.top)
        angle=-90;
    else if (mid2.left > mid1.left)
        angle=Math.atan((mid2.top - mid1.top)/(mid2.left - mid1.left))*360/2/Math.PI;
    else //mid2.left <= mid1.left
        angle=Math.atan((mid2.top - mid1.top)/(mid2.left - mid1.left))*360/2/Math.PI+180;
    var length = Math.sqrt(Math.pow((mid2.left - mid1.left),2) + Math.pow((mid2.top - mid1.top),2));
    
    var arrow = document.createElementNS('http://www.w3.org/2000/svg','svg');
    arrow.setAttribute("id", ci["id"]);
    arrow.setAttribute("style", "position: absolute; width:"+root.width()+"px; height:"+root.height()+"px");

    if(ci["type"]===1) {
        // Drawing a replication line
        var line1 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line1.setAttribute("x1",mid1.left);
        line1.setAttribute("y1",mid1.top - 5);
        line1.setAttribute("x2",mid1.left + length);
        line1.setAttribute("y2",mid1.top - 5);
        line1.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line1.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line2 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line2.setAttribute("x1",mid1.left);
        line2.setAttribute("y1",mid1.top + 5);
        line2.setAttribute("x2",mid1.left + length);
        line2.setAttribute("y2",mid1.top + 5);
        line2.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line2.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line4 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line4.setAttribute("x1",mid1.left+(length/2));
        line4.setAttribute("y1",mid1.top);
        line4.setAttribute("x2",mid1.left+(length/2)-5);
        line4.setAttribute("y2",mid1.top-5);
        line4.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line4.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line3 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line3.setAttribute("x1",mid1.left+(length/2));
        line3.setAttribute("y1",mid1.top);
        line3.setAttribute("x2",mid1.left+(length/2)-5);
        line3.setAttribute("y2",mid1.top+5);
        line3.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line3.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        arrow.appendChild(line1);
        arrow.appendChild(line2);
        arrow.appendChild(line3);
        arrow.appendChild(line4);
    } else {// client connection
        var line = document.createElementNS('http://www.w3.org/2000/svg','line');
        line.setAttribute("x1",mid1.left);
        line.setAttribute("y1",mid1.top);
        line.setAttribute("x2",mid1.left + length);
        line.setAttribute("y2",mid1.top);
        line.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line2 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line2.setAttribute("x1",mid1.left + (length/2));
        line2.setAttribute("y1",mid1.top);
        line2.setAttribute("x2",mid1.left + (length/2)-5);
        line2.setAttribute("y2",mid1.top - 5);
        line2.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line2.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line3 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line3.setAttribute("x1",mid1.left + (length/2));
        line3.setAttribute("y1",mid1.top);
        line3.setAttribute("x2",mid1.left + (length/2)-5);
        line3.setAttribute("y2",mid1.top + 5);
        line3.setAttribute("transform", "rotate("+angle+","+mid1.left+","+mid1.top+")");
        line3.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        arrow.appendChild(line);
        arrow.appendChild(line2);
        arrow.appendChild(line3);
    }
    root.prepend(arrow); 
}
