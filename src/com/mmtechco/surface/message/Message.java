package com.mmtechco.surface.message;

public interface Message {
	static final String ID = "id", TIME = "time", TYPE = "type";

	public String toJSON();
}