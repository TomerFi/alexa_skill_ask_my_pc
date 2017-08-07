package askmypc;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

public class AskMyPcSpeechlet implements Speechlet{

	private static final String APPLICATIONID = "<YOUR_APPLICATION_ID_GOES_HERE>";//amzn1.ask.skill.X
	private static final String USERID = "<YOUR_ACCOUNT_ID_GOES_HERE>";//amzn1.ask.account.X
	
    private static final Logger STDLOGGER = LoggerFactory.getLogger(AskMyPcSpeechlet.class);
	private static final Logger ERRLOGGER = LoggerFactory.getLogger("error");;
	
	public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {

		
		STDLOGGER.info(session.getSessionId()+" session started request accepted");
		String appid = session.getApplication().getApplicationId();
		String usrid = session.getUser().getUserId();
		
		if (!APPLICATIONID.equals(appid) || !USERID.equals(usrid)) {
			ERRLOGGER.error("failed authentication"+System.lineSeparator()+"appid="+appid+System.lineSeparator()+"userid="+usrid);
			throw new SpeechletException("failed authentication");
		}
		STDLOGGER.info(session.getSessionId()+" successfuly authenticated user and application");
	
	}
	
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		STDLOGGER.info(session.getSessionId()+" launch request accepted");
		
		return getWelcomeResponse();
	}	
	
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		STDLOGGER.info(session.getSessionId()+" got "+intentName+" intent request");
		
		if ("StartAction".equals(intentName)) {
			String action = intent.getSlot("Action").getValue();
	    	if (action == null) {
	    		STDLOGGER.info(session.getSessionId()+" action name is of null value");
	    		return getRepeatResponse();
	    	}
			STDLOGGER.info(session.getSessionId()+" starting "+action);
			return startAction(action.toLowerCase(), session);
		} else if ("AMAZON.NoIntent".equals(intentName)) {
			STDLOGGER.info(session.getSessionId()+" ending session");
			return getEndResponse();
		} else if ("AMAZON.YesIntent".equals(intentName)) {
			STDLOGGER.info(session.getSessionId()+" following up");
			return getFollowUpResponse();
		} else if ("AMAZON.HelpIntent".equals(intentName)) {
			STDLOGGER.info(session.getSessionId()+" help initiated");
			return getHelpResponse();
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			STDLOGGER.info(session.getSessionId()+" ending session");
			return getEndResponse();
		} else if ("AMAZON.CancelIntent".equals(intentName)) {
			STDLOGGER.info(session.getSessionId()+" ending session");
			return getEndResponse();
		} else {
			STDLOGGER.error(session.getSessionId()+"invalid intent");
			ERRLOGGER.error(session.getSessionId()+"invalid intent: "+intent.toString());
    		return getFailResponse();
		}
		
	}

	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		STDLOGGER.info(session.getSessionId()+" session ended request accepted");
		
	}
	
    private SpeechletResponse getWelcomeResponse() {
        SsmlOutputSpeech prompt = new SsmlOutputSpeech();
        prompt.setSsml("<speak>Haiboss, What would you like me to do?</speak>");
        
        SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
        repromptSpeech.setSsml("<speak>Please tell me, how can I help you?</speak>");
        
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(prompt, reprompt);
    }
    
    private SpeechletResponse getSuccessResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>Sure thingboss. Anything else?</speak>");
    	
    	SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
    	repromptSpeech.setSsml("<speak>Anything else I can do for you-chief?</speak>");
    	
    	Reprompt reprompt = new Reprompt();
    	reprompt.setOutputSpeech(repromptSpeech);
    	
    	return SpeechletResponse.newAskResponse(prompt, reprompt);
    }
    
    private SpeechletResponse getFollowUpResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>What is it you would like me to do?</speak>");
    	
    	SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
    	repromptSpeech.setSsml("<speak>Tell me what you want me to do, if you don't know what I can do for you, just ask me for help and I'll tell you.</speak>");
    	
    	Reprompt reprompt = new Reprompt();
    	reprompt.setOutputSpeech(repromptSpeech);
    	
    	return SpeechletResponse.newAskResponse(prompt, reprompt);
    }
    
    private SpeechletResponse getHelpResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>I can open apps, and do all kinds of crazy stuff on your computer. Just tell what I can do for you.</speak>");
    	
    	SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
    	repromptSpeech.setSsml("<speak>Just tell me what you wanna do, and I'll try doing it for you. If I'll fail, I'll write it down in my logs. I promise.</speak>");
    	
    	Reprompt reprompt = new Reprompt();
    	reprompt.setOutputSpeech(repromptSpeech);
    	
    	return SpeechletResponse.newAskResponse(prompt, reprompt);
    }
    
    private SpeechletResponse getRepeatResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>hmm... I can't seem to find the correct action. Can you run it by me again?</speak>");
    	
    	SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
    	repromptSpeech.setSsml("<speak>Please repeat the action name.</speak>");
    	
    	Reprompt reprompt = new Reprompt();
    	reprompt.setOutputSpeech(repromptSpeech);
    	
    	return SpeechletResponse.newAskResponse(prompt, reprompt);
    }
    
    private SpeechletResponse getFailResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>Sorryboss. I've encountered an exception. Please check my logs and try again.</speak>");
    	
    	return SpeechletResponse.newTellResponse(prompt);
    }
    
    private SpeechletResponse getEndResponse() {
    	SsmlOutputSpeech prompt = new SsmlOutputSpeech();
    	prompt.setSsml("<speak>OK. I'm here if you need me.</speak>");
    	
    	return SpeechletResponse.newTellResponse(prompt);
    }
    
    private SpeechletResponse startAction (String _actionName, final Session session){
    	try {
    		File directory = (new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getParentFile();
     		File jsonFile = new File(directory,"conf_files/action_map.json");
    		
    		String jsonText = IOUtils.toString(new FileInputStream(jsonFile));
    		JSONObject jsonObj = new JSONObject(jsonText);
    		
    		String actionValue = null;
    		
    		try {
    			actionValue = (String) jsonObj.get(_actionName);
    		} catch (JSONException ex) {
    			actionValue = null;
    		}    		
    		
    		if (actionValue == null) {
    			STDLOGGER.info(session.getSessionId()+" action for "+_actionName+" not found");
    			return getRepeatResponse();
    		}
    		
			try {
				Desktop.getDesktop().open(new File(actionValue));
			} catch (IllegalArgumentException ex) {
				Desktop.getDesktop().browse(new URI(actionValue));
			}
	    	
			return getSuccessResponse();
    		
    	} catch (Exception ex) {
    		ERRLOGGER.error(ex.getMessage(),ex);
    		return getFailResponse();
    	}
    }

}
