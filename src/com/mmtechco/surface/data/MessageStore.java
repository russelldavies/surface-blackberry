package com.mmtechco.surface.data;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

/**
 * Simple stack to hold messages (events, commands, logs, usage, errors). It is
 * persistent across device resets.
 */
public class MessageStore {
	public static final long ID = StringUtilities
			.stringHashToLong(MessageStore.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector messages;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			messages = new ContentProtectedVector();
			store.setContents(messages);
		}
		messages = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addMessage(Object event) {
		messages.insertElementAt(event, 0);
		commit();
	}

	public static synchronized boolean removeMessage() {
		if (hasNext()) {
			messages.removeElementAt(0);
			commit();
			return true;
		}
		return false;
	}

	public static synchronized Object next() {
		if (hasNext()) {
			return messages.firstElement();
		}
		return null;
	}

	private static synchronized boolean hasNext() {
		if (messages.size() > 0) {
			return true;
		}
		return false;
	}

	public static synchronized int length() {
		return messages.size();
	}

	private static void commit() {
		store.setContents(messages);
		store.commit();
	}
}
