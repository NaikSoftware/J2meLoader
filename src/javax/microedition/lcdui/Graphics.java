/*
 * Copyright 2012 Kulikov Dmitriy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.lcdui;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelXorXfermode;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Xfermode;

public class Graphics
{
	public static final int HCENTER		= 1;
	public static final int VCENTER		= 2;
	public static final int LEFT		= 4;
	public static final int RIGHT		= 8;
	public static final int TOP			= 16;
	public static final int BOTTOM		= 32;
	public static final int BASELINE	= 64;
	
	public static final int SOLID		= 0;
	public static final int DOTTED		= 1;
	
	public static final int NONE		= -1;
	public static final int CLEAR		= 0;
	public static final int DARKEN		= 1;
	public static final int DST			= 2;
	public static final int DST_ATOP	= 3;
	public static final int DST_IN		= 4;
	public static final int DST_OUT		= 5;
	public static final int DST_OVER	= 6;
	public static final int LIGHTEN		= 7;
	public static final int MULTIPLY	= 8;
	public static final int SCREEN		= 9;
	public static final int SRC			= 10;
	public static final int SRC_ATOP	= 11;
	public static final int SRC_IN		= 12;
	public static final int SRC_OUT		= 13;
	public static final int SRC_OVER	= 14;
	public static final int XOR			= 15;
	
	private static final PorterDuffXfermode[] XFERMODES =
	{
		new PorterDuffXfermode(PorterDuff.Mode.CLEAR),    // 0
		new PorterDuffXfermode(PorterDuff.Mode.DARKEN),   // 1
		new PorterDuffXfermode(PorterDuff.Mode.DST),      // 2
		new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP), // 3
		new PorterDuffXfermode(PorterDuff.Mode.DST_IN),   // 4
		new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),  // 5
		new PorterDuffXfermode(PorterDuff.Mode.DST_OVER), // 6
		new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),  // 7
		new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY), // 8
		new PorterDuffXfermode(PorterDuff.Mode.SCREEN),   // 9
		new PorterDuffXfermode(PorterDuff.Mode.SRC),      // 10
		new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP), // 11
		new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),   // 12
		new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),  // 13
		new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER), // 14
		new PorterDuffXfermode(PorterDuff.Mode.XOR)       // 15
	};
	
	private static final PixelXorXfermode simpleXorMode = new PixelXorXfermode(0);
	private static final PixelXorXfermode invertXorMode = new PixelXorXfermode(-1);

	private Canvas canvas;
	
	private Paint drawPaint;
	private Paint fillPaint;
	private Paint imagePaint;
	
	private int translateX;
	private int translateY;
	
	private Rect intRect;
	private RectF floatRect;
	private Path path;
	
	private Point windowOrg;
	private Rect windowClip;
	private boolean useWindow;
	
	private DashPathEffect dpeffect;
	private int stroke;
	
	private boolean drawAntiAlias;
	private boolean textAntiAlias;
	
	private Font font;
	private char[] singleChar;
	
	public Graphics()
	{
		drawPaint = new Paint();
		fillPaint = new Paint();
		imagePaint = new Paint();
		
		drawPaint.setStyle(Paint.Style.STROKE);
		fillPaint.setStyle(Paint.Style.FILL);
		
		imagePaint.setAlpha(255);
		
		dpeffect = new DashPathEffect(new float[] { 5, 5 }, 0);
		setStrokeStyle(SOLID);
		
		setAntiAlias(false);
		setAntiAliasText(true);
		
		font = Font.getDefaultFont();
		
		windowOrg = new Point();
		windowClip = new Rect();
		useWindow = false;
		
		intRect = new Rect();
		floatRect = new RectF();
		path = new Path();
		singleChar = new char[1];
	}
	
	public Graphics(Canvas canvas)
	{
		this();
		setCanvas(canvas);
	}
	
	public void setCanvas(Canvas canvas)
	{
		this.canvas = canvas;
	}
	
	public Canvas getCanvas()
	{
		return canvas;
	}
	
	public boolean hasCanvas()
	{
		return canvas != null;
	}
	
	public void setColor(int color)
	{
		setColorAlpha(color | 0xFF000000);
	}
	
	public void setColorAlpha(int color)
	{
		drawPaint.setColor(color);
		fillPaint.setColor(color);
	}
	
	public void setColor(int color, int alpha)
	{
		setColorAlpha((color & 0xFFFFFF) | ((alpha & 0xFF) << 24));
	}
	
	public void setColor(int r, int g, int b)
	{
		setColor(255, r, g, b);
	}
	
	public void setColor(int a, int r, int g, int b)
	{
		drawPaint.setARGB(a, r, g, b);
		fillPaint.setARGB(a, r, g, b);
	}
	
	public void setGrayScale(int value)
	{
		setColor(value, value, value);
	}
	
	public int getGrayScale()
	{
		return (getRedComponent() + getGreenComponent() + getBlueComponent()) / 3;
	}
	
	public int getAlphaComponent()
	{
		return drawPaint.getAlpha();
	}
	
	public int getRedComponent()
	{
		return (drawPaint.getColor() >> 16) & 0xFF;
	}
	
	public int getGreenComponent()
	{
		return (drawPaint.getColor() >> 8) & 0xFF;
	}

	public int getBlueComponent()
	{
		return drawPaint.getColor() & 0xFF;
	}
	
	public int getColor()
	{
		return drawPaint.getColor();
	}
	
	public int getDisplayColor(int color)
	{
		return color;
	}
	
	public void setStrokeStyle(int stroke)
	{
		this.stroke = stroke;
		
		if(stroke == DOTTED)
		{
			drawPaint.setPathEffect(dpeffect);
		}
		else
		{
			drawPaint.setPathEffect(null);
		}
	}
	
	public int getStrokeStyle()
	{
		return stroke;
	}
	
	public void setAntiAlias(boolean aa)
	{
		drawAntiAlias = aa;
		
		drawPaint.setAntiAlias(aa);
		fillPaint.setAntiAlias(aa);
	}
	
	public void setAntiAliasText(boolean aa)
	{
		textAntiAlias = aa;
	}
	
	public void setXorMode(int op)
	{
		if(op == 0)
		{
			drawPaint.setXfermode(simpleXorMode);
			fillPaint.setXfermode(simpleXorMode);
		}
		else if(op == -1)
		{
			drawPaint.setXfermode(invertXorMode);
			fillPaint.setXfermode(invertXorMode);
		}
		else
		{
			PixelXorXfermode mode = new PixelXorXfermode(op);
			
			drawPaint.setXfermode(mode);
			fillPaint.setXfermode(mode);
		}
	}
	
	public void setMode(int mode)
	{
		if(mode >= 0 && mode < XFERMODES.length)
		{
			drawPaint.setXfermode(XFERMODES[mode]);
			fillPaint.setXfermode(XFERMODES[mode]);
		}
		else
		{
			drawPaint.setXfermode(null);
			fillPaint.setXfermode(null);
		}
	}
	
	public int getMode()
	{
		Xfermode mode = drawPaint.getXfermode();
		
		for(int i = 0; i < XFERMODES.length; i++)
		{
			if(XFERMODES[i] == mode)
			{
				return i;
			}
		}
		
		return NONE;
	}
	
	public void setFont(Font font)
	{
		this.font = font;
		font.copyInto(drawPaint);
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public void setWindow(int x, int y, int width, int height)
	{
		windowOrg.set(x, y);
		windowClip.set(0, 0, width, height);
		
		canvas.translate(x, y);
		canvas.clipRect(windowClip, Region.Op.REPLACE);
		
		useWindow = true;
	}
	
	public void resetWindow()
	{
		canvas.translate(-windowOrg.x, -windowOrg.y);
		
		windowClip.set(0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.clipRect(windowClip, Region.Op.REPLACE);
		
		useWindow = false;
	}
	
	public void resetClip()
	{
		setClip(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	public void setClip(int x, int y, int width, int height)
	{
		intRect.set(x, y, x + width, y + height);
		
		if(useWindow)
		{
			canvas.clipRect(windowClip, Region.Op.REPLACE);
			canvas.clipRect(intRect, Region.Op.INTERSECT);
		}
		else
		{
			canvas.clipRect(intRect, Region.Op.REPLACE);
		}
	}
	
	public void clipRect(int x, int y, int width, int height)
	{
		intRect.set(x, y, x + width, y + height);
		canvas.clipRect(intRect, Region.Op.INTERSECT);
	}
	
	public void subtractClip(int x, int y, int width, int height)
	{
		intRect.set(x, y, x + width, y + height);
		canvas.clipRect(intRect, Region.Op.DIFFERENCE);
	}
	
	public int getClipX()
	{
		return canvas.getClipBounds().left;
	}
	
	public int getClipY()
	{
		return canvas.getClipBounds().top;
	}
	
	public int getClipWidth()
	{
		return canvas.getClipBounds().width();
	}
	
	public int getClipHeight()
	{
		return canvas.getClipBounds().height();
	}
	
	public void translate(int dx, int dy)
	{
		translateX += dx;
		translateY += dy;
		
		canvas.translate(dx, dy);
	}
	
	public void resetTranslation()
	{
		translate(-translateX, -translateY);
	}
	
	public int getTranslateX()
	{
		return translateX;
	}
	
	public int getTranslateY()
	{
		return translateY;
	}
	
	public void clear(int color)
	{
		canvas.drawColor(color, PorterDuff.Mode.SRC);
	}
	
	public void drawLine(int x1, int y1, int x2, int y2)
	{
		if(x2 >= x1)
		{
			x2++;
		}
		else
		{
			x1++;
		}
		
		if(y2 >= y1)
		{
			y2++;
		}
		else
		{
			y1++;
		}
		
		canvas.drawLine(x1, y1, x2, y2, drawPaint);
	}
	
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
	{
		floatRect.set(x, y, x + width, y + height);
		canvas.drawArc(floatRect, -startAngle, -arcAngle, false, drawPaint);
	}
	
	public void drawArc(RectF oval, int startAngle, int arcAngle)
	{
		canvas.drawArc(oval, -startAngle, -arcAngle, false, drawPaint);
	}
	
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
	{
		floatRect.set(x, y, x + width, y + height);
		canvas.drawArc(floatRect, -startAngle, -arcAngle, true, fillPaint);
	}
	
	public void fillArc(RectF oval, int startAngle, int arcAngle)
	{
		canvas.drawArc(oval, -startAngle, -arcAngle, true, fillPaint);
	}
	
	public void drawRect(int x, int y, int width, int height)
	{
		canvas.drawRect(x, y, x + width, y + height, drawPaint);
	}
	
	public void drawRect(RectF rect)
	{
		canvas.drawRect(rect, drawPaint);
	}
	
	public void fillRect(int x, int y, int width, int height)
	{
		canvas.drawRect(x, y, x + width, y + height, fillPaint);
	}
	
	public void fillRect(RectF rect)
	{
		canvas.drawRect(rect, fillPaint);
	}
	
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		floatRect.set(x, y, x + width, y + height);
		canvas.drawRoundRect(floatRect, arcWidth, arcHeight, drawPaint);
	}
	
	public void drawRoundRect(RectF rect, int arcWidth, int arcHeight)
	{
		canvas.drawRoundRect(rect, arcWidth, arcHeight, drawPaint);
	}
	
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		floatRect.set(x, y, x + width, y + height);
		canvas.drawRoundRect(floatRect, arcWidth, arcHeight, fillPaint);
	}
	
	public void fillRoundRect(RectF rect, int arcWidth, int arcHeight)
	{
		canvas.drawRoundRect(rect, arcWidth, arcHeight, fillPaint);
	}
	
	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		path.reset();
		
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();
		
		canvas.drawPath(path, fillPaint);
	}
	
	public void drawChar(char character, float x, float y, int anchor)
	{
		singleChar[0] = character;
		drawChars(singleChar, 0, 1, x, y, anchor);
	}
	
	public void drawChars(char[] data, int offset, int length, float x, float y, int anchor)
	{
		if(anchor == 0)
		{
			anchor = LEFT | TOP;
		}
		
		if((anchor & Graphics.LEFT) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.LEFT);
		}
		else if((anchor & Graphics.RIGHT) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.RIGHT);
		}
		else if((anchor & Graphics.HCENTER) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.CENTER);
		}

		if((anchor & Graphics.TOP) != 0)
		{
			y -= drawPaint.ascent();
		}
		else if((anchor & Graphics.BOTTOM) != 0)
		{
			y -= drawPaint.descent();
		}
		else if((anchor & Graphics.VCENTER) != 0)
		{
			y -= drawPaint.ascent() + (drawPaint.descent() - drawPaint.ascent()) / 2;
		}
		
		drawPaint.setAntiAlias(textAntiAlias);
		canvas.drawText(data, offset, length, x, y, drawPaint);
		drawPaint.setAntiAlias(drawAntiAlias);
	}
	
	public void drawString(String text, int x, int y, int anchor)
	{
		if(anchor == 0)
		{
			anchor = LEFT | TOP;
		}
		
		if((anchor & Graphics.LEFT) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.LEFT);
		}
		else if((anchor & Graphics.RIGHT) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.RIGHT);
		}
		else if((anchor & Graphics.HCENTER) != 0)
		{
			drawPaint.setTextAlign(Paint.Align.CENTER);
		}

		if((anchor & Graphics.TOP) != 0)
		{
			y -= drawPaint.ascent();
		}
		else if((anchor & Graphics.BOTTOM) != 0)
		{
			y -= drawPaint.descent();
		}
		else if((anchor & Graphics.VCENTER) != 0)
		{
			y -= drawPaint.ascent() + (drawPaint.descent() - drawPaint.ascent()) / 2;
		}
		
		drawPaint.setAntiAlias(textAntiAlias);
		canvas.drawText(text, x, y, drawPaint);
		drawPaint.setAntiAlias(drawAntiAlias);
	}
	
	public void drawImage(Image image, int x, int y, int anchor)
	{
		if((anchor & Graphics.RIGHT) != 0)
		{
			x -= image.getWidth();
		}
		else if((anchor & Graphics.HCENTER) != 0)
		{
			x -= image.getWidth() / 2;
		}
		
		if((anchor & Graphics.BOTTOM) != 0)
		{
			y -= image.getHeight();
		}
		else if((anchor & Graphics.VCENTER) != 0)
		{
			y -= image.getHeight() / 2;
		}
		
		canvas.drawBitmap(image.getBitmap(), x, y, null);
	}
	
	public void drawImage(Image image, int x, int y, int width, int height, boolean filter, int alpha)
	{
		imagePaint.setFilterBitmap(filter);
		imagePaint.setAlpha(alpha);
		
		if(width > 0 && height > 0)
		{
			intRect.set(x, y, x + width, y + height);
			canvas.drawBitmap(image.getBitmap(), null, intRect, imagePaint);
		}
		else
		{
			canvas.drawBitmap(image.getBitmap(), x, y, imagePaint);
		}
	}
	
	public void drawRegion(Image image, int srcx, int srcy, int width, int height, int transform, int dstx, int dsty, int anchor)
	{
		drawImage(Image.createImage(image, srcx, srcy, width, height, transform), dstx, dsty, anchor);
	}
	
	public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha)
	{
		canvas.drawBitmap(rgbData, offset, scanlength, x, y, width, height, processAlpha, drawPaint);
	}
}