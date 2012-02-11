package com.mmtechco.surface.ui;

import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.ui.component.LockSliderField;
import com.mmtechco.surface.ui.container.EvenlySpacedVerticalFieldManager;
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

public class TouchLockScreen extends FullScreen implements FieldChangeListener {
	LockSliderField mandownSlider;
	LockSliderField unlockSlider;
	LockSliderField alertSlider;

	public TouchLockScreen() {
		//EvenlySpacedVerticalFieldManager manager = new EvenlySpacedVerticalFieldManager(USE_ALL_HEIGHT);

		// Mandown Slider
		add(mandownSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_mandown.png"), Bitmap
				.getBitmapResource("lockscreen_mandown.png"), 10, 0));
		
		// Logo image
		Bitmap logoBitmap = Bitmap.getBitmapResource("surface_logo.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int newWidth = (int) (Display.getWidth() * 0.9);
		int newHeight = (int) (newWidth / ratio);
		add(new BitmapField(ToolsBB
				.resizeBitmap(logoBitmap, newWidth, newHeight,
						Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				Field.FIELD_HCENTER));

		// Alert Slider
		add(alertSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_alert.png"), Bitmap
				.getBitmapResource("lockscreen_alert.png"), 10, 10));
		
		// Unlock Slider
		add(unlockSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_unlock.png"), Bitmap
				.getBitmapResource("lockscreen_lock.png"), 10, 0));
		
		mandownSlider.setChangeListener(this);
		unlockSlider.setChangeListener(this);
		alertSlider.setChangeListener(this);
		
		//add(manager);
		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
	}

	public void fieldChanged(Field field, int context) {
		if (field == unlockSlider) {
			close();
		} else {
			String message = "Sending...";
			if (field == mandownSlider) {
				mandownSlider.setState(mandownSlider.initialState);
				Messager.sendMessage(Messager.type_mandown, message);
			} else if (field == alertSlider) {
				alertSlider.setState(alertSlider.initialState);
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
