package com.mmtechco.surface.ui.component;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

public class LockButtonField extends BaseButtonField {
	String text;
	EncodedImage image;
	Font buttonFont;

	// Stroke width for the highlight circle
	int strokeWidth = 6;

	int textWidth, textHeight, totalWidth, totalHeight;

	public LockButtonField(EncodedImage image, String text) {
		super(Field.FOCUSABLE | Field.FIELD_HCENTER);
		this.image = image;
		this.text = text;
	}

	public void applyFont() {
		// Font should be 20% size of the image
		buttonFont = getFont().derive(Font.BOLD, image.getScaledHeight() / 5);
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

		totalWidth = Math.max(image.getScaledWidth() + (strokeWidth * 2),
				textWidth);
		totalHeight = image.getScaledHeight() + (strokeWidth * 2) + textHeight;

		setExtent(totalWidth, totalHeight);
	}

	protected void paint(Graphics g) {
		Font oldFont = g.getFont();
		int oldColor = g.getColor();

		try {
			g.drawBitmap(strokeWidth, strokeWidth, image.getScaledWidth(),
					image.getScaledHeight(), image.getBitmap(), 0, 0);
			if (g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS)) {
				g.setColor(Color.WHITE);
				g.setStrokeWidth(strokeWidth);
				int cx = totalWidth / 2;
				int cy = cx;
				int r = totalWidth / 2 - strokeWidth;
				g.drawEllipse(cx, cy, cx + r, cy, cx, cy + r, 0, 360);
			}
			g.setFont(buttonFont);
			g.drawText(text, 0, (totalHeight - textHeight), DrawStyle.HCENTER,
					totalWidth);
		} finally {
			g.setFont(oldFont);
			g.setColor(oldColor);
			g.setStrokeStyle(1);
		}
	}

	protected void drawFocus(Graphics g, boolean on) {
		// Paint() handles it all
		g.setDrawingStyle(Graphics.DRAWSTYLE_FOCUS, true);
		paintBackground(g);
		paint(g);
	}
}