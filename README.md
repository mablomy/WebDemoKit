# WebDemoKit

A little webgui tool to show MySQL server activity, MySQL Router, replication, create some load and display it.
Used to showcase Replication, Group Replication, Replication Sets. 

## Getting Started

You need one machine to host this tool, which is essentially a webserver.

### Prerequisites

Having a JRE 1.8 should be enough. The machine must be reachable on port 8000. (not yet configurable)

### Installing

Cloning the GitHub repo is all you need. The repo does include the compiled executable. (I know, not the idea of GitHub...) 
The minimum that you need is: /dist/, /client/, /demokit.cfg

## Running WebDemoKit

Be sure to have your shell in the repo directory. Start the webserver:

```
java -jar dist/WebDemoKit.jar
```

Then access the webgui by browsing to your machine on port 8000:

```
http://<your machine>:8000/index.html
```

If you like to debug a little, try the REST call below to get raw JSON status data.

```
http://<your machine>:8000/getStatus
```

## Configuration

WebDemokIt is flexible. The webgui design is determined by the configuration file "demokit.cfg". This file defines all GUI elements and connection parameters for MySQL servers and Routers. Check the existing demokit.cfg file and manipulate as needed. (Ignore the first value "id". Must be present but the value is not used.)
After modifying the demokit.cfg file you need to either restart the webserver or click on "stop WebDemoKit" and "start WebDemoKit" to reload.
CAVE: Passwords must be given in clear text in the config file.

## Troubleshooting

Diagnostic output is given on the console that started the webserver. This might be real errors of WebDemoKit, connection problems to the MySQL servers or any weird status.


## Author and support

Created by Mario Beck. If you need help, try mario.beck (at) oracle.com

