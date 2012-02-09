package com.mmtechco.surface.ui;

import com.mmtechco.surface.ui.component.LockButtonField;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class KeypadLockScreen extends FullScreen {
	private static final String TAG = ToolsBB
			.getSimpleClassName(KeypadLockScreen.class);

	private static Logger logger = Logger.getInstance();

	private LabelField surfaceLabel;

	public KeypadLockScreen() {
		super(Manager.NO_VERTICAL_SCROLL | Manager.USE_ALL_HEIGHT
				| Manager.USE_ALL_WIDTH);

		surfaceLabel = new LabelField("Surface", Field.NON_FOCUSABLE
				| DrawStyle.HCENTER) {
			protected void paint(Graphics g) {
				int oldColor = g.getColor();
				try {
					g.setColor(Color.WHITE);
					super.paint(g);
				} finally {
					g.setColor(oldColor);
				}
			}
		};
		
		if (FontManager.getInstance().load("kabel.ttf", "Kabel",
				FontManager.APPLICATION_FONT) == FontManager.SUCCESS) {
			try {
				FontFamily typeface = FontFamily.forName("Kabel");
				Font kabelFont = typeface.getFont(Font.PLAIN, 100);
				surfaceLabel.setFont(kabelFont);
			} catch (ClassNotFoundException e) {
				logger.log(TAG, e.getMessage());
			}
		}
		add(surfaceLabel);

		HorizontalFieldManager hfm = new HorizontalFieldManager();
		hfm.add(new LockButtonField("Man Down", Bitmap
				.getBitmapResource("lockscreen_mandown.png")));
		hfm.add(new LockButtonField("Unlock", Bitmap
				.getBitmapResource("lockscreen_unlock.png")));
		hfm.add(new LockButtonField("Alert", Bitmap
				.getBitmapResource("lockscreen_alert.png")));
		add(hfm);
		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
	}

	protected boolean keyDown(int keycode, int time) {
		if (Keypad.key(keycode) == Keypad.KEY_LOCK) {
			Backlight.enable(false);
			return true;
		}
		return super.keyDown(keycode, time);
	}
}
