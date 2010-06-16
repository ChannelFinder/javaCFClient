/**
 * 
 */
package gov.bnl.channelfinder.channelfinderAPI;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

/**
 * @author shroffk
 * Parses the HTML pay load containing the details about the error
 * TODO
 * improve the parsing
 */
public class ClientResponseParser extends ParserCallback {

	private String message;
	private int i;
	private boolean enable;

	public ClientResponseParser() {
		super();
		this.message = "";
		this.i = 0;
		this.enable = false;
	}
	
	public void handleText(char[] data, int pos) {
		switch (i) {
		case 3:
			if (enable)
				message += new String(data);
			else
				enable = true;
			break;
		default:
			break;
		}
	}

	public void handleStartTag(Tag t, MutableAttributeSet a, int p) {
		if (t == HTML.Tag.B)
			i++;
		else if (t == HTML.Tag.H3)
			enable = false;
	}

	public String getMessage() {
		return this.message;
	}
}
