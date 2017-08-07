package askmypc;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;

public class AskMyPcServlet extends SpeechletServlet {

	private static final long serialVersionUID = 1L;
	
	public AskMyPcServlet() {
		this.setSpeechlet(new AskMyPcSpeechlet());
	}
}
