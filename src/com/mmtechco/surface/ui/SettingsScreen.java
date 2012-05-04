package com.mmtechco.surface.ui;

import java.util.Enumeration;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import com.mmtechco.surface.Settings;
import com.mmtechco.surface.ui.component.LabeledSwitch;
import com.mmtechco.surface.ui.container.FieldSet;
import com.mmtechco.surface.ui.container.JustifiedHorizontalFieldManager;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.device.api.collection.util.SortedReadableList;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.KeywordFilterField;
import net.rim.device.api.ui.component.KeywordProvider;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.TextSpinBoxField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.SpinBoxFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.StringUtilities;

public class SettingsScreen extends MainScreen implements FieldChangeListener {
	Border titleBorder = BorderFactory.createBitmapBorder(new XYEdges(8, 9, 3,
			9), Bitmap.getBitmapResource("fieldset_title_border.png"));
	Border contentBorder = BorderFactory.createBitmapBorder(new XYEdges(6, 9,
			10, 9), Bitmap.getBitmapResource("fieldset_body_border.png"));

	Bitmap switch_left = Bitmap.getBitmapResource("switch_left.png");
	Bitmap switch_right = Bitmap.getBitmapResource("switch_right.png");
	Bitmap switch_left_focus = Bitmap
			.getBitmapResource("switch_left_focus.png");
	Bitmap switch_right_focus = Bitmap
			.getBitmapResource("switch_right_focus.png");

	LabeledSwitch genSurfaceSwitch, genSoundSwitch;
	LabeledSwitch alertEmergCallSwitch;
	LabeledSwitch shieldOnSwitch, shieldVolumeSwitch;

	public SettingsScreen() {
		setTitle("Surface Settings");
		generalSettings();
		alertSettings();
		shieldSettings();
	}

	private void generalSettings() {
		FieldSet set = generateFieldSet("General");
		genSurfaceSwitch = generateSwitch(set, Settings.genSurface,
				"Follow up Surface\n(sent when you miss a Surface)");
		genSoundSwitch = generateSwitch(set, Settings.genSound, "Sound");
	}

	private void alertSettings() {
		FieldSet set = generateFieldSet("Alert");
		alertEmergCallSwitch = generateSwitch(set, Settings.alertCall,
				"Emergency Call Alert");

		// Emergency number
		String displayText;
		String number = !Settings.emergencyNums.isEmpty() ? (String) Settings.emergencyNums
				.firstElement() : null;
		if (number == null) {
			displayText = "No number set. Using default emergency service number";
		} else {
			displayText = "Current emergency number: " + number;
		}
		set.add(new LabelField(displayText));
		ButtonField numberButton = new ButtonField("Select Emergency Number");
		numberButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (Settings.alertCall) {
					UiApplication.getUiApplication().pushScreen(
							new NumberSelectionScreen());
				} else {
					Dialog.alert("Please enable Emergency Call Alert");
				}
			}
		});
		set.add(numberButton);

		// Stionary alert time
		set.add(new LabelField("Current stationary alert time: "
				+ Settings.alertStationary + " mins"));
		ButtonField stationaryButton = new ButtonField("Stationary Alert Time");
		stationaryButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				String[] selections = { "Set", "Cancel" };
				Dialog addDialog = new Dialog("Stationary Alert Time",
						selections, null, 0, null);

				Integer[] NUMS = new Integer[30];
				for (int i = 0; i < 30; i++) {
					NUMS[i] = new Integer(i + 1);
				}
				SpinBoxFieldManager spinBoxMgr = new SpinBoxFieldManager(
						FIELD_HCENTER);
				spinBoxMgr.setVisibleRows(3);
				spinBoxMgr.setClickToLock(true);
				TextSpinBoxField spinBoxSeconds = new TextSpinBoxField(NUMS,
						TextSpinBoxField.NUMERIC_CHOICES);
				spinBoxMgr.add(spinBoxSeconds);
				addDialog.add(spinBoxMgr);

				if (addDialog.doModal() == 0) {
					Settings.alertStationary = Integer.valueOf(
							spinBoxSeconds.get(spinBoxSeconds.getIndex())
									.toString()).intValue();
				}
			}
		});
		set.add(stationaryButton);
	}

	private void shieldSettings() {
		FieldSet set = generateFieldSet("Shield");
		shieldOnSwitch = generateSwitch(set, Settings.shieldOn,
				"Surface Shield");
		shieldVolumeSwitch = generateSwitch(set, Settings.alertOn,
				"Volume Button Alert");
	}

	private FieldSet generateFieldSet(String text) {
		FieldSet set = new FieldSet(text, titleBorder, contentBorder,
				USE_ALL_WIDTH);
		set.setMargin(10, 10, 10, 10);
		add(set);
		return set;
	}

	private LabeledSwitch generateSwitch(FieldSet set, boolean setting,
			String switchText) {
		LabeledSwitch labeledSwitch = new LabeledSwitch(switch_left,
				switch_right, switch_left_focus, switch_right_focus, "On",
				"Off", setting);
		labeledSwitch.setChangeListener(this);
		JustifiedHorizontalFieldManager switchManager = new JustifiedHorizontalFieldManager(
				new LabelField(switchText), labeledSwitch, false, USE_ALL_WIDTH);
		switchManager.setPadding(5, 5, 5, 5);
		set.add(switchManager);
		return labeledSwitch;
	}

	public void fieldChanged(Field field, int context) {
		if (field == genSurfaceSwitch) {
			Settings.updateSettings(Settings.KEY_GEN_SURFACE,
					Settings.genSurface = genSurfaceSwitch.getOnState());
		} else if (field == genSoundSwitch) {
			Settings.updateSettings(Settings.KEY_GEN_SOUND,
					Settings.genSound = genSoundSwitch.getOnState());
		} else if (field == alertEmergCallSwitch) {
			Settings.updateSettings(Settings.KEY_ALERT_CALL,
					Settings.alertCall = alertEmergCallSwitch.getOnState());
		} else if (field == shieldOnSwitch) {
			Settings.updateSettings(Settings.KEY_SHIELD,
					Settings.shieldOn = shieldOnSwitch.getOnState());
		} else if (field == shieldVolumeSwitch) {
			Settings.updateSettings(Settings.KEY_ALERT_BUTTON,
					Settings.alertOn = shieldVolumeSwitch.getOnState());
		}
	}
}

class NumberSelectionScreen extends MainScreen {
	KeywordFilterField keywordFilterField;
	ContactList contactList;

	public NumberSelectionScreen() {
		buildContactList();
		keywordFilterField = new KeywordFilterField();
		keywordFilterField
				.setLabel("Select number or add other number from menu");
		keywordFilterField.setSourceList(contactList, contactList);

		setTitle(keywordFilterField.getKeywordField());
		add(keywordFilterField);

		addMenuItem(setNumberItem);
	}

	public void buildContactList() {
		contactList = new ContactList();
		try {
			PIM pim = PIM.getInstance();
			BlackBerryContactList contacts = (BlackBerryContactList) pim
					.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
			Enumeration items = contacts
					.items(BlackBerryContactList.SEARCH_CONTACTS);
			while (items.hasMoreElements()) {
				BlackBerryContact contact = (BlackBerryContact) items
						.nextElement();
				// Get first name and last name From Contact List
				String displayName = null;
				if (contact.countValues(Contact.NAME) > 0
						&& contact.countValues(Contact.TEL) > 0) {
					String[] name = contact.getStringArray(Contact.NAME, 0);
					String firstName = name[Contact.NAME_GIVEN];
					String lastName = name[Contact.NAME_FAMILY];
					if (firstName != null && lastName != null) {
						displayName = firstName + " " + lastName;
					} else if (firstName != null) {
						displayName = firstName;
					} else if (lastName != null) {
						displayName = lastName;
					}
					String number = contact.getString(Contact.TEL, 0);
					contactList.addElement(new ContactHolder(displayName,
							number));
				}
			}
		} catch (PIMException e) {
			// TODO: add logging
			Dialog.alert("Could not retrieve contacts");
		}
	}

	protected boolean navigationClick(int status, int time) {
		ContactHolder contact = (ContactHolder) keywordFilterField
				.getSelectedElement();
		if (contact != null) {
			Settings.emergencyNums.addElement(contact.getNumber());
		}
		close();
		return true;
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ENTER) {
			return navigationClick(0, 0);
		}
		return super.keyChar(c, status, time);
	}

	private final MenuItem setNumberItem = new MenuItem("Set number", 0, 0) {
		public void run() {
			keywordFilterField.setKeyword("");

			String[] selections = { "Set", "Cancel" };
			Dialog addDialog = new Dialog("Set Number", selections, null, 0,
					null);
			BasicEditField inputField = new BasicEditField("Number: ", "", 50,
					BasicEditField.FILTER_PHONE);
			addDialog.add(inputField);

			if (addDialog.doModal() == 0) {
				Settings.emergencyNums.addElement(inputField.getText());
			}
		}
	};
}

class ContactList extends SortedReadableList implements KeywordProvider {
	public ContactList() {
		super(new ContactListComparator());
	}

	void addElement(Object element) {
		doAdd(element);
	}

	public String[] getKeywords(Object element) {
		if (element instanceof ContactHolder) {
			return StringUtilities.stringToWords(element.toString());
		}
		return null;
	}

	final static class ContactListComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			if (o1 == null || o2 == null)
				throw new IllegalArgumentException(
						"Cannot compare null contacts");
			return o1.toString().compareTo(o2.toString());
		}
	}
}

class ContactHolder {
	private String name, number;

	public ContactHolder(String name, String number) {
		this.name = name;
		this.number = number;
	}

	public String toString() {
		return name;
	}

	public String getNumber() {
		return number;
	}
}