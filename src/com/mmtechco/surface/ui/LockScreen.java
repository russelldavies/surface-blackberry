package com.mmtechco.surface.ui;

import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

public abstract class LockScreen extends FullScreen implements
		FieldChangeListener {
	// This is the lock key on Curve devices. It is also the media play/mute
	// button
	static final int KEY_LOCK_ALT = 17891328;
	Manager manager;

	public LockScreen(Manager manager) {
		this.manager = manager;

		// Title bar
		StandardTitleBar titleBar = new StandardTitleBar().addClock()
				.addNotifications().addSignalIndicator();
		titleBar.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
				StandardTitleBar.BATTERY_VISIBLE_ALWAYS);
		setTitleBar(titleBar);

		// Logo image
		Bitmap logoBitmap = Bitmap.getBitmapResource("surface_logo.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int newWidth = (int) (Display.getWidth() * 0.9);
		int newHeight = (int) (newWidth / ratio);
		manager.add(new BitmapField(ToolsBB.resizeBitmap(logoBitmap, newWidth,
				newHeight, Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				Field.FIELD_HCENTER));

		add(manager);

		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
	}

	protected boolean keyDown(int keycode, int time) {
		// Prevent user from locking the device and instead turn off backlight
		switch (Keypad.key(keycode)) {
		case Keypad.KEY_LOCK:
		case KEY_LOCK_ALT:
		case Keypad.KEY_VOLUME_UP:
			Backlight.enable(false);
			return true;
		default:
			return false;
		}
	}

	protected boolean keyRepeat(int keycode, int time) {
		return keyDown(keycode, time);
	}
}
