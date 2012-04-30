package com.mmtechco.surface.ui;

import com.mmtechco.surface.Settings;
import com.mmtechco.surface.ui.component.LabeledSwitch;
import com.mmtechco.surface.ui.container.FieldSet;
import com.mmtechco.surface.ui.container.JustifiedHorizontalFieldManager;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class SettingsScreen extends MainScreen implements FieldChangeListener {
	Border titleBorder = BorderFactory.createBitmapBorder(new XYEdges(8, 9, 3,
			9), Bitmap.getBitmapResource("fieldset_title_border.png"));
	Border contentBorder = BorderFactory.createBitmapBorder(new XYEdges(6, 9,
			10, 9), Bitmap.getBitmapResource("fieldset_body_border.png"));

	Bitmap switch_left = Bitmap.getBitmapResource("switch_left.png");
	Bitmap switch_right = Bitmap.getBitmapResource("switch_right.png");
	Bitmap switch_left_focus = Bitmap
			.getBitmapResource("switch_left_focus.png");
	Bitmap switch_right_focus = Bitmap
			.getBitmapResource("switch_right_focus.png");

	LabeledSwitch genSurfaceSwitch, genSoundSwitch;
	LabeledSwitch alertEmergCallSwitch;
	LabeledSwitch shieldOnSwitch, shieldVolumeSwitch;

	public SettingsScreen() {
		setTitle("Surface Settings");
		generalSettings();
		alertSettings();
		shieldSettings();
	}

	private void generalSettings() {
		FieldSet set = generateFieldSet("General");
		genSurfaceSwitch = generateSwitch(set, Settings.genSurface, "Follow up Surface - sent when you miss a Surface");
		genSoundSwitch = generateSwitch(set, Settings.genSound, "Sound");
	}
	
	private void alertSettings() {
		FieldSet set = generateFieldSet("Alert");
		alertEmergCallSwitch = generateSwitch(set, Settings.alertCall, "Emergency Call Alert");
		// TODO: add emergency number selector
		// TODO: stationary alert spinbox
	}
	
	private void shieldSettings() {
		FieldSet set = generateFieldSet("Shield");
		shieldOnSwitch = generateSwitch(set, Settings.shieldOn, "Surface Shield");
		shieldVolumeSwitch = generateSwitch(set, Settings.alertOn, "Volume Button Alert");
	}

	private FieldSet generateFieldSet(String text) {
		FieldSet set = new FieldSet(text, titleBorder, contentBorder,
				USE_ALL_WIDTH);
		set.setMargin(10, 10, 10, 10);
		add(set);
		return set;
	}

	private LabeledSwitch generateSwitch(FieldSet set, boolean setting,
			String switchText) {
		LabeledSwitch labeledSwitch = new LabeledSwitch(switch_left,
				switch_right, switch_left_focus, switch_right_focus, "On",
				"Off", setting);
		labeledSwitch.setChangeListener(this);
		JustifiedHorizontalFieldManager switchManager = new JustifiedHorizontalFieldManager(
				new LabelField(switchText), labeledSwitch, false, USE_ALL_WIDTH);
		switchManager.setPadding(5, 5, 5, 5);
		set.add(switchManager);
		return labeledSwitch;
	}

	public void fieldChanged(Field field, int context) {
		if (field == genSurfaceSwitch) {
			Settings.updateSettings(Settings.KEY_GEN_SURFACE, Settings.genSurface = genSurfaceSwitch.getOnState());
		} else if (field == genSoundSwitch) {
			Settings.updateSettings(Settings.KEY_GEN_SOUND, Settings.genSound = genSoundSwitch.getOnState());
		} else if (field == alertEmergCallSwitch) {
			Settings.updateSettings(Settings.KEY_ALERT_CALL, Settings.alertCall = alertEmergCallSwitch.getOnState());
		} else if (field == shieldOnSwitch) {
			Settings.updateSettings(Settings.KEY_SHIELD, Settings.shieldOn = shieldOnSwitch.getOnState());
		} else if (field == shieldVolumeSwitch) {
			Settings.updateSettings(Settings.KEY_ALERT_BUTTON, Settings.alertOn = shieldVolumeSwitch.getOnState());
		}
	}
}
