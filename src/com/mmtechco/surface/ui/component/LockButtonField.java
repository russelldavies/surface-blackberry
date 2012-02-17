package com.mmtechco.surface.ui.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

public class LockButtonField extends BaseButtonField {
	String text;
	Bitmap bitmap;
	Font buttonFont;

	// Stroke width for the highlight circle
	int strokeWidth = 6;

	int textWidth, textHeight, totalWidth, totalHeight;

	public LockButtonField(Bitmap bitmap, String text) {
		super(Field.FOCUSABLE | Field.FIELD_HCENTER);
		this.bitmap = bitmap;
		this.text = text;
	}

	public void applyFont() {
		// Font should be 20% size of the image
		buttonFont = getFont().derive(Font.BOLD, bitmap.getWidth() / 5);
	}

	public int getPreferredWidth() {
		return totalWidth;
	}

	public int getPreferredHeight() {
		return totalHeight;
	}

	protected void layout(int width, int height) {
		textWidth = buttonFont.getAdvance(text);
		textHeight = buttonFont.getHeight();

		totalWidth = Math.max(bitmap.getWidth() + (strokeWidth * 2), textWidth);
		totalHeight = bitmap.getHeight() + (strokeWidth * 2) + textHeight;

		setExtent(totalWidth, totalHeight);
	}

	protected void paint(Graphics g) {
		g.drawBitmap(strokeWidth, strokeWidth, bitmap.getWidth(),
				bitmap.getHeight(), bitmap, 0, 0);

		Font oldFont = g.getFont();
		try {
			g.setFont(buttonFont);
			g.drawText(text, 0, (totalHeight - textHeight), DrawStyle.HCENTER,
					totalWidth);
		} finally {
			g.setFont(oldFont);
		}
	}

	protected void drawFocus(Graphics g, boolean on) {
		int oldColor = g.getColor();
		try {
			g.setColor(Color.WHITE);
			g.setStrokeWidth(strokeWidth);
			int cx = totalWidth / 2;
			int cy = cx;
			int r = totalWidth / 2 - strokeWidth;
			g.drawEllipse(cx, cy, cx + r, cy, cx, cy + r, 0, 360);
		} finally {
			g.setColor(oldColor);
			g.setStrokeStyle(1);
		}
	}
}