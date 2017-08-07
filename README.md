# alexa_skill_ask_my_pc
Alexa java skill for opening files and shortcuts on a local computer running the skill with tomcat.

## Technical Concept
This is just a concept section, I will go thru all the steps as clearly as I can in the steps section.

The skill runs as a Java Servlet on a local computer running a Tomcat web server,<br/>
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
Then I would recommend using a dynamic dns service in the sorts of NO-IP and use the updates app to update your modem address in the NO-IP servers.

The skill's invocation word is Computer, but you can use whatever invocation word or phrase you want,<br/>
Just remember that it's supposed to intuitive and that alexa needs to understand you.

After getting thru the communication part, the skill itself is pretty simple,<br/>
It has only one major intent called StartAction and that intent has only one custom type slot called Action.<br/>
The custom type for the slot is called *LIST_OF_ACTIONS* and it is actually a list for all the actions you're going to ask alexa to perform.

The skill grabs the Action slot value and in a Key-Value fashion retrieves the necessary action to perform from a designated json file called *action_map.json*.<br/>
The skill then tries to open the value retrieved as a File, and if it fails it tries to open is as a Uri,<br/>
Which means the value retrieved can by either a file or an http address to open in your default browser.<br/>
Think of the json file like mappings file, you match an "what-to-do" to any "action" you're going to ask alexa to perform.

**Example 1: Alexa, ask computer to start excel.<br/>**
For that purpose, we will:<br/>
1- add **excel** to the *LIST_OF_ACTIONS* custom slot type and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open excel.

2- add the line **"excel": "C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE"** to our *action_map.json* file.<br/>
Which means that for every time the skill receives the action named excel, the skill will then open C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE.

**Example 2: Alexa, ask computer to open facebook.<br/>**
For that purpose, we will:<br/>
1- add **facebook** to the *LIST_OF_ACTIONS* custom slot ype and save our skill interface.<br/>
Which means we can now ask alexa to tell our skill to open facebook.

2- add the line **"facebook": "https://www.facebook.com/"** to our *action_map.json* file.<br/>
Which means that for every time the skill receives the action named facebook, the skill will then open https://www.facebook.com/ with our default web browser.
