# Alexa skill ask my pc<hr>
Alexa java skill for opening files and web pages on a local computer running the skill with tomcat.
You can check out the skill in action at my youtube channel [here](https://youtu.be/UMA1FSwb1pw).

## Table Of Contents
- [Basic Concept](#basic-concept)
- [Setting up our environment](#setting-up-our-environment)
  - [Prerequisites](#prerequisites)
    - [Getting a static dns name from NOIP](#getting-a-static-dns-name-from-noip)
    - [Creating a self-signed certificate](#creating-a-self-signed-certificate)
    - [Installing Java Development Kit (JDK)](#installing-java-development-kit-jdk)
    - [Installing Apache Tomcat](#installing-apache-tomcat)
- [Example settings of the skill](#example-settings-of-the-skill)
  - [Example 1: Alexa ask computer to start excel](#example-1-alexa-ask-computer-to-start-excel)
  - [Example 2: Alexa ask computer to open facebook](#example-2-alexa-ask-computer-to-open-facebook)
- [Logs](#logs)


### Basic Concept
This is just a concept section, I will go thru all the steps as clearly as I can in the following sections.

The basic concept is a skill running as a Java Servlet on a local computer running a Tomcat web server,<br/>
Which means the local computer is the Endpoint for the alexa skill.<br/>
According to Amazon's demands regarding using a different Endpoint then lambda,<br/>
The Tomcat server must accept communication using Https protocol with the default port of 443,<br/>
Which means we're going to have to open this port on our router to allow incoming traffic.<br/>
And of course, if your router is dhcp mode, you will have to give your computer a static address inside your lan and forward all incoming traffic with the port 443 to that static address.<br/>
Another demand by Amazon is that the Tomcat server needs to present a valid certificate.<br/>
Amazon allows the use of a self-signed certificates as long as the skill is in development status,<br/>
Which is a good thing for us, because I don't think anyone would want to go live with a skill that does stuff on their local computer.<br/>
Other than that Amazon has a few more demands regarding implanting an alexa skill on an Https Endpoint,<br/>
But besides making sure the incoming request's application id belongs to the skill we created,<br/>
The Alexa Skill Kit for Java handles all of those demands for us.

The skill interface is configured for and Https Endpoint,<br/>
So, if you don't want to update the skill interface with the new ip address of your modem every time your isp changes it,<br/>
Then I would recommend using a dynamic dns service in the sorts of NOIP and use the updates app to update your modem address in the NO-IP servers.

The skill's invocation word is Computer, but you can use whatever invocation word or phrase you want,<br/>
Just remember that it's supposed to intuitive and that alexa needs to understand you.

After getting thru the communication part, the skill itself is pretty simple,<br/>
It has only one major intent called StartAction and that intent has only one custom type slot called Action.<br/>
The custom type for the slot is called *LIST_OF_ACTIONS* and it is actually a list for all the actions you're going to ask alexa to perform.

The skill grabs the Action slot value and in a Key-Value fashion retrieves the necessary action to perform from a designated json file called *action_map.json*.<br/>
The skill then tries to open the value retrieved as a File, and if it fails it tries to open it as a Uri,<br/>
Which means the value retrieved can by either a file or an http address to open in your default browser.<br/>
Think of the json file like mappings file, you match an "what-to-do" to any "action" you're going to ask alexa to perform.

### Setting up our environment
In this section I'll try to elaborate as much as I can,<br/>
If you're familliar with some of the actions described here, feel free to jump to next part.
#### Prerequisites
##### Getting a static dns name from NOIP
Create and account with https://www.noip.com/ and create a hostname to be used as a static name to access out web server.<br/>
Download and install NOIPs DNS Update Client (DUC) [here](https://www.noip.com/download?page=win), this softwere allows you to update the ip address recived from your isp periodicly.<br/>
Write down your chosen dns name, we will use it very soon.

#### Creating a self-signed certificate
Well, this part was actually the most fustrating part for me.<br/>
I had now prior knowledge of creating or even using certificates before working on this project,<br/>
There were even a couple of times I felt like quiting this project because of my difficulties overcoming this part,<br/>
But evnetually I found a way.<br/>
There are a couple of diffrent ways creating this certificate, maybe mine is not the oprimal one, but it works.<br/>
Lets get started.<br/>
- [ ] under construction

#### Installing Java Development Kit (JDK)
In order to be able to build and the project, you're going to need to intall Java JDK, I've used version 8.<br/>
You can find the correct distribution for your operating system [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), just download and install.<br/>
After we've finished installing JDK, we now need to add a couple of enviorment variables which will make it easy for us to use Java.<br/>.
You can either search windows (if you have windows 10) for the phrase "Edit the system environment variables",<br/>
or just right-click on This Pc/My Computer >> choose "Advanced system setting" >> and got into the "Advanced" Tab.<br/>
Now click on "Enviorment Variables...", our focus will be on the "System variables".<br/>
Click "New..." and the following variable:<br/>
Variable name: *JAVA_HOME*
Variable value: *C:\Program Files\Java\jdk1.8.0_121*<br/>
If you've installed java in a diffrent path or you've installed a diffrent version, updated this variable value accoradingly.<br/>

#### Installing Apache Tomcat
For a web server for hosting this skill, I've used Apache Tomcat 9.0.0.M22 https://tomcat.apache.org.<br/>
I've used the binary distribution, but you can also deploy it as a windows service.<br/>
You can download the core binary distribution [here](https://tomcat.apache.org), there is no installation involved when using the binary distribution, just open the downloaded zip file on any folder you see fit, just remmber that we're going to use this folder later on.

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
