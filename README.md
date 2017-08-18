# Alexa skill ask my pc<hr>
Alexa custom skill written in java for opening files and web pages on a local computer running the skill as a web service with tomcat.
You can check out the skill in action on my youtube channel [here](https://youtu.be/UMA1FSwb1pw).

## Table Of Contents
- [Basic Concept](#basic-concept)
- [Setting up our environment](#setting-up-our-environment)
  - [Prerequisites](#prerequisites)
    - [Getting a static dns name from NOIP](#getting-a-static-dns-name-from-noip)
    - [Creating a self-signed certificate](#creating-a-self-signed-certificate)
    - [Installing Java Development Kit (JDK)](#installing-java-development-kit-jdk)
    - [Installing Apache Maven](#installing-apache-maven)
    - [Installing Apache Tomcat](#installing-apache-tomcat)
  - [Configuring](#configuring)
    - [Assigning a static ip for our computer](#assigning-a-static-ip-for-our-computer)
    - [Forwarding port 443 towards our static ip](#forwarding-port-443-towards-our-static-ip)
    - [Configuring Tomcat for https support](#configuring-tomcat-for-https-support)
    - [Configuring Tomcat default host name](#configuring-tomcat-default-host-name)
    - [Configuring Servlet and Servlet Mappings](#configuring-servlet-and-servlet-mappings)
- [Example settings of the skill](#example-settings-of-the-skill)
  - [Example 1: Alexa ask computer to start excel](#example-1-alexa-ask-computer-to-start-excel)
  - [Example 2: Alexa ask computer to open facebook](#example-2-alexa-ask-computer-to-open-facebook)
- [Logs](#logs)


### Basic Concept
This is just a concept section, I will go through all the steps as clearly as I can in the following sections.

The basic concept here is hosting an alexa custom skill as a web service.</br>
The skill is a build as a Java Servlet on a local computer running a Tomcat web server,<br/>
Which means the local computer is the Endpoint for the alexa skill.<br/>
According to Amazon's requirements regarding using a different Endpoint then lambda,<br/>
The Tomcat server must support Https protocol and accept requests with the default port of 443,<br/>
Which means we're going to have to open this port on our router to allow incoming traffic.<br/>
And of course, if your router is dhcp mode, you will have to give your computer a static address inside your lan and forward all incoming traffic with the port 443 to that static address.<br/>
Another requirements by Amazon is that the Tomcat server needs to present a valid certificate.<br/>
Amazon allows the use of a self-signed certificates as long as the skill is in development status,<br/>
Which is a good thing for us, because I don't think anyone would want to go live with a skill that does stuff on their local computer.<br/>
Other than those requirements Amazon has a few more regarding implanting an alexa skill on an Https Endpoint,<br/>
But besides making sure the incoming request's application id belongs to the skill we've created,<br/>
The Alexa Skill Kit for Java handles all of those requirements for us. You can read all about the requirements for hosting a custom alexa skill [here](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/developing-an-alexa-skill-as-a-web-service).

The skill interface needs to be configured with your modem ip address as an Https Endpoint,<br/>
So, if you don't want to update the skill interface with the new ip address of your modem every time your isp changes it,<br/>
Then I would recommend using a dynamic dns service in the sorts of NOIP and use the updates app to update your modem address in NOIP's servers.

The skill's invocation word is Computer, but you can use whatever invocation word or phrase you want,<br/>
Just remember that it's supposed to intuitive and that alexa needs to understand you.

After getting thru the communication part, the skill itself is pretty simple,<br/>
It has only one major intent called StartAction and that intent has only one custom type slot called Action.<br/>
The custom type for the slot is called *LIST_OF_ACTIONS* and it is actually a list for all the actions you're going to ask alexa to perform on you computer.

The skill grabs the Action slot value and in a Key-Value fashion retrieves the necessary action to perform from a designated json file called *action_map.json*.<br/>
The skill then tries to open the value retrieved as a File, and if it fails it tries to open it as a Uri,<br/>
Which means the value retrieved can by either a file or an web address to open in your default browser.<br/>
Think of the json file like a mappings file, you match a "what-to-do" to any "action" you're going to ask alexa to perform.

### Setting up our environment
In this section I'll try to elaborate as much as I can,<br/>
If you're familliar with some of the actions described here, feel free to jump to next part.
#### Prerequisites
##### Getting a static dns name from NOIP
Create an account with https://www.noip.com/ and set up a hostname to be used as a static name to access our web server.<br/>
Download and install NOIPs DNS Update Client (DUC) [here](https://www.noip.com/download?page=win), this softwere allows you to update the ip address received from your isp periodicly.<br/>
Write down your chosen dns name, we will use it very soon.

#### Creating a self-signed certificate
Well, this part was actually the most fustrating part for me.<br/>
I had now prior knowledge of creating or even using certificates before working on this project,<br/>
There were even a couple of times I felt like quiting this project because of my difficulties overcoming this part,<br/>
But evnetually I found a way.<br/>
There are a couple of diffrent ways creating this certificate, maybe mine is not the most optimal one, but it works!<br/>
So... Lets get started.<br/>
- [ ] under construction

#### Installing Java Development Kit (JDK)
In order to be able to build and the project we're going to use Maven.<br/>
One of Maven's requirements is an installation of JDK. so, you're going to need to intall Java JDK, I've used version 8.<br/>
You can find the correct distribution for your operating system [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), just download and install.<br/>
After we've finished installing JDK, we now need to add a couple of enviorment variables which will make it easier for us to use Java.<br/>.
You can either search windows (if you have windows 10) for the phrase "Edit the system environment variables",<br/>
or just right-click on This Pc/My Computer >> choose "Advanced system setting" >> and got into the "Advanced" Tab.<br/>
Now click on "Enviorment Variables...", our focus will be on the "System variables".<br/>
Click "New..." and the following variable:<br/>
Variable name: *JAVA_HOME*<br/>
Variable value: *C:\Program Files\Java\jdk1.8.0_121*<br/>
**Importent note:**<br/>
If you've installed java in a diffrent path or you've installed a diffrent version, updated this variable value accoradingly.<br/>
<br/>
To check the installation proccess, open a Command Prompt window and type *java -version* you should be getting the information about the installed Java softwere,<br/>
If it's not working properly, make sure the installation was finished and that you've added the *JAVA_HOME* variable correctly.<br/>
If you did everything ok, a computer reboot might be helpful.

#### Installing Apache Maven
[Maven](https://maven.apache.org) is a tool used for project managment, I've used it working on this project, which means we need in order to build the project eventually.<br/>
You can download the version I've used, 3.5.0 [here](https://maven.apache.org/download.cgi), this if of course a binary ditribution.<br/>
Unzip it in any folder you see fit, and in the same manner when we've installed the JDK, get yourself to the system enviorment variables screen.<br/>
Add the following 3 variables in "System variables" section:<br/>
Variable name: *M2_HOME*<br/>
Variable value: the folder you've extracted from the zip file, in my case its *E:\apache-maven-3.5.0*<br/>
Variable name: *MAVEN_OPTS*<br/>
Variable value: *-Xms256m -Xmx512m*<br/>
Variable name: *M2*<br/>
Variable value: *%M2_HOME%\bin*<br/>
Now we need to add to add a value into an existing variable in the "System variables" section (if it doesnt exits, create it):<br/>
Variable name: *Path*<br/>
Value to add: *%M2%*<br/>
**Importent note:**<br/>
If you're using Windows 10, the editing variables interface is very easy, just click "New" while you're inside the *Path* variable,<br/>
If you're working with an older version of Windows or you're editing the variable value in text mode, just go to the end of the current text and add *;* as a sperator before adding *%M2%*.<br/>
**DO NOT**, under any circumstances delete or edit any existing text inside this or any other pre-existing variables.<br/>
<br/>
To check the installation proccess, open a Command Prompt window and type *mvn -v* you should be getting the information about the deployed Maven tool,<br/>
If it's not working properly, make sure you hava java JDK installed properly and follow all the steps in the installing Maven section. If everything looks ok, a computer reboot might be helpful.

#### Installing Apache Tomcat
For a web server for hosting this skill, I've used [Apache Tomcat](https://tomcat.apache.org) 9.0.0.M22.<br/>
I've used the binary distribution, but you can also deploy it as a windows service.<br/>
You can download the core binary distribution [here](https://tomcat.apache.org), there is no installation involved when using the binary distribution, just open the downloaded zip file on any folder you see fit, just remember that we're going to use this folder later on.<br/>
Now, in the same manner when we've installed the JDK and Maven, get yourself to the system enviorment variables screen.<br/>
In the "System variables" section we're going to add the following variable:<br/>
Variable name: *CATALINA_HOME*
Variable value: the folder you've extracted from the zip file, in my case its *E:\apache-tomcat-9.0.0.M20*<br/>
We'll get back to configuring Tomcat later.

### Configuring
#### Assigning a static ip for our computer
This part you're going to have to on your own, because I can tell you how to do it if you use a TP-Ling AC1750 router, which is what I'm using.<br/>
But if you use a diffrent router, well... you're going to have to do this part on your own.<br/>
But don't worry, I'll give you the guidlines.<br/>
First of all, open up a Command Prompt windows and type *ipconfig /all*, identify your ethernet adapter and take note of the following:<br/>
Physical Address - is the mac address you're going to assign the static ip to on your router.<br/>
IPv4 Address - is the ip you're going to assign to the mac address on your router.<br/>
Default Gateway - is the ip address of your router, from your router's gui you will configure the static ip.<br/>
Now, open your favorite web browser and type the ip address of your router, which is the default gateway address we noted earlier.</br>
Type your user name and password if asked. If you don't know the user name and password, check under the router for a sticker indicating the default ones, if you can't find it, you'll have to dig around in google for your router's manufacture name and model, you'll probably find it listed somewhere. Once you're inside, it's highly recommended changing the password to something more private then default one.</br>
Now, look for anything related to *Address Reservation* or *Static IP* and create a record with your computer's mac address and the ip address you wrote down earlier.</br>
Depending on the router, you might be asked to reboot it. Go ahead and reboot it and your computer will receive a static ip address once you're done.</br>
Please note, this ip address is inside your lan only, it means nothing outside of your home.

#### Forwarding port 443 towards our static ip
Open your router's gui on your favorite web browser, the same as in the static ip section.</br>
Look for anything related to *Port Forwarding* or *Virtual Servers* and create a record directing the port 443 to static ip you've assigned for your computer.</br>
Depending on the router, you might be asked to reboot it. Go ahead and reboot it and once you're done, your router will redirect all incoming requests with the port 443 towards your computer. Now we need to make our computer accept those requests, in the next section.

#### Configuring Tomcat for https support
Now we need to make our Tomcat web server support https protocol. In order to do that, we need to define a Connector.</br>
Go to the tomcat folder, wherever you've extracted it, go into the *conf* subfolder and open the file server.xml in any text editor.</br></br>
Find the *Service* tag and add an https connector right under it. You can find an example of the connector in the [*http_connector.xml*](http_connector.xml) I've added to this project, just edit its content and copy it the full content to the *server.xml* file.</br>

Just make sure to update your chosen keystore password in the *keystorePass* property,</br>
And the path of the jks file in the *keystoreFile* property before saving.</br>

As far as the path goes, the path is relative and you can't use windows syntax, instead of '\\' use '/'.</br>
For example, if your jks file is in the *conf* folder inside the *tomcat* folder, and your file name is *keystore.jks*, your path will be */conf/keystore.jks*.</br>
I just want to recommend that you will comment out any other connectors that might be open. It doesn't really has anything to do with this skill, and it doesn't really matters what connectors you have open if you didn't forwarded their port on the router, it's just security tip. You can comment out the connectors by surround the connector like so:</br>
\<!-- UNWANTED CONNECTOR \-->.</br>
Your Tomcat web server is now supporting http protocol.

#### Configuring Tomcat default host name
This is not a must do step, you can skip it and it will not effect the skill activity at all.</br>
Nevertheless, I would recommend doing it for better log records.</br>
Look inside the *server.xml* for and *Engine* tag and edit the *defaultHost* property, instead of *localhost* type the dns name you've created with NOIP.

#### Configuring Servlet and Servlet Mappings
Now we need to map a pattern to our servlet, I know we didn't actually did the servlet part yet, so this maybe a little bit out of the blue, but bare with me.</br>
We need to create a servlet reference for tomcat and map a url pattern to it.</br>
Our servlet will eventually be *askmypc.AskMyPcServlet*, while *askmypc* as the package name and *AskMyPcServlet* as our servlet class name. And give our servlet a name, the name can be what ever you want. For now, the name will be *AskMyPc*.</br>
After referencing our servelt, we need to map our desired url pattern to it. Again, the url pattern can be what ever you want. For now, our url pattern will be */askmypc*.

### Example settings of the skill
#### Example 1: Alexa ask computer to start excel
For that purpose, we will:<br/>
1- add **excel** to the *LIST_OF_ACTIONS* custom slot type and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open excel.

2- add the line **"excel": "C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE"** to our *action_map.json* file.<br/>
Which means that for every time the skill receives the action named excel, the skill will then open C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE.

#### Example 2: Alexa ask computer to open facebook
For that purpose, we will:<br/>
1- add **facebook** to the *LIST_OF_ACTIONS* custom slot ype and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open facebook.

2- add the line **"facebook": "https://www.facebook.com/"** to our *action_map.json* file.<br/>
Which means that for every time the skill receives the action named facebook, the skill will then open https://www.facebook.com/ in our default web browser.

### Logs
