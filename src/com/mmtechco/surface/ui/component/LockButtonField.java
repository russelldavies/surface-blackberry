package com.mmtechco.surface.ui.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class LockButtonField extends BaseButtonField {
	String text;
	Bitmap bitmap;
	
	public LockButtonField(String text, Bitmap bitmap) {
		super(Field.FIELD_HCENTER);
		
		this.text = text;
		this.bitmap = bitmap;
	}
	
	public int getPreferredWidth() {
		return bitmap.getWidth();
	}

	public int getPreferredHeight() {
		return bitmap.getHeight();
	}
	
	protected void layout(int width, int height) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}

	protected void paint(Graphics g) {
		g.drawBitmap(0, 0, getPreferredWidth(), getPreferredHeight(), bitmap, 0, 0);
		g.drawText(text, 0, 0, (int) getStyle() & DrawStyle.BOTTOM & DrawStyle.HCENTER);
	}
	
	protected void drawFocus(Graphics g, boolean on) {
		// Paint() handles it all
		g.setDrawingStyle(Graphics.DRAWSTYLE_FOCUS, true);
		paintBackground(g);
		paint(g);
	}
}
