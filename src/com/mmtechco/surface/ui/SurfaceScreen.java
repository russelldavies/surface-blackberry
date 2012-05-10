//#preprocess
package com.mmtechco.surface.ui;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import com.mmtechco.surface.Settings;
import com.mmtechco.surface.message.EventMessage;
import com.mmtechco.surface.ui.component.ActionButtonField;
import com.mmtechco.surface.ui.container.EvenlySpacedVerticalFieldManager;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.media.control.AudioPathControl;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.LED;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
//#ifdef TOUCH
import net.rim.device.api.ui.component.BitmapField;
//#endif
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class SurfaceScreen extends FullScreen implements ObserverScreen {
	private static final String TAG = ToolsBB .getSimpleClassName(SurfaceScreen.class);
	private static Logger logger = Logger.getInstance();
	
	// Specified in seconds
	static final int interval = 3 * 60;

	private ActionButtonField surfaceButton;

	private LabelField statusLabelField = new LabelField("You are requested to surface",
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
	
	private VibrateThread viber;
	private Player player;

	public SurfaceScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		EvenlySpacedVerticalFieldManager dualManager = new EvenlySpacedVerticalFieldManager(
				USE_ALL_HEIGHT | FIELD_HCENTER);

		// Status field
		StatusField statusField = new StatusField(statusLabelField);

		//#ifdef TOUCH
		// Logo - only added on touch-only devices
		Bitmap logoBitmap = Bitmap.getBitmapResource("surface_logo.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int logoWidth = (int) (Display.getWidth() * 0.9);
		int logoHeight = (int) (logoWidth / ratio);
		dualManager.add(new BitmapField(ToolsBB
				.resizeBitmap(logoBitmap, logoWidth, logoHeight,
						Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT),
				Field.FIELD_HCENTER));
		//#endif

		// Action button
		double factor = 0.75;
		int spinnerSize = (int) (Display.getHeight() * factor);
		int numFrames = 21;
		Bitmap spinner = ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("spinner_surface.png"), spinnerSize
						* numFrames, spinnerSize, Bitmap.FILTER_LANCZOS,
				Bitmap.SCALE_TO_FIT);
		surfaceButton = new ActionButtonField(this, spinner, numFrames,
				interval, Field.FIELD_HCENTER);

		// Add fields to manager
		dualManager.add(surfaceButton);
		dualManager.add(statusField);
		add(dualManager);
		setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
		
		surface();
	}
	
	private void surface() {
		surfaceButton.startCountdown(EventMessage.STATE_SUR, interval);
		
		// Play sound
		if (Settings.genSound) {
			try {
				player = javax.microedition.media.Manager.createPlayer(
						getClass().getResourceAsStream("/sounds/beep.mp3"),
						"audio/mpeg");
				player.realize();
				VolumeControl volume = (VolumeControl) player
						.getControl("VolumeControl");
				volume.setLevel(100);
				// Direct audio to speaker even if headset/headphones are
				// plugged in
				AudioPathControl apc = (AudioPathControl) player
						.getControl("net.rim.device.api.media.control.AudioPathControl");
				apc.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);
				player.prefetch();
				player.start();
			} catch (Exception e) {
				logger.log(TAG, e.getMessage());
			}
		}
		// Vibrate phone to sound
		viber = new VibrateThread();
		viber.start();
		// Blink LED
		LED.setConfiguration(500, 250, LED.BRIGHTNESS_100);
		LED.setState(LED.STATE_BLINKING);
	}
	
	public void stopAlerts() {
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
}