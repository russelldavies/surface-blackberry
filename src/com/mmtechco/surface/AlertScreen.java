package com.mmtechco.surface;

import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.MMTools;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.ui.BitmapButtonField;
import com.mmtechco.surface.ui.ForegroundManager;
import com.mmtechco.surface.ui.ListStyleButtonField;
import com.mmtechco.surface.ui.ListStyleButtonSet;
import com.mmtechco.surface.ui.NegativeMarginVerticalFieldManager;
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
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.FlowFieldManager;
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
	private static MMTools tools = ToolsBB.getInstance();

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	/*
	Manager contentOne;
	Manager contentTwo;
	Manager contentThree;
	*/

	Manager bodyWrapper;
	//Manager currentBody;
	
	LabelField lf1;
	LabelField lf2;
	LabelField lf3;
	LabelField lf_current;
	

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
		//BitmapButtonField actionButton = new BitmapButtonField( Bitmap.getBitmapResource("reddown.png"), Bitmap.getBitmapResource("redup.png"));
		//actionButton.setChangeListener(new FieldChangeListener() {
		//	public void fieldChanged(Field field, int context) {
		//		Dialog.alert("you clicked me");
		//	}
		//});
		//vfm.add(actionButton);

		// Registration fields
		vfm.add(statusTextField);
		vfm.add(idTextField);

		// Context Buttons

		// ButtonField contextButton = surfaceButton;
		// contextButton.setChangeListener(new FieldChangeListener() {
		// public void fieldChanged(Field field, int context) {
		// // TODO: set action button
		// }
		// });


		PillButtonSet pills = new PillButtonSet();
		PillButtonField pillOne = new PillButtonField("ManDown");
		PillButtonField pillTwo = new PillButtonField("Surface");
		PillButtonField pillThree = new PillButtonField("Alert");
		pills.add(pillOne);
		pills.add(pillTwo);
		pills.add(pillThree);
		pills.setMargin(15, 15, 5, 15);
		pills.setSelectedField(pillTwo);

		
		bodyWrapper = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		/*
		contentOne = new ListStyleButtonSet();
		contentOne.add(new ListStyleButtonField("Home", 0));
		contentTwo = new ListStyleButtonSet();
		contentTwo.add(new ListStyleButtonField("Work Address", 0));
		contentThree = new ListStyleButtonSet();
		contentThree.add(new ListStyleButtonField("Address", 0));
		currentBody = contentTwo;
		*/
		
		lf1 = new LabelField("lf1");
		lf2 = new LabelField("lf2");
		lf3 = new LabelField("lf3");
		lf_current = lf2;

		bodyWrapper.add(lf_current);

		pillOne.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (lf_current != lf1) {
					bodyWrapper.replace(lf_current, lf1);
					lf_current = lf1;
				}
			}
		});
		pillTwo.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (lf_current != lf2) {
					bodyWrapper.replace(lf_current, lf2);
					lf_current = lf2;
				}
			}
		});
		pillThree.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (lf_current != lf3) {
					bodyWrapper.replace(lf_current, lf3);
					lf_current = lf3;
				}
			}
		});


		vfm.add(bodyWrapper);
		vfm.add(pills);
		
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

	public void alert(final double longitude, final double latitude) {
		// Bring screen to front
		UiApplication.getUiApplication().requestForeground();

		new Thread() {
			public void run() {
				String queryString = Registration.getRegID()
						+ Tools.ServerQueryStringSeparator
						+ Constants.type_surface
						+ Tools.ServerQueryStringSeparator + tools.getDate()
						+ Tools.ServerQueryStringSeparator + latitude
						+ Tools.ServerQueryStringSeparator + longitude;
				new Server().contactServer(queryString);
			}
		}.start();

	}

	//public void close() {
		// App is pushed to background rather than terminated when screen is
		// closed.
		// TODO: enable me
		// UiApplication.getUiApplication().requestBackground();
	//}

	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}
}
