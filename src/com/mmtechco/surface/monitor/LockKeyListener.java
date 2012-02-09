package com.mmtechco.surface.monitor;

import com.mmtechco.surface.ui.KeypadLockScreen;

import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;

public final class LockKeyListener implements KeyListener {

	public boolean keyChar(char key, int status, int time) {
		return false;
	}

	public boolean keyDown(int keycode, int time) {
		if (Keypad.key(keycode) == Keypad.KEY_VOLUME_DOWN) {
			UiApplication.getUiApplication().pushScreen(new KeypadLockScreen());
			UiApplication.getUiApplication().requestForeground();
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
