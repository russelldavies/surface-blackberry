package com.mmtechco.surface.ui.component;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;

public class LockButtonField extends BaseButtonField {
	Font font;
	String text;
	EncodedImage image;
	
	int strokeWidth = 5;

	public LockButtonField(EncodedImage image, String text) {
		super(Field.FOCUSABLE | Field.FIELD_HCENTER);

		this.text = text;
		this.image = image;

		try {
			FontFamily typeface = FontFamily.forName("Kabel Dm BT");
			font = typeface.getFont(Font.PLAIN, 20);
		} catch (ClassNotFoundException e) {
		}
	}

	public int getPreferredWidth() {
		return image.getScaledWidth() + strokeWidth;
	}

	public int getPreferredHeight() {
		return image.getScaledHeight() + strokeWidth;
	}

	protected void layout(int width, int height) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}

	protected void paint(Graphics g) {
		g.drawBitmap(strokeWidth, strokeWidth, getWidth(), getHeight(), image.getBitmap(), 0, 0);

		if(g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS)) {
			g.setColor(Color.WHITE);
			g.setStrokeWidth(5);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			int r = cx;
			g.drawEllipse(cx, cy, cx + r, cy, cx, cy + r, 0, 360);
			g.drawRect(0, 0, getWidth(), getHeight());
		}
		/*
		Font oldFont = g.getFont();
		if (font != null) {
			g.setFont(font);
		}
		g.drawText(text, 0, 0, (int) getStyle() & DrawStyle.BOTTOM
				& DrawStyle.HCENTER);
		g.setFont(oldFont);
		*/
	}

	protected void drawFocus(Graphics g, boolean on) {
		// Paint() handles it all
		g.setDrawingStyle(Graphics.DRAWSTYLE_FOCUS, true);
		paintBackground(g);
		paint(g);
	}
}
