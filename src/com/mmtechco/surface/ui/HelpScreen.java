package com.mmtechco.surface.ui;

import com.mmtechco.surface.ui.container.FieldSet;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class HelpScreen extends MainScreen {

	public HelpScreen() {
		setTitle("Surface Help");

		Border titleBorder = BorderFactory.createBitmapBorder(new XYEdges(8, 9,
				3, 9), Bitmap.getBitmapResource("fieldset_title_border.png"));
		Border contentBorder = BorderFactory
				.createBitmapBorder(new XYEdges(6, 9, 10, 9),
						Bitmap.getBitmapResource("fieldset_body_border.png"));

		FieldSet set = new FieldSet("Main Screen", titleBorder, contentBorder,
				USE_ALL_WIDTH);
		set.add(new LabelField(
			"Launch the app by its icon to show the main screen.\n\n"
			+ "In this screen you can send a Surface, Man Down, or Alert.\n\n" +
			" To select which notification to send, select the type from the " +
			"bottommost icons. Then click the centre or Action button to send " +
			"the notification."));
		set.setMargin(10, 10, 10, 10);
		add(set);
		
		set = new FieldSet("Lock Screen", titleBorder, contentBorder,
				USE_ALL_WIDTH);
		set.add(new LabelField(
			"The Lock Screen can be enabled from the Settings screen.\n\n" +
			"It is activated when the device backlight turns off or can be " +
			"activated on demand by double tapping the Volume Up button.\n\n" +
			"Like the Main Screen is can be used to send a notification."));
		set.setMargin(10, 10, 10, 10);
		add(set);
		
		set = new FieldSet("Button Activated Alert", titleBorder, contentBorder,
				USE_ALL_WIDTH);
		set.add(new LabelField(
			"Should you enable it from the Settings Screen, you can send an " +
			"Alert by double tapping the Volume Down key.\n\n" +
			"Note that if you are not using the Surface Lock Screen but rather " +
			"the default lockscreen, this feature will not work."));
		set.setMargin(10, 10, 10, 10);
		add(set);
	}
}
