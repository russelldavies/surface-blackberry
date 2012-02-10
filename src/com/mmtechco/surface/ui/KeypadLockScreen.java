package com.mmtechco.surface.ui;

import com.mmtechco.surface.ui.component.LockButtonField;
import com.mmtechco.surface.ui.container.EvenlySpacedHorizontalFieldManager;
import com.mmtechco.surface.ui.container.EvenlySpacedVerticalFieldManager;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class KeypadLockScreen extends FullScreen {
	private static final String TAG = ToolsBB
			.getSimpleClassName(KeypadLockScreen.class);

	private static Logger logger = Logger.getInstance();

	public KeypadLockScreen() {
		EvenlySpacedVerticalFieldManager dualManager = new EvenlySpacedVerticalFieldManager(
				USE_ALL_HEIGHT);

		EncodedImage logoImage = EncodedImage
				.getEncodedImageResource("surface_logo.png");
		float ratio = (float) logoImage.getWidth()
				/ (float) logoImage.getHeight();
		int width = (int) ((float) Display.getWidth() * 0.9);
		int height = (int) ((float) width / ratio);
		logoImage = ToolsBB.resizeImage(logoImage, width, height);
		BitmapField logoField = new BitmapField(logoImage.getBitmap(),
				Field.FIELD_HCENTER);

		EvenlySpacedHorizontalFieldManager buttons = new EvenlySpacedHorizontalFieldManager(
				USE_ALL_WIDTH);
		int spacing = 7;
		int buttonSize = (int) ((float) width / 3) - (spacing * 3);
		buttons.add(new LockButtonField(ToolsBB.resizeImage(
				EncodedImage.getEncodedImageResource("lockscreen_mandown.png"),
				buttonSize, buttonSize), "Man Down"));
		buttons.add(new LockButtonField(ToolsBB.resizeImage(
				EncodedImage.getEncodedImageResource("lockscreen_unlock.png"),
				buttonSize, buttonSize), "Unlock"));
		buttons.add(new LockButtonField(ToolsBB.resizeImage(
				EncodedImage.getEncodedImageResource("lockscreen_alert.png"),
				buttonSize, buttonSize), "Alert"));

		dualManager.add(logoField);
		dualManager.add(buttons);
		add(dualManager);
		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
	}

	protected boolean keyDown(int keycode, int time) {
		// Prevent user from locking the device
		if (Keypad.key(keycode) == Keypad.KEY_LOCK) {
			Backlight.enable(false);
			return true;
		}
		return super.keyDown(keycode, time);
	}
}
