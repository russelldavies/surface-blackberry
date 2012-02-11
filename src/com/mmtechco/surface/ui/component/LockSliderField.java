package com.mmtechco.surface.ui.component;

import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;

public class LockSliderField extends Field {
	private Bitmap thumb;
	private Bitmap slider;

	private int numStates;
	private int currentState;
	public final int initialState;
	public final int finalState;

	private int totalWidth;
	private int totalHeight;

	private int thumbWidth;
	private int thumbHeight;

	public LockSliderField(Bitmap slider, Bitmap thumb, int numStates,
			int initialState) {
		super(Field.FOCUSABLE | Field.FIELD_HCENTER);

		this.slider = slider;
		this.thumb = thumb;
		this.numStates = numStates;
		setState(this.initialState = initialState);
		
		if(initialState < numStates) {
			finalState = numStates;
		} else {
			finalState = 0;
		}
	}

	public int getPreferredWidth() {
		return totalWidth;
	}

	public int getPreferredHeight() {
		return totalHeight;
	}

	protected void layout(int width, int height) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException();

		// Take all available width but leave some side margin
		totalWidth = width - 40;

		// Slider should be as wide as field width
		float ratio = (float) slider.getWidth() / slider.getHeight();
		int newWidth = totalWidth;
		int newHeight = (int) (newWidth / ratio);
		slider = ToolsBB.resizeBitmap(slider, newWidth, newHeight,
				Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT);
		totalHeight = slider.getHeight();

		// Thumb should fit within slider
		ratio = (float) thumb.getWidth() / thumb.getHeight();
		newHeight = slider.getHeight();
		newWidth = (int) (newHeight * ratio);
		thumb = ToolsBB.resizeBitmap(thumb, newWidth, newHeight,
				Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT);
		thumbWidth = thumb.getWidth();
		thumbHeight = thumb.getHeight();

		setExtent(totalWidth, totalHeight);
	}

	protected void paint(Graphics g) {
		int thumbXOffset = ((totalWidth - thumbWidth) * currentState)
				/ numStates;
		g.drawBitmap(thumbXOffset, (totalHeight - thumbHeight) >> 1,
				thumbWidth, thumbHeight, thumb, 0, 0);
	}

	protected void paintBackground(Graphics g) {
		g.drawBitmap(0, 0, totalWidth, totalHeight, slider, 0, 0);
	}

	protected void drawFocus(Graphics g, boolean on) {
		// Empty method to prevent default focus effect
	}

	protected boolean touchEvent(TouchEvent message) {
		boolean isConsumed = false;
		boolean isOutOfBounds = false;
		int x = message.getX(1);
		int y = message.getY(1);
		// Check to ensure point is within this field
		if (x < 0 || y < 0 || x > getExtent().width || y > getExtent().height) {
			isOutOfBounds = true;
		}
		switch (message.getEvent()) {
		case TouchEvent.CLICK:
		case TouchEvent.MOVE:
			if (isOutOfBounds) {
				// Consume
				return true;
			}
			// update state
			int stateWidth = getExtent().width / numStates;
			int numerator = x / stateWidth;
			int denominator = x % stateWidth;
			if (denominator > stateWidth / 2) {
				numerator++;
			}
			currentState = numerator;
			invalidate();
			// Thumb is in actionable position
			if(currentState == finalState) {
				fieldChangeNotify(0);
			}
			isConsumed = true;
			break;
		case TouchEvent.UNCLICK:
		case TouchEvent.UP:
			setState(initialState);
			invalidate();
			isConsumed = true;
			break;
		}
		return isConsumed;
	}

	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		if (dx > 0 || dy > 0) {
			incrementState();
			//fieldChangeNotify(0);
			return true;
		} else if (dx < 0 || dy < 0) {
			decrementState();
			//fieldChangeNotify(0);
			return true;
		}
		return super.navigationMovement(dx, dy, status, time);
	}

	public void decrementState() {
		if (currentState > 0) {
			currentState--;
			invalidate();
		}
	}

	public void incrementState() {
		if (currentState < numStates) {
			currentState++;
			invalidate();
		}
	}

	public void setState(int newState) {
		if (newState > numStates) {
			throw new IllegalArgumentException();
		} else {
			currentState = newState;
			invalidate();
		}
	}

	public void setDirty(boolean dirty) {
		// We never want to be dirty or muddy
	}

	public void setMuddy(boolean muddy) {
		// We never want to be dirty or muddy
	}
}
