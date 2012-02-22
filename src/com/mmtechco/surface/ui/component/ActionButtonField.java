package com.mmtechco.surface.ui.component;

import java.util.Timer;
import java.util.TimerTask;

import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.ui.SurfaceScreen;
import com.mmtechco.surface.ui.ToastPopupScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;

public class ActionButtonField extends BaseButtonField {
	private static final String TAG = ToolsBB
			.getSimpleClassName(ActionButtonField.class);
	private static Logger logger = Logger.getInstance();

	private Bitmap spinner;
	private int numFrames;
	private int frameWidth;
	private int frameHeight;
	private int currentFrame;
	private int timerID = -1;
	private boolean spinning;
	
	private Bitmap button;
	private int buttonWidth;
	private int buttonHeight;
	private String buttonText;
	private Font buttonFont;
	
	// Specified in seconds
	private int interval;

	private ObserverScreen screen;
	private static Application app = Application.getApplication();

	//public ActionButtonField(ObserverScreen screen, int fieldSize, long style) {
	public ActionButtonField(ObserverScreen screen, Bitmap spinner, int numFrames, int interval, long style) {
		super(style);
		this.screen = screen;
		this.spinner = spinner;
		this.numFrames = numFrames;
		this.interval = interval;
		
		frameWidth = spinner.getWidth() / numFrames;
		frameHeight = spinner.getHeight();
		
		// Button should be 60% the size of the spinner
		double factor = 0.6;
		buttonWidth = (int) (frameWidth * factor);
		buttonHeight = (int) (frameHeight * factor);
		button = ToolsBB.resizeBitmap(
				Bitmap.getBitmapResource("alertbutton_normal.png"),
				buttonWidth, buttonHeight, Bitmap.FILTER_LANCZOS,
				Bitmap.SCALE_TO_FIT);
		
		// Surface is default action
		setSurface();
	}
	
	/*
	public void applyFont() {
		// Font should be 20% size of the image
		buttonFont.getAdvance(text);
		buttonFont = getFont().derive(Font.BOLD, image.getScaledHeight() / 5);
	}
	*/

	public int getPreferredWidth() {
		return frameWidth;
	}

	public int getPreferredHeight() {
		return frameHeight;
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
		g.drawBitmap((frameWidth - buttonWidth) / 2,
				(frameHeight - buttonHeight) / 2, buttonWidth, buttonHeight,
				button, 0, 0);

		// Draw text
		int oldColor = g.getColor();
		Font oldFont = g.getFont();
		try {
			g.setColor(Color.WHITE);
			FontFamily typeface = FontFamily.forName("Kabel Dm BT");
			Font font = typeface.getFont(Font.PLAIN, 40);
			g.setFont(font);
			g.drawText(buttonText, 0, frameWidth / 2, DrawStyle.HCENTER,
					frameWidth);
		} catch (ClassNotFoundException e) {
			logger.log(TAG, e.getMessage());
		} finally {
			g.setColor(oldColor);
			g.setFont(oldFont);
		}
	}

	private void setButtonText(String text) {
		this.buttonText = text;
		invalidate();
	}

	private void startSpin() {
		spinning = true;
		if (timerID == -1) {
			timerID = app.invokeLater(new Runnable() {
				public void run() {
					if (spinning) {
						invalidate();
					}
				}
			}, (interval * 1000 / numFrames), true);
		}
	}

	private void stopSpin() {
		spinning = false;
		if (timerID != -1) {
			app.cancelInvokeLater(timerID);
			timerID = -1;
			currentFrame = 0;
		}
	}

	public void setSurface() {
		setButtonText("Surface");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				sendMessage(Messager.type_surface);
			}
		});
	}

	public void setAlert() {
		setButtonText("Alert");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				startCountdown(Messager.type_alert, interval);
			}
		});
	}

	public void setManDown() {
		setButtonText("Man Down");
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				startCountdown(Messager.type_mandown, interval);
			}
		});
	}

	public void startCountdown(final String type, final int interval) {
		startSpin();
		final String origStatus = screen.getStatus();

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
							if (type.equals(Messager.type_surface)) {
								((Screen)screen).close();
							} else {
								screen.setStatus(origStatus);
								sendMessage(type);
							}
						}
					}
				});
			}
		}, 0, 1000);

		// Set Action button to a cancel button
		if (type.equals(Messager.type_surface)) {
			setButtonText("Surface");
		} else {
			setButtonText("Cancel");
		}
		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				// Cancel countdown, restore button to original state,
				// and restore status text
				stopSpin();
				countdown.cancel();
				if (type.equals(Messager.type_surface)) {
					((SurfaceScreen)screen).stopAlerts();
					sendMessage(type);
					((Screen)screen).close();
				} else if (type.equals(Messager.type_alert)) {
					setAlert();
				} else if (type.equals(Messager.type_mandown)) {
					setManDown();
				}
				screen.setStatus(origStatus);
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
		//screen.setStatus(statusMsg);
		Messager.sendMessage(type, statusMsg);
		//screen.setStatus("Message Sent...");
		//screen.setStatus(prevStatus);
		stopSpin();
	}
}