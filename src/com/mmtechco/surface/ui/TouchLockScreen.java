package com.mmtechco.surface.ui;

import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.ui.component.LockSliderField;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class TouchLockScreen extends LockScreen {
	LockSliderField mandownSlider;
	LockSliderField unlockSlider;
	LockSliderField alertSlider;
	
	public TouchLockScreen() {
		//super(new EvenlySpacedVerticalFieldManager(USE_ALL_HEIGHT));
		super(new VerticalFieldManager(VerticalFieldManager.FIELD_HCENTER));
		
		// Mandown Slider
		manager.add(mandownSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_mandown.png"), Bitmap
				.getBitmapResource("lockscreen_mandown.png"), 10, 0));
		
		// Alert Slider
		manager.add(alertSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_alert.png"), Bitmap
				.getBitmapResource("lockscreen_alert.png"), 10, 10));
		
		// Unlock Slider
		manager.add(unlockSlider = new LockSliderField(Bitmap
				.getBitmapResource("lockscreen_slider_unlock.png"), Bitmap
				.getBitmapResource("lockscreen_lock.png"), 10, 0));
		
		mandownSlider.setChangeListener(this);
		unlockSlider.setChangeListener(this);
		alertSlider.setChangeListener(this);
	}

	public void fieldChanged(Field field, int context) {
		if (field == unlockSlider) {
			close();
		} else {
			String message = "Sending...";
			if (field == mandownSlider) {
				mandownSlider.setState(mandownSlider.initialState);
				Messager.sendMessage(Messager.STATE_MNS, message);
			} else if (field == alertSlider) {
				alertSlider.setState(alertSlider.initialState);
				Messager.sendMessage(Messager.STATE_ALH, message);
			}
		}
	}
}
