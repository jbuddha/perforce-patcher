package org.buddha.perforce.patch;

import static org.buddha.perforce.patch.LineAction.DEFAULT;

/**
 *
 * @author buddha
 */
public class LineDiff {
	LineAction action;
	String text;

	public LineDiff(LineAction action, String text) {
		this.action = action;
		this.text = text;
	}

	public LineDiff(String text) {
		this.action = DEFAULT;
		this.text = text;
	}

	public LineAction getAction() {
		return action;
	}

	public void setAction(LineAction action) {
		this.action = action;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return action + text; 
	}
}
