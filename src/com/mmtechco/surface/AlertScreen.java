package com.mmtechco.surface;

import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.util.Logger;
import com.mmtechco.surface.util.SurfaceResource;
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
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public final class AlertScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(AlertScreen.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private static Logger logger = Logger.getInstance();

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	public AlertScreen() {
		super(Manager.NO_VERTICAL_SCROLL);

		// Give reference of self to Registration so fields can be updated
		Registration.addObserver(this);

		// Set initial text for registration info fields
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(
				VerticalFieldManager.USE_ALL_HEIGHT
						| VerticalFieldManager.USE_ALL_WIDTH
						| VerticalFieldManager.FIELD_HCENTER);

		// Add logo
		vfm.add(new BitmapField(Bitmap.getBitmapResource("logo.png"),
				Field.FIELD_HCENTER));

		// Action button
		FieldChangeListener buttonListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Dialog.alert("Success!!! You clicked the Custom Button!!!");
			}
		};
		BitmapButtonField actionButton = new BitmapButtonField(
				Bitmap.getBitmapResource("reddown.png"),
				Bitmap.getBitmapResource("redup.png"));
		actionButton.setChangeListener(buttonListener);
		vfm.add(actionButton);

		// Registration fields
		vfm.add(statusTextField);
		vfm.add(idTextField);

		// Context Buttons

		HorizontalFieldManager button_hfm = new HorizontalFieldManager(
				HorizontalFieldManager.USE_ALL_WIDTH | Manager.FIELD_HCENTER);

		ButtonField mandownButton = new ButtonField("ManDown",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		ButtonField surfaceButton = new ButtonField("Surface",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		ButtonField alertButton = new ButtonField("Alert",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		ButtonField contextButton = surfaceButton;

		contextButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				// TODO: set action button
			}
		});

		button_hfm.add(mandownButton);
		button_hfm.add(surfaceButton);
		button_hfm.add(alertButton);
		vfm.add(button_hfm);

		vfm.setBackground(BackgroundFactory.createLinearGradientBackground(
				Color.BLACK, Color.BLACK, Color.RED, Color.RED));
		add(vfm);
	}

	/*
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

	public void alert(double longitude, double latitude) {
		// TODO Push surface alert screen
		
	}

	public void close() {
		// App is pushed to background rather than terminated when screen is
		// closed.
		UiApplication.getUiApplication().requestBackground();
	}

	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}


	/**
	 * Button field with a bitmap as its label.
	 */
	public class BitmapButtonField extends Field implements DrawStyle {
		private Bitmap bitmap;
		private Bitmap bitmapHighlight;
		private boolean highlighted = false;

		/**
		 * Instantiates a new bitmap button field.
		 * 
		 * @param bitmap
		 *            the bitmap to use as a label
		 */
		public BitmapButtonField(Bitmap bitmap, Bitmap bitmapHighlight) {
			this(bitmap, bitmapHighlight, ButtonField.CONSUME_CLICK
					| ButtonField.FIELD_HCENTER | ButtonField.FIELD_VCENTER);
		}

		public BitmapButtonField(Bitmap bitmap, Bitmap bitmapHighlight,
				long style) {
			super(style);
			this.bitmap = bitmap;
			this.bitmapHighlight = bitmapHighlight;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.rim.device.api.ui.component.ButtonField#getPreferredWidth()
		 */
		public int getPreferredWidth() {
			return bitmap.getWidth();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.rim.device.api.ui.component.ButtonField#getPreferredHeight()
		 */
		public int getPreferredHeight() {
			return bitmap.getHeight();
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see net.rim.device.api.ui.component.ButtonField#layout(int, int)
		 */
		protected void layout(int width, int height) {
			setExtent(getPreferredWidth(), getPreferredHeight());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.rim.device.api.ui.component.ButtonField#paint(net.rim.device.
		 * api.ui.Graphics)
		 */
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.RED);
			graphics.fillRect(0, 0, getWidth(), getHeight());
			graphics.drawBitmap(0, 0, bitmap.getWidth(), bitmap.getHeight(),
					highlighted ? bitmapHighlight : bitmap, 0, 0);
		}

		public boolean isFocusable() {
			return true;
		}

		protected void onFocus(int direction) {
			highlighted = true;
			invalidate();
		}

		protected void onUnfocus() {
			highlighted = false;
			invalidate();
		}

		protected boolean navigationClick(int status, int time) {
			fieldChangeNotify(1);
			return true;
		}

		protected void fieldChangeNotify(int context) {
			try {
				this.getChangeListener().fieldChanged(this, context);
			} catch (Exception e) {
				logger.log(TAG, e.getMessage());
			}
		}
	}

}
