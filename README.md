# Alexa skill ask my pc<hr>
Alexa custom skill written in java for opening files and web pages on a local computer running the skill as a web service with tomcat.
You can check out the skill in action on my youtube channel [here](https://youtu.be/UMA1FSwb1pw).</br>

This skill's flow is the shortest one possible:</br>
*Alexa enabled device \--> Alexa servers \--> Your local computer*</br>
which guarantees faster responses.</br>

Please follow this guide as closely as you can to insure a working skill, I would recommend reading it thrugh before actually following it.

## Table Of Contents
- [Release of liability](#release-of-liability)
- [Basic concept](#basic-concept)
- [Setting up our environment](#setting-up-our-environment)
  - [Prerequisites](#prerequisites)
    - [Getting a static dns name from NOIP](#getting-a-static-dns-name-from-noip)
    - [Installing Java Development Kit (JDK)](#installing-java-development-kit-jdk)
    - [Installing Apache Maven](#installing-apache-maven)
    - [Installing Apache Tomcat](#installing-apache-tomcat)
    - [Creating a self-signed certificate](#creating-a-self-signed-certificate)
      - [Creating the certificate](#creating-the-certificate)
      - [Creating the truststore](#creating-the-truststore)
    - [Prerequisites summary](#prerequisites-summary)      
  - [Configuring](#configuring)
    - [Assigning a static ip for our computer](#assigning-a-static-ip-for-our-computer)
    - [Forwarding port 443 towards our static ip](#forwarding-port-443-towards-our-static-ip)
    - [Configuring Tomcat for https support](#configuring-tomcat-for-https-support)
    - [Configuring Tomcat default host name](#configuring-tomcat-default-host-name)
    - [Configuring Servlet and Servlet Mappings](#configuring-servlet-and-servlet-mappings)
    - [Constructing our endpoint url](#constructing-our-endpoint-url)
    - [Configuring summary](#configuring-summary)
- [Creating our skill](#creating-our-skill)
  - [Setting up a skill interface with alexa](#setting-up-a-skill-interface-with-alexa)
  - [Creating and deploying our skill](#creating-and-deploying-our-skill)
  - [Start Tomcat web server](#start-tomcat-web-server)
- [Testing our skill](#testing-our-skill)
- [Example settings of the skill](#example-settings-of-the-skill)
  - [Example 1: Alexa ask computer to start excel](#example-1-alexa-ask-computer-to-start-excel)
  - [Example 2: Alexa ask computer to open facebook](#example-2-alexa-ask-computer-to-open-facebook)
- [Log files](#log-files)

### Release of liability
This is not an official guide, I just wrote about what I did to make this skill work, so other people who want to can do the same.</br>
Please consider the instructions in this guide as guidelines only, following them is at your own risk.</br>
I take no responsibility for any wrong doings on your computer made by yourself or any other.</br>
If at any point you feel not sure or you're concern your personal security maybe breached, please stop following this guide, rollback any changes you might have made and if need, please contact an it expert to help you fix your computer security.</br>
This project is designed for personal use only, it's in no way a commercial skill, it is not meant to profit from by me or any other.

### Basic concept
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

##### Installing Java Development Kit (JDK)
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

##### Installing Apache Maven
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

##### Installing Apache Tomcat
For a web server for hosting this skill, I've used [Apache Tomcat](https://tomcat.apache.org) 9.0.0.M22.<br/>
I've used the binary distribution, but you can also deploy it as a windows service.<br/>
You can download the core binary distribution [here](https://tomcat.apache.org), there is no installation involved when using the binary distribution, just open the downloaded zip file on any folder you see fit, just remember that we're going to use this folder later on.<br/>
Now, in the same manner when we've installed the JDK and Maven, get yourself to the system enviorment variables screen.<br/>
In the "System variables" section we're going to add the following variable:<br/>
Variable name: *CATALINA_HOME*
Variable value: the folder you've extracted from the zip file, in my case its *E:\apache-tomcat-9.0.0.M20*<br/>
We'll get back to configuring Tomcat later.

##### Creating a self-signed certificate
Now we need to to create a self-signed certificate that will be presented by our web-server and accepted by alexa.</br>
We'll split this task into two parts:</br>
The first part will be creating the private-key certificate, a configuration file and a public certificate for alexa using *openSSL*, so that alexa's servers can identify our web server.</br>
The second part will creating a truststore and importing our certificate into it with *Java keytool* so that our Tomcat web server can access present our certificate upon requests.<br>
**Important Note:** all the certificate related files created in the following sections, **should be stored in a secure location for your eyes only**, anyone who gets a hold of this files can identify himself as your web server.

###### Creating the certificate
Creating the certificate itself is prety straightforward, we're going to follow amazon's guide for creating a self-signed certificate, you can find it [here](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/testing-an-alexa-skill#create-a-private-key-and-self-signed-certificate-for-testing), and use *OpenSSL for Windows*, you can download the binary distribution [here](http://gnuwin32.sourceforge.net/packages/openssl.htm).</br>
Extract the downloaded *openssl* zip file wherever you see fit, open a Command Prompt windows **with administrative privileges** (right click, run as administrator), navigate to the folder you've extracted the zip file to and go into the subfolder *bin*.</br>
Type in the following command to create a private key:</br>
*openssl genrsa -out private-key.pem 2048*<br>
A private key certificate called *private-key.pem* is now created inside the *bin* folder.</br>

Copy the [*configuration.cnf*](configuration.cnf) file (taken from amazon's guide) I've added to this repository into the *bin* with the newly created *private-key.pem* file and open the it in any text editor.</br>
The most important part here is to replace the value of *DNS.1* with the dns static name received from *NOIP* otherwise the the incoming requests towards this dns name won't be able to identify your web server with the created certificate.</br>
You also need to update the following parameters:
- ST: Provide your two letter state abbreviation
- L: Provide the name of the city in which you are located
- O: Provide a name for your organization
- CN: Provide a name for the skill

When you done editing the cnf file, save it and get back to the Command Prompt windows, if its closed, re-open it (as administrator) and navigate again to the *bin* folder and run the following command in order to create a x509 public certificate valid for 365 days (you can change it if you want), based on your *private-key.pem* and *configuration.cnf* files:</br>
*openssl req -new -x509 -days 365 -key private-key.pem -config configuration.cnf -out certificate.pem*</br>
This will create a public certificate called *certificate.pem* inside the *bin* folder. This will be the certificate file we'll update in the [skill interface section](#setting-up-a-skill-interface-with-alexa) so that alexa's servers can identify our web server.

###### Creating the truststore
We need to create a truststore and import our certificate into it so that it can be accessed by our Tomcat web server. This will be accomplished using the *Java keytool* which is included in our *JDK* installation and the *openSSL* tool we used to create the certificate. We'll follow [Oracle's guide](https://docs.oracle.com/cd/E35976_01/server.740/es_admin/src/tadm_ssl_convert_pem_to_jks.html).</br>

The first thing we need to do is convert the created *certificate.pem* file into a *.pkcs12* which is acceptable by the standarts of the java truststore. Open a Command Prompt windows **with administrative privileges** (right click, run as administrator) and navigate to the *bin* folder inside you *openSSL* distribution which for now also holds the private-key, configuration file and certificate we created in the prior section, type:</br>
*openssl pkcs12 -export -out certificate.pkcs12 -in certificate.pem*</br>
This will generate a *certificate.pkcs12* file in openssl's bin subfolder. This will be the certificate presented by our web server, to accomplished that we will need to create a Java Truststore (jks file) and import and *certificate.pkcs12* into it.</br>

Navigate windows explorer to *%java_home%\bin* (start --> run) and move the created *certificate.pkcs12* into it. At this point I advice you to move the other three files from openssl's bin folder (private-key, configuration, certificate) to a secure folder accessible only to you.</br>
Open a Command Prompt windows **with administrative privileges** (right click, run as administrator) and type *cd %java_home%\bin* and press enter, this will navigate to the bin folder inside your jdk's installation.</br>
Now we need to create a *truststore*, empty it, and import our pkcs12 file into it. To create the initial truststore type:</br>
*keytool -genkey -keyalg RSA -alias endeca -keystore keystore.ks*</br>
When creating the keystore file you will be asked to enter your personal data, you can enter whatever you want, it doesn't really matters because we're going to empty the store, the only thing thats actually matters at this point is your *keystore password* which will be used by Tomcat to access the keystore, **write it down**.</br>
Once done, this will generate a keystore.ks file inside jdk's bin subfolder, now lets empty it using the following command:</br>
*keytool -delete -alias endeca -keystore keystore.ks*</br>
After emptying the keystore, lets import our pkcs12 file using the following command:</br>
*keytool -v -importkeystore -srckeystore certificate.pkcs12 -srcstoretype PKCS12 -destkeystore keystore.ks -deststoretype JKS*</br>
You will be asked if you "Trust this certificate?", well... you should, you've created and signed it :-). Type yes and press enter.
Now change the type of your *keystore.ks* file from ks to jks, *keystore.ks* will become *keystore.jks*.</br>
Move the *keystore.jks* file and the *certificate.pkcs12* to a secure folder accessible only to you, **please note** the only file need to accesible for Tomcat is the *keystore.jks* file and the only way to access it is using the keystore password you wrote down earlier.

###### Prerequisites summary
If you got to this point of the guide, you now have:
- A static dns name from NOIP which will be used to send requests towards your computer.
- A fully configured JDK installation that will allow you to work with Maven.
- A fully configured Maven distribution that will allow you to package your project.
- A (not yet configured) Tomcat distribution that will act as your web server and process all incoming requests.
- A self-signed x509 public certificate that will help alexa's servers identify your web server (*certificate.pem*).
- A Java Truststore file which holds your certificate in it and will be accessed with your selected password by your web server (*keystore.jks*) in order to present your certificate upon all incoming requests.

If only some or none of the above sounds familiar to you, you might want to go back to the appropriate section before continuing to configuring section.

#### Configuring
##### Assigning a static ip for our computer
The first thing we'll have to do is give our computer a permanent ip so that we can direct all incoming requests with the port 443 to it.</br>
This part you're going to have to on your own, because I can tell you how to do it if you use a TP-Link AC1750 router, which is what I'm using.<br/>
But if you use a diffrent router, well... you're going to have to do this part on your own.<br/>
But don't worry, I'll give you the guidelines.<br/>
First of all, open up a Command Prompt windows and type *ipconfig /all*, identify your ethernet adapter and take note of the following:<br/>
Physical Address - is the mac address you're going to assign the static ip to on your router. You can look at it as the name of your computer.<br/>
IPv4 Address - is the ip address assign to your computer dynamiclly by your router, you're going to assign it to the mac address on your router as a static ip.<br/>
Default Gateway - is the ip address of your router, from your router's gui you will configure the static ip.<br/>

Now, open your favorite web browser and type the ip address of your router, which is the default gateway address we noted earlier.</br>
Type your user name and password if asked. If you don't know the user name and password, check under the router for a sticker indicating the default ones, if you can't find it, you'll have to dig around in google for your router's manufacture name and model, you'll probably find it listed somewhere. Once you're inside, it's highly recommended changing the password to something more private then default one.</br>
Now, look for anything related to *Address Reservation* or *Static IP* and create a record with your computer's mac and the ip addresses you wrote down earlier.</br>
Depending on the router, you might be asked to reboot it. Go ahead and reboot it and your computer will receive a static ip address once you're done.</br>
Please note, this ip address is inside your lan only, it means nothing outside of your home.

##### Forwarding port 443 towards our static ip
Now that your computer has a static ip address, we can forward all incoming requests with the port 443 towards it.</br>
Open your router's gui on your favorite web browser, the same as we did in the static ip section.</br>
Look for anything related to *Port Forwarding* or *Virtual Servers* and create a record directing the port 443 to the static ip you've assigned to your computer.</br>
Depending on the router, you might be asked to reboot it. Go ahead and reboot it and once you're done, your router will redirect all incoming requests with the port 443 towards your computer. Now we need to make our computer accept these requests, in the next section.

##### Configuring Tomcat for https support
Now that we know all incoming requests with the port 443 will be forwarded from our router to our computer, we need to tell our web server to accept these requests. We need to make our Tomcat web server support https protocol and present our certificate for all incoming requests. In order to do that, we need to define a Connector.</br>
Go to the tomcat folder, wherever you've extracted it, go into the *conf* subfolder and open the file server.xml in any text editor.</br>

Find the *Service* tag and add an https connector right under it. You can find an example of the connector in the [*http_connector.xml*](http_connector.xml) file I've added to this project, just edit its content and copy its full content to the *server.xml* file.</br>

Before saving the *server.xml* file make sure to update your chosen keystore password in the *keystorePass* property,</br>
And the path of the jks file in the *keystoreFile* property.</br>

As far as the path goes, the path is relative and you can't use windows syntax, instead of '\\' use '/'.</br>
For example, if your jks file is in the *conf* folder inside the *tomcat* folder, and your file name is *keystore.jks*, your path will be */conf/keystore.jks*.</br>

Your Tomcat web server is now supporting https protocol and will present your self-signed certificate upon each incoming request in https protocol with the port 443.</br>

I just want to recommend that you will comment out any other connectors that might be open. It doesn't really has anything to do with this skill, and it doesn't really matters what connectors you have open if you didn't forwarded their port on the router, it's just security tip. You can comment out the connectors by surround the connector like so:</br>
\<!-- UNWANTED CONNECTOR \-->.</br>
Uncommenting is just a recommendation, this it not a "must do" step, if you're unsure, it's better to skip this part then to damage the xml syntax. 

##### Configuring Tomcat default host name
This is not a "must do" step, you can skip it and it will not effect the skill activity at all.</br>
Nevertheless, I would recommend doing it for better log records.</br>
Look inside the *server.xml* for and *Engine* tag and edit the *defaultHost* property, instead of *localhost* type the dns name you've created with NOIP.

##### Configuring Servlet and Servlet Mappings
Now that all incoming requests in https protocol with the port 443 will reach our web server that will present our certificate, we need to tell our web server what to do with these requests. We want our tomcat server to direct this requests towards our skill.</br>

Our skill is actually a java class which extends amazon's SpeechletServlet class which implements a java ee servlet. Our servlet class name will be *AskMyPcServlet* and it will reside inside a package called *askmypc* which makes our qualified class name *askmypc.AskMyPcServlet* **write it down**.</br>
Our servlet's only job will be to create a new instance of our speechlet class which implements amazon's Speechlet Class.</br>
Our speechlet class will be called *AskMyPcSpeechlet* and it will reside inside the same package as our servlet. **Our AskMyPcSpeechlet class is the code behind our skill** and it was written based on and using the [Alexa Skills Kit for Java](https://github.com/amzn/alexa-skills-kit-java)</br>.

Now, we're not creating the skill yet, this is just the part where we tell our web server where to direct incoming requests to.
In order to do that, we need to create a servlet reference for tomcat and map a url pattern to it.</br>
Our servlet class qualified name *askmypc.AskMyPcServlet*, while *askmypc* is the package name and *AskMyPcServlet* is our servlet class name. We need to give our servlet a name, the name can be what ever you want, think of it as a reference name. For now, the reference name will be *AskMyPc* and it will be mapped to our servlet class.</br>
After referencing our servelt, we need to map our desired url pattern to it. Again, the url pattern can be what ever you want. For now, our url pattern will be */askmypc*. We need to map our url pattern to the reference name of our servlet class.</br>

Go to your tomcat folder, and open the subfolder webapps, ROOT, WEB-INF. Open the file *web.xml* and find the tag *web-app*.</br>
Inside the *web-app* tag add the content from the file [*servlet_mapping.xml*](servlet_mapping.xml) file I've added to this project.</br>
If you want to change the url pattern before copying the content, you can, just **write down** your selected pattern for later use.</br>
Once you're done, you may not have a working skill yet, but you do have an endpoint url for it.

##### Constructing our endpoint url
Now, before we can create our skill interface with alexa, lets prepare our endpoint url. The endpoint url will constructed like this:
- the protocol we've defined in our tom connector: '*https://*'
- the dns name we got from NOIP: '*mydomainname.whatever*'
- the port we've forwarded on our router: '*:443*'
- the url pattern we've mapped to our servlet: '*/askmypc*'

The end result will be something like: *https://mydomainname.whatever:443/askmypc*</br>
This is your endpoint url, **write it down** and lets set up our skill interface.

##### Configuring summary
If you made it here, you now have:
- A router configured to forward all incoming requests with the port 443 to your web server.
- A fully working static named web server that accepts and handles requests in https protocol with port 443 and presents a self-signed certificate.
- A url pattern mapped to a servlet class for handling all requests.
- An endpoint url which will direct request from all over the world to your web server.

If only some or none of the above sounds familiar to you, you might want to go back to the appropriate section before continuing to skill creating section.

### Creating our skill
#### Setting up a skill interface with alexa
Open your web browser and sign in with your amazon developer account [here](https://developer.amazon.com/). If you don't have an account, create it.</br>
Once you're in, click *Alexa* and then click *Get Started* in the *Alexa Skills Kit* section, then click *Add New Skill* and set up you skill like so:</br>
Skill Information tab, is where we define how to invoke our skill. Fill in the next parameters, leave the rest as it is:
- *Name*: AskMyPc, you can name the skill what ever you want, your not going to go live with this skill so it doesn't really matters.
- *Invocation Name*: Computer, this is important, the invocation name is the way you ask alexa to start the skill. *Alexa, start computer*. If you don't want to use *Computer* as an invocation name, just make sure to take a look [here](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/choosing-the-invocation-name-for-an-alexa-skill#invocation-name-requirements) before choosing a good working invocation name.
Click *Save* and **write down your generated *Application Id*.**</br>

Interaction Model tab, is where we define how to interact with our skill:
- *Intent Schema*: copy the content from the the file I've added to this project: [*speechAssets/intentSchema.json*](speechAssets/intentSchema.json). This is basicly just telling alexa what are the intents our code can handle.
- *Custom Slot Types*: Type *LIST_OF_ACTIONS* to create a new custom slot which is mapped to the *StartAction* intent in the Intent Schema.
- *Enter Values*: copy the content from the file I've added to this project: [*speechAssets/LIST_OF_ACTIONS.txt*](speechAssets/LIST_OF_ACTIONS.txt) to create a list of values valid for our custom slot and click *Add* to add the slot. Please note that this is just a basic default list I've created, once you get the skill working, you will be able to change it as much as you want.
- *Sample Utterances*: copy the content from the file I've added to this project [*speechAssets/sampleUtterances.txt*](speechAssets/sampleUtterances.txt) and click *Save* and *Next*. this is actually the part where we teach alexa how to parse our sentences and phrases and tell where in the phrase she should expect our custom slot value. You can add as many sample utterances as you want, the more the better, just try not to confuse alexa with poorly built utterances.

Configuration tab, is where we define our skill endpoint:
- *Service Endpoint Type*: HTTPS.
- *Default*: paste your endpoint url like you've constructed [here](#constructing-our-endpoint-url).
- *Provide geographical region endpoints*: No. Click *Save* and *Next*.

SSL Certificate tab, is where we set up our certificate:
- *Certificate for DEFAULT Endpoint*: I will upload a self-signed certificate in X.509 format. Again, because we are not going to go live with this skill, a self-signed certificate will suffice.
- In the text box copy **all** the content of *certificate.pem* file we've created [here](#creating-the-certificate) (including the *----BEGIN CERTIFICATE----* and the *-----END CERTIFICATE-----* parts). This is the part where we tell the skill interface what is the certificate will be presented to our web server. Click *Save* and *Next*.

Test tab, this is where we can test our skill.</br>
Now, we can't test is yet because we have'nt finished it yet, but we need to get our user id for finishing up our skill, so if you don't know it already, this is the fastest way I know how to get it.</br>
Scroll down to the *Service Simulator* section and type whatever you want as an utterance (it's not going to work anyway) and click *Ask AskMyPc*.</br>
Take a look at the *Service Request* window, find and **write down the value of the *userId* parameter.**

#### Creating and deploying our skill
Download this repository as a zip file and extract it wherever you want, open the folder *src, main, java, askmypc* and open the file *AskMyPcSpeechlet.java* in any text editor:
- Edit the value of *APPLICATIONID*
- Edit the value of *USERID*

with the application and user ids you wrote down [here](#setting-up-a-skill-interface-with-alexa) and save the file.</br>
This is actually how we limit our skill to recieve requests only from our own skill and our own user.</br>

Open a Command Prompt window and navigate to location you've extracted the downloaded zip file, make sure you are in the same folder of the *pom.xml* file which in the maven instructions file on how to create the package.</br>
Type **mvn package**, if you did everything correctly you are suppose to see the package being build by Maven.</br>
Once the build is finished and you see a *BUILD SUCCESS* message on the Command Prompt window, you can close this window and go back to the extracted zip file.</br>
You should see a new folder created named *target*, open it and copy the following to your Tomcat server folder:
- Copy the file *AskMyPc-1.0.0.jar* to the folder *webapps/ROOT/WEB-INF/lib*. This is the skill itself.
- Copy the folder *conf_files* to the folder *webapps/ROOT/WEB-INF*. This folder holds the configuration file you'll use to map actions to slot values. I've added a couple of examples for this configutarion file [here](#example-settings-of-the-skill).
That's it. You skill is now deployed to your web server, not we need to start the server.</br>

You can now delete the extracted zip file of this repository, you have no need for it anymore.

#### Start Tomcat web server
Navigate to your tomcat folder and open the subfolder called *bin*. You will find two *.bat* files for your use:
- *startup.bat* to start your server.
- *shutdown.bat* to stop your server.

If you choose to work with the binary distributaion of tomcat like I did, and not as a windows service. The web server won't start itself when you computer boot up, you'll have to run the *startup.bat* file manually or create some kind of schedule for it. I just prefered creating a shortcut on my desktop and double clicking it whenever I want the server running, I actually never shutdown my pc, so the server is always on anyways.

### Testing our skill
Well, actually if you got this far, your skill is already working, so you can just say *Alexa, start my computer* to invoke the skill or you can go back to amazon devloper portal like we did [here](#setting-up-a-skill-interface-with-alexa), go into the *Test* tab and scroll down to the *Service Simulator*, type *start computer* (or whatever invocation phrase you chosed) and you can see the reply from your web server.</br>
Try saying: *Alexa, ask computer to open facebook* or type *ask computer to open facebook* in the testing tab, the result will be facebook opening up on the default browser on your computer.</br>
Before leaving this guide, please take a look in [*Examples section*](#example-settings-of-the-skill) to understand how to configure your skill correctly and in the [*Logs section*](#log-files) in order to understand how to read the log files if you'll ever need to.

### Example settings of the skill
#### Example 1: Alexa ask computer to start excel
For that purpose, we will:<br/>
1- add **excel** to the *LIST_OF_ACTIONS* custom slot type and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open excel.

2- add the line **"excel": "C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE"** to our *conf_files/action_map.json* file.<br/>
Which means that for every time the skill receives the action named excel, the skill will then open C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE.

#### Example 2: Alexa ask computer to open facebook
For that purpose, we will:<br/>
1- add **facebook** to the *LIST_OF_ACTIONS* custom slot ype and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open facebook.

2- add the line **"facebook": "https://www.facebook.com/"** to our *conf_files/action_map.json* file.<br/>
Which means that for every time the skill receives the action named facebook, the skill will then open https://www.facebook.com/ in our default web browser.

### Log files
If you followed this guide correctly, there are 3 logs avaialabe for your use. In the case of something not working properly, you can check this logs and find out how to fix it. These logs are at the server side, which means they only work as long as the request succesfully arrived from the alexa servers to your server. If something went wrong with the skill interface you will not see anything helpfull in these log files, in this case you will have to check your alexa app for any cards or try testing it with Test tab in the alexa developer portal to see what went wrong.</br>

Navigate to your tomcat folder and open the subfolder calls *logs*, in this folder you will see all of the tomcat log files, but for our skill there 3 logs in particualr:
- The first log file name is the name of your dns name prefixed with *_access_log.yyyy-mm-dd.txt*.</br>
This is a daily rolled log, which means that there is a log for each day the skill was invoked.</br>
In this log file you can see all the requests to you web server and response codes from you web server.

- The second log file name is *AskMyPc.yyyy-mm-dd.log*.</br>
This is a daily rolled log, which means that there is a log for each day the skill was invoked.</br>
In this log you can see details of every interaction with the skill, you will see rather or not the request passed the validation part and what exactrly happend thrugh the interaction from start to end. Each raw will contain a session id which is unique per each session created, so you will be able to differ one interation from another.

- The third log file name is *AskMyPc.error.log* and it will contain the stack trace for every error occurring in the skill.</br>
This is the only log not daily rolled.

You can delete all 3 logs if you need to, the skill will simply create new ones on its next invocation or start-up.
