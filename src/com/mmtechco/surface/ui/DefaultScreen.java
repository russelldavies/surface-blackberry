//#preprocess
package com.mmtechco.surface.ui;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.ui.component.ActionButtonField;
import com.mmtechco.surface.ui.component.PillButtonField;
import com.mmtechco.surface.ui.container.PillButtonSet;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
//#ifdef TOUCH
import net.rim.device.api.ui.component.BitmapField;
//#endif
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public final class DefaultScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	// Specified in seconds
	private final int interval = 5;

	// GUI widgets
	private LabelField statusLabelField = new LabelField(
			"SN: [none] | Status: " + r.getString(i18n_RegRequesting),
			Field.NON_FOCUSABLE | DrawStyle.HCENTER) {
		protected void paint(Graphics graphics) {
			int oldColor = graphics.getColor();
			try {
				graphics.setColor(Color.WHITE);
				super.paint(graphics);
			} finally {
				graphics.setColor(oldColor);
			}
		}
	};

	public DefaultScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		// Give reference of self to Registration so registration status can be
		// updated
		Registration.addObserver(this);

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_HEIGHT
				| USE_ALL_WIDTH | FIELD_HCENTER);

		//#ifdef TOUCH
		// Logo - only added on touch-only devices
		Bitmap logoBitmap = Bitmap.getBitmapResource("surface_logo.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int newWidth = (int) (Display.getWidth() * 0.9);
		int newHeight = (int) (newWidth / ratio);
		BitmapField logoField = new BitmapField(ToolsBB.resizeBitmap(
				logoBitmap, newWidth, newHeight, Bitmap.FILTER_LANCZOS,
				Bitmap.SCALE_TO_FIT), Field.FIELD_HCENTER);
		logoField.setPadding(0, 0, 10, 0);
		vfm.add(logoField);
		//#endif
		
		// Action button
		double factor = 0.75;
		int spinnerSize = (int) (Display.getHeight() * factor);
		int numFrames = 20;
		Bitmap spinner = ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("spinner_alert-mandown.png"),
				spinnerSize * numFrames, spinnerSize, Bitmap.FILTER_LANCZOS,
				Bitmap.SCALE_TO_FIT);
		final ActionButtonField actionButton = new ActionButtonField(this, spinner, numFrames,
				interval, Field.FIELD_HCENTER);

		// Status field
		StatusField statusField = new StatusField(statusLabelField);

		// Context Buttons
		PillButtonSet pills = new PillButtonSet();
		final PillButtonField pillOne = new PillButtonField("Surface");
		final PillButtonField pillTwo = new PillButtonField("Alert");
		final PillButtonField pillThree = new PillButtonField("Man Down");
		pills.add(pillOne);
		pills.add(pillTwo);
		pills.add(pillThree);
		pills.setMargin(15, 15, 5, 15);
		pills.setSelectedField(pillOne);
		FieldChangeListener pillListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (field == pillOne) {
					actionButton.setSurface();
					actionButton.setFocus();
				} else if (field == pillTwo) {
					actionButton.setAlert();
					actionButton.setFocus();
				} else if (field == pillThree) {
					actionButton.setManDown();
					actionButton.setFocus();
				}
			}
		};
		pillOne.setChangeListener(pillListener);
		pillTwo.setChangeListener(pillListener);
		pillThree.setChangeListener(pillListener);


		// Add elements to field manager
		vfm.add(actionButton);
		vfm.add(statusField);
		vfm.add(pills);
		
		vfm.setBackground(BackgroundFactory.createLinearGradientBackground(
		Color.BLACK, Color.BLACK, Color.RED, Color.RED));
		add(vfm);
	}

	/**
	 * Update the screen label fields showing registration status
	 */
	public void setStatus(final String status) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				statusLabelField.setText(status);
			}
		});
	}

	public String getStatus() {
		return statusLabelField.getText();
	}

	public void close() {
		// App is pushed to background rather than terminated when screen is
		// closed except in debug mode
		//#ifndef DEBUG
		UiApplication.getUiApplication().requestBackground();
		//#else
		super.close();
		//#endif
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem lockscreenMenu = new MenuItem("Lock Screen", 0x100020, 0) {
			public void run() {
				//#ifdef TOUCH
				Screen lockscreen = new TouchLockScreen();
				//#else
				Screen lockscreen = new KeypadLockScreen();
				//#endif
				Ui.getUiEngine().pushGlobalScreen(lockscreen,
						Surface.SCREEN_PRIORITY_LOCKSCREEN,
						UiEngine.GLOBAL_SHOW_LOWER);
			}
		};
		menu.add(lockscreenMenu);
		
		MenuItem settingsMenu = new MenuItem("Settings", 0x100010, 1) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(new SettingsScreen());
			}
		};
		menu.add(settingsMenu);
		
		MenuItem helpMenu = new MenuItem("Help", 0x100030, 2) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(new HelpScreen());
			}
		};
		menu.add(helpMenu);
		
		super.makeMenu(menu, instance);
	}
}

/**
 * Creates a rounded rectangle to hold registration info fields
 */
class StatusField extends VerticalFieldManager {
	StatusField(Field field) {
		super(Manager.FIELD_HCENTER);
		add(field);
		setPadding(5, 5, 5, 5);
		setMargin(10, 10, 10, 10);
	}

	protected void paintBackground(Graphics g) {
		int oldColor = g.getColor();
		try {
			g.setColor(Color.BLACK);
			g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
			// g.setColor(Color.GRAY);
			// g.drawRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
		} finally {
			g.setColor(oldColor);
		}
	}
}