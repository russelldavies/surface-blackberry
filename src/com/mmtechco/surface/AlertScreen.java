package com.mmtechco.surface;

import java.util.Timer;
import java.util.TimerTask;

import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.MMTools;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.ui.BitmapButtonField;
import com.mmtechco.surface.ui.PillButtonField;
import com.mmtechco.surface.ui.PillButtonSet;
import com.mmtechco.surface.util.Constants;
import com.mmtechco.surface.util.Logger;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.surface.util.Tools;
import com.mmtechco.surface.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public final class AlertScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(AlertScreen.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private static Logger logger = Logger.getInstance();
	private static MMTools tools = ToolsBB.getInstance();

	// GUI widgets
	private LabelField statusLabelField = new LabelField("",
			Field.NON_FOCUSABLE | DrawStyle.HCENTER) {
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.WHITE);
			super.paint(graphics);
		}
	};

	BitmapTextButtonField actionButton;
	PillButtonSet pills;
	PillButtonField pillOne;
	PillButtonField pillTwo;
	PillButtonField pillThree;

	// In seconds
	public final int cooldownPeriod = 5;

	String prevStatus = "";

	public AlertScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		// Give reference of self to Registration so fields can be updated
		Registration.addObserver(this);

		// Set initial text for registration info fields
		statusLabelField.setText("SN: [none] | Status: "
				+ r.getString(i18n_RegRequesting));

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_HEIGHT
				| USE_ALL_WIDTH | FIELD_HCENTER);

		// Add logo
		// vfm.add(new BitmapField(Bitmap.getBitmapResource("logo.png"),
		// Field.FIELD_HCENTER));

		// Action button
		actionButton = new BitmapTextButtonField(
				Bitmap.getBitmapResource("alertbutton_normal.png"),
				Bitmap.getBitmapResource("alertbutton_focus.png"), "Surface",
				FIELD_HCENTER);
		actionButton.setSurface();
		vfm.add(actionButton);

		// Registration information
		vfm.add(new RegInfoStyleField());

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

		pillOne.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				actionButton.setSurface();
			}
		});
		pillTwo.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				actionButton.setAlert();
			}
		});
		pillThree.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				actionButton.setManDown();
			}
		});
		vfm.add(pills);

		vfm.setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
		add(vfm);
	}

	/**
	 * Update the screen label fields
	 */
	public void update() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				// Only set text if reg id has been received
				String regId = Registration.getRegID();
				String regStatus = Registration.getStatus();
				if (!regId.equals("0")) {
					statusLabelField.setText("SN: " + regId + " | Status: "
							+ regStatus);
				} else {
					statusLabelField.setText("SN: [none] | Status: "
							+ regStatus);
				}
			}
		});
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
				// TODO: Makes sound and vibrates
				// TODO: countdown timer of 3 minutes, show progress bar
				// text goes to sending, and then sent
			}
		});

	}

	public void close() {
		super.close();
		// App is pushed to background rather than terminated when screen is
		// closed.
		// TODO: enable this debugging is complete
		// UiApplication.getUiApplication().requestBackground();
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

	class BitmapTextButtonField extends BitmapButtonField {
		private String text;

		public BitmapTextButtonField(Bitmap normalState, Bitmap focusState,
				String text, long style) {
			super(normalState, focusState, style);
			this.text = text;
		}

		protected void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.WHITE);
			g.drawText(text, getWidth() / 2 - 20, getHeight() / 2);
		}

		protected void setButtonText(String text) {
			this.text = text;
			invalidate();
		}

		public void setSurface() {
			setButtonText("Surface");
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					sendSurface();
				}
			});
		}

		public void setAlert() {
			setButtonText("Alert");
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					sendAlert();
				}
			});
		}

		public void setManDown() {
			setButtonText("Man Down");
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					sendManDown();
				}
			});
		}

		private void sendSurface() {
			sendMessage(Constants.type_surface);
		}

		private void sendAlert() {
			startCountdown(Constants.type_alert);
		}

		private void sendManDown() {
			startCountdown(Constants.type_mandown);
		}

		private void startCountdown(final String type) {
			setButtonText("Cancel");
			prevStatus = statusLabelField.getText();
			// Start countdown
			final Timer countdown = new Timer();
			countdown.scheduleAtFixedRate(new CountdownTask(cooldownPeriod,
					type), 0, 1000);
			// Set Action button to a cancel button
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					// Cancel countdown, restore button to original state,
					// and restore status text
					countdown.cancel();
					if (type.equals(Constants.type_alert)) {
						setAlert();
					} else if (type.equals(Constants.type_mandown)) {
						setManDown();
					}
					statusLabelField.setText(prevStatus);
				}
			});
		}

		private void sendMessage(final String type) {
			String statusMsg = "Sending ";
			if (type.equals(Constants.type_surface)) {
				statusMsg = statusMsg + "Surface";
				actionButton.setSurface();
			} else if (type.equals(Constants.type_alert)) {
				statusMsg = statusMsg + "Alert";
				actionButton.setAlert();
			} else if (type.equals(Constants.type_mandown)) {
				statusMsg = statusMsg + "Man Down";
				actionButton.setManDown();
			}
			statusMsg = statusMsg + "...";
			// Save existing status before changing
			prevStatus = statusLabelField.getText();
			statusLabelField.setText(statusMsg);

			// Spawn new thread so the event lock is not blocked
			(new Thread() {
				public void run() {
					String queryString = Registration.getRegID()
							+ Tools.ServerQueryStringSeparator + type
							+ Tools.ServerQueryStringSeparator
							+ tools.getDate()
							+ Tools.ServerQueryStringSeparator
							+ LocationMonitor.longitude
							+ Tools.ServerQueryStringSeparator
							+ LocationMonitor.latitude;
					logger.log(TAG, queryString);
					new Server().contactServer(queryString);
				}
			}).start();
			statusLabelField.setText("Message Sent...");
			statusLabelField.setText(prevStatus);
		}
	}

	/**
	 * Pass in a number and it counts down to zero updating the status text
	 */
	private class CountdownTask extends TimerTask {
		int count;
		String type;

		public CountdownTask(int period, final String type) {
			count = period;
			this.type = type;
		}

		public void run() {
			if (count == 0) {
				this.cancel();
				return;
			}
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					statusLabelField.setText("Time remaining to cancel: "
							+ String.valueOf(--count));
					if (count == 0) {
						statusLabelField.setText(prevStatus);
						actionButton.sendMessage(type);
					}
				}
			});
		}
	}
}
