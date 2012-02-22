//#preprocess
package com.mmtechco.surface.monitor;

import com.mmtechco.surface.Surface;
import com.mmtechco.surface.ui.KeypadLockScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiEngine;

public final class LockKeyListener implements KeyListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(LockKeyListener.class);
	private static Logger logger = Logger.getInstance();
	
	private int lastTime;

	public boolean keyChar(char key, int status, int time) {
		return false;
	}

	public boolean keyDown(int keycode, int time) {
		if (Keypad.key(keycode) == Keypad.KEY_VOLUME_UP) {
			logger.log(TAG, "Volume key caught. Last time: " + lastTime + " Current time: " + time);
			// Continue if second press is within a second
			if (time - lastTime < 1000) {
				logger.log(TAG, "Pushing lockscreen");
				//#ifdef TOUCH
				Screen lockscreen = new TouchLockScreen();
				//#else
				Screen lockscreen = new KeypadLockScreen();
				//#endif
				Ui.getUiEngine().pushGlobalScreen(lockscreen,
						Surface.SCREEN_PRIORITY_LOCKSCREEN,
						UiEngine.GLOBAL_SHOW_LOWER);
			}
			lastTime = time;
			// Consume event
			return true;
		}
		return false;
	}

	public boolean keyRepeat(int keycode, int time) {
		return false;
	}

	public boolean keyStatus(int keycode, int time) {
		return false;
	}

	public boolean keyUp(int keycode, int time) {
		return false;
	}
}