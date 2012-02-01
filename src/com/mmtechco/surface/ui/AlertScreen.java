package com.mmtechco.surface.ui;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.MMTools;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.ui.component.BitmapButtonField;
import com.mmtechco.surface.ui.component.PillButtonField;
import com.mmtechco.surface.ui.container.PillButtonSet;
import com.mmtechco.surface.util.Constants;
import com.mmtechco.surface.util.Logger;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.surface.util.Tools;
import com.mmtechco.surface.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.media.control.AudioPathControl;
import net.rim.device.api.system.Alert;
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
	public final int surfaceInterval = 3 * 60;

	String prevStatus = "";

	Player player;
	VibrateThread viber;

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
				// Start countdown
				actionButton.startCountdown(Constants.type_surface,
						surfaceInterval);
			}
		});

		// Play sound
		play();
		viber = new VibrateThread();
		viber.start();
	}

	public void play() {
		try {
			VolumeControl volume = (VolumeControl) player
					.getControl("VolumeControl");
			volume.setLevel(100);
			// Direct audio to speaker even if headset or headphones are plugged
			// in
			AudioPathControl apc = (AudioPathControl) player
					.getControl("net.rim.device.api.media.control.AudioPathControl");
			apc.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);
			player = javax.microedition.media.Manager.createPlayer(
					AlertScreen.class.getResourceAsStream("/sounds/beep.mp3"),
					"audio/mpeg");
			player.realize();
			player.prefetch();
			player.start();
		} catch (Exception e) {
			logger.log(TAG, e.getMessage());
		}
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
					sendMessage(Constants.type_surface);
				}
			});
			// Stop playing audio
			if (player != null && player.getState() == Player.STARTED) {
				try {
					player.stop();
					player.close();
				} catch (MediaException e) {
					logger.log(TAG, e.getMessage());
				}
			}
			// Stop vibrating
			if (viber != null) {
				viber.stop();
			}
		}

		public void setAlert() {
			setButtonText("Alert");
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					startCountdown(Constants.type_alert, cooldownPeriod);
				}
			});
		}

		public void setManDown() {
			setButtonText("Man Down");
			setChangeListener(null);
			setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					startCountdown(Constants.type_mandown, cooldownPeriod);
				}
			});
		}

		private void startCountdown(final String type, int cooldownPeriod) {
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
					if (type.equals(Constants.type_surface)) {
						setSurface();
					} else if (type.equals(Constants.type_alert)) {
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
							+ LocationMonitor.latitude
							+ Tools.ServerQueryStringSeparator
							+ LocationMonitor.longitude;
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

	class VibrateThread extends Thread {
		private final int[] vibePattern = { 650, 208, 992, 225, 983, 200, 1000,
				200, 1009, 191, 1009, 208, 983, 217, 1042, 216, 1100, 200,
				1159, 200, 1208, 208, 1259, 200, 1308, 208, 1367, 208, 1409,
				208, 1258, 117, 83, 117, 92, 116, 1659, 108, 83, 117, 92, 108,
				1667, 108, 92, 116, 75, 117, 1675, 100, 92, 108, 100, 100,
				1667, 116, 84, 108, 100, 108, 1667, 108, 84, 116, 92, 108,
				1667, 108, 92, 108, 100, 92, 1667, 108, 100, 109, 75, 116,
				1675, 100, 92, 117, 91, 109, 1658, 117, 83, 117, 83, 117, 66,
				62225, 13217, 0 };
		private boolean stop;

		public void run() {
			int duration = 0;
			int sleep = 0;
			for (int i = 0; i < vibePattern.length - 1; i++) {
				if (stop) {
					break;
				}
				if (i % 2 == 0) {
					duration = vibePattern[i];
					sleep = vibePattern[i + 1];
					Alert.startVibrate(duration);
					try {
						Thread.sleep(duration + sleep);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		public void stop() {
			stop = true;
		}
	};
}