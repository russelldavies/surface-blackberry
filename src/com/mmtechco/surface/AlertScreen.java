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
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.TextField;
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
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE) {
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.WHITE);
			super.paint(graphics);
		}
	};
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE) {
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.WHITE);
			super.paint(graphics);
		}
	};

	final BitmapTextButtonField actionButton;
	PillButtonSet pills;
	PillButtonField pillOne;
	PillButtonField pillTwo;
	PillButtonField pillThree;
	
	// In milliseconds
	public final int cooldownInterval = 30 * 1000;

	public AlertScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		// Give reference of self to Registration so fields can be updated
		Registration.addObserver(this);

		// Set initial text for registration info fields
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

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
				if (!regId.equals("0")) {
					idTextField.setText(regId);
				}
				statusTextField.setText(Registration.getStatus());
			}
		});
	}

	/**
	 * Show screen and request user to surface
	 */
	public void alert(final double longitude, final double latitude) {
		// Bring screen to front
		UiApplication.getUiApplication().requestForeground();
		// Select Surface pill
		pills.setSelectedField(pillOne);
		// TODO: Makes sound and vibrates
		// TODO: countdown timer of 3 minutes, show progress bar
		// text goes to sending, and then sent

	}

	public void close() {
		super.close();
		// App is pushed to background rather than terminated when screen is
		// closed.
		// TODO: enable this
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
			// Registration fields
			add(statusTextField);
			add(idTextField);

			// setPadding(5, 5, 5, 5);
			// setMargin(10, 10, 10, 10);
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
		g.drawText(text, getWidth() / 2, getHeight() / 2);
	}

	public void setText(String text) {
		this.text = text;
		invalidate();
	}

	public void sendSurface() {
	}

	public void sendAlert() {
	}

	public void sendManDown() {
	}

	public void sendMessage(final String type) {
		// Threaded so non-blocking
		new Thread() {
			public void run() {
				String queryString = Registration.getRegID()
						+ Tools.ServerQueryStringSeparator + type
						+ Tools.ServerQueryStringSeparator
						+ ToolsBB.getInstance().getDate()
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.latitude
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.longitude;
				System.out.println(queryString);
				new Server().contactServer(queryString);
			}
		}.start();
	}

	public void setSurface() {
		setText("Surface");

		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				sendMessage(Constants.type_surface);
				Dialog.alert("surface message sent");
			}
		});
	}

	public void setAlert() {
		setText("Alert");

		setChangeListener(null);
		setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {

				sendMessage(Constants.type_alert);
				Dialog.alert("surface message sent");
			}
		});

		new Timer().scheduleAtFixedRate(new CountdownTask(30), 0, 1000);

	}

	public void setManDown() {
		setText("Man Down");
	}
}

class CountdownTask extends TimerTask {
	int counter = 0;
	int duration;
	
	public CountdownTask(int duration) {
		this.duration = duration;
	}
	public void run() {
		if (counter == duration) {
			// TODO: send message
		}
		counter++;
	}

}