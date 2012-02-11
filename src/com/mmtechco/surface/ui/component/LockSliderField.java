package com.mmtechco.surface.ui.component;

import com.mmtechco.util.ToolsBB;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class LockSliderField extends Field {
	private Bitmap thumb;
	private Bitmap slider;
	
	private int numStates;
	private int currentState;
	private boolean selected;
	
	private int totalWidth;
	private int totalHeight;
	
	public LockSliderField(Bitmap slider, Bitmap thumb, int numStates, long style) {
		super(style | Field.FOCUSABLE);
		
		this.slider = slider;
		this.thumb = thumb;
		this.numStates = numStates;
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
        
        // Take all available width
        totalWidth = width;
        initBitmaps();
        totalHeight = slider.getHeight();
        
        setExtent(totalWidth, totalHeight);
	}

	protected void paint(Graphics g) {
		g.drawBitmap(0, 0, totalWidth, totalHeight, slider, 0, 0);
		g.drawBitmap(0, 0, thumb.getWidth(), thumb.getHeight(), thumb, 0, 0);
	}
	
	private void initBitmaps() {
		// Slider should be as wide as field width
		float ratio = (float) slider.getWidth() / slider.getHeight();
		int newWidth = totalWidth;
		int newHeight = (int) (newWidth / ratio);
		slider = ToolsBB.resizeBitmap(slider, newWidth, newHeight,
				Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT);
		
		// Thumb should fit within slider
		ratio = (float) thumb.getWidth() / thumb.getHeight();
		newHeight = slider.getHeight();
		newWidth = (int) (newHeight * ratio);
		thumb = ToolsBB.resizeBitmap(thumb, newWidth, newHeight,
				Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT);
	}

    /**
     * Change the state of the switch. Zero is far left
     * @param on - if true, the switch will be set to on state
     */
    public void setState(int newState) {
        if( newState > numStates ){
            throw new IllegalArgumentException();
        } else {
            currentState = newState;
            invalidate();
        }
    }
}
