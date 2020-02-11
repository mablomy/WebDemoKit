/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";



function displayConnection(ci) {
    // only display connection if not yet present
    if (document.getElementById(ci["id"]) !== null) return;
    
    var from = $("#"+ci["from"]);
    var to   = $("#"+ci["to"]);
    var root = $("#main");
    
    var x1 = from.position().left + (parseInt(from.css("width")) /2);
    var y1 = from.position().top  + (parseInt(from.css("height"))/2);
    var x2 =   to.position().left + (parseInt(  to.css("width")) /2);
    var y2 =   to.position().top  + (parseInt(  to.css("height"))/2);
    
    var angle;
    if (x1===x2 && y1<y2)
        angle=90;
    else if (x1===x2 && y1>=y2)
        angle=-90;
    else if (x2>x1)
        angle=Math.atan((y2-y1)/(x2-x1))*360/2/Math.PI;
    else //x2<=x1
        angle=Math.atan((y2-y1)/(x2-x1))*360/2/Math.PI+180;
    var length = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
    
    var arrow = document.createElementNS('http://www.w3.org/2000/svg','svg');
    arrow.setAttribute("id", ci["id"]);
    arrow.setAttribute("style", "position: absolute; width:"+root.width()+"px; height:"+root.height()+"px");

    if(ci["type"]===1) {
        // Drawing a replication line
        var line1 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line1.setAttribute("x1",x1);
        line1.setAttribute("y1",y1-5);
        line1.setAttribute("x2",x1+length);
        line1.setAttribute("y2",y1-5);
        line1.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line1.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line2 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line2.setAttribute("x1",x1);
        line2.setAttribute("y1",y1+5);
        line2.setAttribute("x2",x1+length);
        line2.setAttribute("y2",y1+5);
        line2.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line2.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line4 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line4.setAttribute("x1",x1+(length/2));
        line4.setAttribute("y1",y1);
        line4.setAttribute("x2",x1+(length/2)-5);
        line4.setAttribute("y2",y1-5);
        line4.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line4.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line3 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line3.setAttribute("x1",x1+(length/2));
        line3.setAttribute("y1",y1);
        line3.setAttribute("x2",x1+(length/2)-5);
        line3.setAttribute("y2",y1+5);
        line3.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line3.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        arrow.appendChild(line1);
        arrow.appendChild(line2);
        arrow.appendChild(line3);
        arrow.appendChild(line4);
    } else {// client connection
        var line = document.createElementNS('http://www.w3.org/2000/svg','line');
        line.setAttribute("x1",x1);
        line.setAttribute("y1",y1);
        line.setAttribute("x2",x1+length);
        line.setAttribute("y2",y1);
        line.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line2 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line2.setAttribute("x1",x1+(length/2));
        line2.setAttribute("y1",y1);
        line2.setAttribute("x2",x1+(length/2)-5);
        line2.setAttribute("y2",y1-5);
        line2.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line2.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        var line3 = document.createElementNS('http://www.w3.org/2000/svg','line');
        line3.setAttribute("x1",x1+(length/2));
        line3.setAttribute("y1",y1);
        line3.setAttribute("x2",x1+(length/2)-5);
        line3.setAttribute("y2",y1+5);
        line3.setAttribute("transform", "rotate("+angle+","+x1+","+y1+")");
        line3.setAttribute("style","stroke:rgb(30,30,30);stroke-width:2");
        arrow.appendChild(line);
        arrow.appendChild(line2);
        arrow.appendChild(line3);
    }
    root.prepend(arrow); 
}
