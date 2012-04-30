package com.mmtechco.surface.monitor;

import com.mmtechco.surface.Settings;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.message.EventMessage;
import com.mmtechco.surface.message.Messager;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;

public final class LockKeyListener implements KeyListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(LockKeyListener.class);
	private static Logger logger = Logger.getInstance();
	
	private int lastTime;
	private Screen lockscreen;
	
	public LockKeyListener(Screen lockscreen) {
		this.lockscreen = lockscreen;
	}
	
	public boolean keyDown(int keycode, int time) {
		if (Settings.shieldOn && Keypad.key(keycode) == Keypad.KEY_VOLUME_UP) {
			logger.log(TAG, "Volume up caught.");
			// Continue if second press is within a second
			if (time - lastTime < 1000) {
				logger.log(TAG, "active screen: " + UiApplication.getUiApplication().getActiveScreen());
				logger.log(TAG, "lock screen:" + lockscreen);
				// Only push lockscreen if is is not already shown
				if (!lockscreen.isDisplayed()) {
					logger.log(TAG, "Pushing lockscreen");
					Ui.getUiEngine().pushGlobalScreen(lockscreen,
							Surface.SCREEN_PRIORITY_LOCKSCREEN,
							UiEngine.GLOBAL_SHOW_LOWER);
				}
			}
			lastTime = time;
			// Consume event
			return true;
		}
		else if (Settings.alertOn && Keypad.key(keycode) == Keypad.KEY_VOLUME_DOWN) {
			logger.log(TAG, "Volume down caught.");
			// Continue if second press is within a second
			if (time - lastTime < 1000) {
				logger.log(TAG, "Sending Alert");
				Messager.sendMessage(new EventMessage(EventMessage.STATE_ALH), "Sending Alert...");
			}
			lastTime = time;
			// Consume event
			return true;
		}
		return false;
	}
	
	public boolean keyUp(int keycode, int time) {
		return false;
	}
	
	public boolean keyChar(char key, int status, int time) {
		return false;
	}

	public boolean keyRepeat(int keycode, int time) {
		return false;
	}

	public boolean keyStatus(int keycode, int time) {
		return false;
	}
}