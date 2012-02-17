package com.mmtechco.surface.ui;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * Popupscreen that displays a message and then closes after a specified time.
 */
public class ToastPopupScreen extends PopupScreen {
	LabelField labelField;
	Timer timer;

	public ToastPopupScreen(String message) {
		super(new VerticalFieldManager());
		
		labelField = new LabelField(message);
		add(labelField);
	}
	
	public void setText(String text) {
		labelField.setText(text);
		invalidate();
	}
	
	/**
	 * Close the screen after specified duration
	 * @param duration milliseconds before screen closes
	 */
	public void dismiss(int duration) {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						close();
					}
				});
			}
		}, duration);
	}

	/**
	 * Overrides the default implementation. Closes the popup screen when the
	 * Escape key is pressed.
	 * 
	 * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
	 */
	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			if(timer != null) {
				timer.cancel();
				close();
			}
			return true;
		}
		return super.keyChar(c, status, time);
	}
}
