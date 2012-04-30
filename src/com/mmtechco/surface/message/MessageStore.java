package com.mmtechco.surface.message;

import java.util.Enumeration;

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

	public static synchronized void pushMesage(Message message) {
		messages.insertElementAt(message, 0);
		commit();
	}
	
	public static synchronized Message popMessage() {
		try {
			Message message = (Message) messages.firstElement();
			messages.removeElement(message);
			commit();
			return message;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	public static synchronized EventMessage popEvent() {
		return (EventMessage) getObject(EventMessage.class);
	}
	
	public static synchronized LogMessage popLog() {
		return (LogMessage) getObject(LogMessage.class);
	}
	
	public static synchronized ErrorMessage popError() {
		return (ErrorMessage) getObject(ErrorMessage.class);
	}
	
	private static synchronized Object getObject(Class c) {
		for (Enumeration e = messages.elements(); e.hasMoreElements();){
			Object message = e.nextElement();
			if (c.isInstance(message)) { 
				messages.removeElement(message);
				commit();
				return message;
			}
		}
		return null;
	}

	public static synchronized int length() {
		return messages.size();
	}

	private static void commit() {
		store.setContents(messages);
		store.commit();
	}
}
