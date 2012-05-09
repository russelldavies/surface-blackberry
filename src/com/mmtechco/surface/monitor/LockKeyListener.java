package com.mmtechco.surface.monitor;

import com.mmtechco.surface.Settings;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.message.EventMessage;
import com.mmtechco.surface.message.Messager;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Alert;
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
	
	private static final int INTERVAL = 2500;
	
	private int lastTime;
	private boolean sending, vibing, locked;
	
	private Screen lockscreen;
	
	public LockKeyListener(Screen lockscreen) {
		this.lockscreen = lockscreen;
	}
	
	public boolean keyDown(int keycode, int time) {
		lastTime = time;
		return false;
	}
	
	public boolean keyUp(int keycode, int time) {
		Alert.stopVibrate();
		locked = false;
		vibing = false;
		sending = false;
		
		return false;
	}
	
	public boolean keyRepeat(int keycode, int time) {
		// Lock screen
		if (Settings.shieldOn && Keypad.key(keycode) == Keypad.KEY_VOLUME_UP) {
			logger.log(TAG, "Volume up caught.");
			if(!locked && time - lastTime > INTERVAL) {
				locked = true;
				// Only push lockscreen if is is not already shown
				if (!lockscreen.isDisplayed()) {
					logger.log(TAG, "Pushing lockscreen");
					Ui.getUiEngine().pushGlobalScreen(lockscreen,
							Surface.SCREEN_PRIORITY_LOCKSCREEN,
							UiEngine.GLOBAL_SHOW_LOWER);
				}
			}
			return true;
		}
		
		// Button alert
		if (Settings.alertOn && Keypad.key(keycode) == Keypad.KEY_VOLUME_DOWN) {
			logger.log(TAG, "Volume down caught.");
			// Start vibing
			if (!vibing && time - lastTime > INTERVAL) {
				vibing = true;
				Alert.startVibrate(2500);
				lastTime = time;
				return true;
			}
			// Send after continues holding through vibrate
			if (!sending && time - lastTime > INTERVAL) {
				sending = true;
				logger.log(TAG, "Sending Alert");
				Messager.sendMessage(new EventMessage(EventMessage.STATE_ALH), "Sending Alert...");
				Alert.startVibrate(100);
				return true;
			}
		}
		return false;
	}

	public boolean keyStatus(int keycode, int time) {
		return false;
	}
	
	public boolean keyChar(char key, int status, int time) {
		return false;
	}
}