package com.mmtechco.surface.ui;

import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.ui.component.LockButtonField;
import com.mmtechco.surface.ui.container.EvenlySpacedHorizontalFieldManager;
import com.mmtechco.surface.ui.container.EvenlySpacedVerticalFieldManager;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.media.MediaActionHandler;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class KeypadLockScreen extends FullScreen implements FieldChangeListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(KeypadLockScreen.class);
	private static Logger logger = Logger.getInstance();

	LockButtonField mandownButton;
	LockButtonField unlockButton;
	LockButtonField alertButton;

	public KeypadLockScreen() {
		EvenlySpacedVerticalFieldManager dualManager = new EvenlySpacedVerticalFieldManager(
				USE_ALL_HEIGHT);

		// Logo image
		Bitmap logoBitmap = Bitmap.getBitmapResource("surface_logo.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int newWidth = (int) (Display.getWidth() * 0.9);
		int newHeight = (int) (newWidth / ratio);
		dualManager.add(new BitmapField(ToolsBB
				.resizeBitmap(logoBitmap, newWidth, newHeight,
						Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				Field.FIELD_HCENTER));

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

		dualManager.add(buttons);
		mandownButton.setChangeListener(this);
		unlockButton.setChangeListener(this);
		alertButton.setChangeListener(this);

		add(dualManager);
		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
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

	protected boolean keyDown(int keycode, int time) {
		// Prevent user from locking the device and instead turn off backlight
		int key = Keypad.key(keycode);
		if (key == Keypad.KEY_LOCK
				|| key == MediaActionHandler.MEDIA_ACTION_PLAYPAUSE_TOGGLE
				|| key == Keypad.KEY_VOLUME_UP) {
			Backlight.enable(false);
		}
		return false;
	}
}
