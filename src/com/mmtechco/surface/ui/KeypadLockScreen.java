package com.mmtechco.surface.ui;

import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.ui.component.LockButtonField;
import com.mmtechco.surface.ui.container.EvenlySpacedHorizontalFieldManager;
import com.mmtechco.surface.ui.container.EvenlySpacedVerticalFieldManager;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;

public class KeypadLockScreen extends LockScreen {
	LockButtonField mandownButton;
	LockButtonField unlockButton;
	LockButtonField alertButton;
	
	public KeypadLockScreen() {
		super(new EvenlySpacedVerticalFieldManager(USE_ALL_HEIGHT));

		// Three buttons
		EvenlySpacedHorizontalFieldManager buttons = new EvenlySpacedHorizontalFieldManager(
				USE_ALL_WIDTH);
		int spacing = 7;
		int buttonSize = (int) ((float) Display.getWidth() / 3) - (spacing * 3);
		buttons.add(mandownButton = new LockButtonField(ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("lockscreen_mandown.png"), buttonSize,
				buttonSize, Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				"Man Down"));
		buttons.add(unlockButton = new LockButtonField(ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("lockscreen_unlock.png"), buttonSize,
				buttonSize, Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				"Unlock"));
		buttons.add(alertButton = new LockButtonField(ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("lockscreen_alert.png"), buttonSize,
				buttonSize, Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				"Alert"));

		manager.add(buttons);
		mandownButton.setChangeListener(this);
		unlockButton.setChangeListener(this);
		alertButton.setChangeListener(this);
	}

	public void fieldChanged(Field field, int context) {
		if (field == unlockButton) {
			close();
		} else {
			String message = "Sending...";
			if (field == mandownButton) {
				Messager.sendMessage(Messager.type_mandown, message);
			} else if (field == alertButton) {
				Messager.sendMessage(Messager.type_alert, message);
			}
		}
	}
}