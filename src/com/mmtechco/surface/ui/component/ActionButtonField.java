package com.mmtechco.surface.ui.component;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import com.mmtechco.surface.Messager;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.media.control.AudioPathControl;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.LED;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;

public class ActionButtonField extends BaseButtonField {
	private static final String TAG = ToolsBB
			.getSimpleClassName(ActionButtonField.class);
	private static Logger logger = Logger.getInstance();

	private String text;
	private Bitmap spinner;
	private Bitmap button;
	private int numFrames;
	private int frameWidth;
	private int frameHeight;

	private int currentFrame;
	private int timerID = -1;

	private Application app;
	private boolean spinning;

	// In seconds
	public final int cooldownInterval = 5;
	public final int surfaceInterval = 3 * 60;

	VibrateThread viber;
	private Player player;

	ObserverScreen screen;
	
	String prevStatus;

	public ActionButtonField(ObserverScreen screen, long style) {
		super(style);
		this.screen = screen;

		button = Bitmap.getBitmapResource("alertbutton_normal.png");
		spinner = Bitmap.getBitmapResource("wait.png");
		numFrames = 19;
		frameWidth = spinner.getWidth() / numFrames;
		frameHeight = spinner.getHeight();
		app = Application.getApplication();

		setSurface();
	}

	protected void layout(int width, int height) {
		setExtent(frameWidth, frameHeight);
	}

	protected void paint(Graphics g) {
		// Spinner
		g.drawBitmap(0, 0, frameWidth, frameHeight, spinner, frameWidth
				* currentFrame, 0);
		if (spinning) {
			currentFrame++;
			if (currentFrame >= numFrames) {
				currentFrame = 0;
			}
		}

		// Action button
		g.drawBitmap(0, 0, getWidth(), getHeight(), button, 0, 0);

		// Draw text
		int oldColor = g.getColor();
		Font oldFont = g.getFont();
		try {
			g.setColor(Color.WHITE);
			FontFamily typeface = FontFamily.forName("Kabel Dm BT");
			Font font = typeface.getFont(Font.PLAIN, 30);
			g.setFont(font);
			g.drawText(text, getWidth() / 3, getHeight() / 3);
		} catch (ClassNotFoundException e) {
			logger.log(TAG, e.getMessage());
		} finally {
			g.setColor(oldColor);
			g.setFont(oldFont);
		}
	}

	public void setButtonText(String text) {
		this.text = text;
		invalidate();
	}

	public void startSpin() {
		spinning = true;
		if (timerID == -1) {
			timerID = app.invokeLater(new Runnable() {
				public void run() {
					if (spinning) {
						invalidate();
					}
				}
			}, (cooldownInterval * 1000 / numFrames), true);
		}
	}

	public void stopSpin() {
		spinning = false;
		if (timerID != -1) {
			app.cancelInvokeLater(timerID);
			timerID = -1;
			currentFrame = 0;
		}
	}

	public void surface() {
		startCountdown(Messager.type_surface, surfaceInterval);
		// Play sound
		play();
		// Vibrate phone to sound
		viber = new VibrateThread();
		viber.start();
		// Blink LED
		LED.setConfiguration(500, 250, LED.BRIGHTNESS_100);
		LED.setState(LED.STATE_BLINKING);
	}

	public void setSurface() {
		setButtonText("Surface");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				prevStatus = screen.getStatus();
				sendMessage(Messager.type_surface);
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
		// Stop LED
		LED.setState(LED.STATE_OFF);
	}

	public void setAlert() {
		setButtonText("Alert");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				startCountdown(Messager.type_alert, cooldownInterval);
			}
		});
	}

	public void setManDown() {
		setButtonText("Man Down");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				startCountdown(Messager.type_mandown, cooldownInterval);
			}
		});
	}

	private void startCountdown(final String type, final int interval) {
		startSpin();

		setButtonText("Cancel");
		prevStatus = screen.getStatus();
		
		// Start countdown
		final Timer countdown = new Timer();
		countdown.scheduleAtFixedRate(new TimerTask() {
			int counter = interval;

			public void run() {
				if (counter == 0) {
					this.cancel();
					return;
				}
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						screen.setStatus("Time remaining to cancel: "
								+ String.valueOf(--counter));
						if (counter == 0) {
							screen.setStatus(prevStatus);
							sendMessage(type);
						}
					}
				});
			}
		}, 0, 1000);

		// Set Action button to a cancel button
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				stopSpin();

				// Cancel countdown, restore button to original state,
				// and restore status text
				countdown.cancel();
				if (type.equals(Messager.type_surface)) {
					setSurface();
				} else if (type.equals(Messager.type_alert)) {
					setAlert();
				} else if (type.equals(Messager.type_mandown)) {
					setManDown();
				}
				screen.setStatus(prevStatus);
			}
		});
	}

	private void sendMessage(String type) {
		
		String statusMsg = "Sending ";
		if (type.equals(Messager.type_surface)) {
			statusMsg = statusMsg + "Surface";
			setSurface();
		} else if (type.equals(Messager.type_alert)) {
			statusMsg = statusMsg + "Alert";
			setAlert();
			Messager.sendAlertSMS();
		} else if (type.equals(Messager.type_mandown)) {
			statusMsg = statusMsg + "Man Down";
			setManDown();
			Messager.makeCall();
		}
		statusMsg = statusMsg + "...";
		
		screen.setStatus(statusMsg);
		Messager.sendMessage(type);
		screen.setStatus("Message Sent...");
		screen.setStatus(prevStatus);
		
		stopSpin();
	}

	private void play() {
		try {
			Player player = javax.microedition.media.Manager.createPlayer(
					getClass().getResourceAsStream("/sounds/beep.mp3"),
					"audio/mpeg");
			player.realize();
			VolumeControl volume = (VolumeControl) player
					.getControl("VolumeControl");
			volume.setLevel(100);
			// Direct audio to speaker even if headset/headphones are plugged in
			AudioPathControl apc = (AudioPathControl) player
					.getControl("net.rim.device.api.media.control.AudioPathControl");
			apc.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);
			player.prefetch();
			player.start();
		} catch (Exception e) {
			logger.log(TAG, e.getMessage());
		}
	}
}

class VibrateThread extends Thread {
	private final int[] vibePattern = { 650, 208, 992, 225, 983, 200, 1000,
			200, 1009, 191, 1009, 208, 983, 217, 1042, 216, 1100, 200, 1159,
			200, 1208, 208, 1259, 200, 1308, 208, 1367, 208, 1409, 208, 1258,
			117, 83, 117, 92, 116, 1659, 108, 83, 117, 92, 108, 1667, 108, 92,
			116, 75, 117, 1675, 100, 92, 108, 100, 100, 1667, 116, 84, 108,
			100, 108, 1667, 108, 84, 116, 92, 108, 1667, 108, 92, 108, 100, 92,
			1667, 108, 100, 109, 75, 116, 1675, 100, 92, 117, 91, 109, 1658,
			117, 83, 117, 83, 117, 66, 62225, 13217, 0 };
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