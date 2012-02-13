//#preprocess
package com.mmtechco.surface.ui;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.ui.component.ActionButtonField;
import com.mmtechco.surface.ui.component.PillButtonField;
import com.mmtechco.surface.ui.container.PillButtonSet;
import com.mmtechco.surface.util.SurfaceResource;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
//#ifdef TOUCH
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.BitmapField;
//#endif
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public final class AlertScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private LabelField statusLabelField = new LabelField(
			"SN: [none] | Status: " + r.getString(i18n_RegRequesting),
			Field.NON_FOCUSABLE | DrawStyle.HCENTER) {
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.WHITE);
			super.paint(graphics);
		}
	};
	ActionButtonField actionButton;
	PillButtonSet pills;
	PillButtonField pillOne;
	PillButtonField pillTwo;
	PillButtonField pillThree;

	public AlertScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		// Give reference of self to Registration so registration status can be
		// updated
		Registration.addObserver(this);

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_HEIGHT
				| USE_ALL_WIDTH | FIELD_HCENTER);

		// Logo - only add on touch-only devices
		//#ifdef TOUCH
		vfm.add(new BitmapField(Bitmap.getBitmapResource("surface_logo.png"),
				Field.FIELD_HCENTER));
		//#endif

		// Action button
		actionButton = new ActionButtonField(this, Field.FIELD_HCENTER);

		// Context Buttons
		pills = new PillButtonSet();
		pillOne = new PillButtonField("Surface");
		pillTwo = new PillButtonField("Alert");
		pillThree = new PillButtonField("Man Down");
		pills.add(pillOne);
		pills.add(pillTwo);
		pills.add(pillThree);
		pills.setMargin(15, 15, 5, 15);
		pills.setSelectedField(pillOne);
		FieldChangeListener pillListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (field == pillOne) {
					actionButton.setSurface();
				} else if (field == pillTwo) {
					actionButton.setAlert();
				} else if (field == pillThree) {
					actionButton.setManDown();
				}
			}
		};
		pillOne.setChangeListener(pillListener);
		pillTwo.setChangeListener(pillListener);
		pillThree.setChangeListener(pillListener);

		// Add elements to field manager
		vfm.add(actionButton);
		vfm.add(new RegInfoStyleField());
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

	/**
	 * Show screen and request user to surface
	 */
	public void surface() {
		// Bring screen to front
		UiApplication.getUiApplication().requestForeground();

		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				// Select Surface pill
				pills.setSelectedField(pillOne);
				// Start countdown
				actionButton.surface();
			}
		});
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

	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}

	/**
	 * Creates a rounded rectangle to hold registration info fields
	 */
	private class RegInfoStyleField extends VerticalFieldManager {
		RegInfoStyleField() {
			super(Manager.FIELD_HCENTER);
			add(statusLabelField);
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
}