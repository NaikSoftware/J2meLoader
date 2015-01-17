package yuku.ambilwarna;

import android.content.*;
import android.graphics.*;
import android.graphics.Shader.TileMode;
import android.util.*;
import android.view.*;
/* change this import */
import ua.naiksoftware.j2meloader.R;

public class AmbilWarnaKotak extends View {

    public AmbilWarnaKotak(Context context) {
        this(context, null);
    }

    public AmbilWarnaKotak(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmbilWarnaKotak(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        satudp = context.getResources().getDimension(R.dimen.ambilwarna_satudp);
    }
    Paint paint;
    Shader dalam;
    Shader luar;
    float hue;
    float satudp;
    float[] tmp00 = new float[3];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paint == null) {
            paint = new Paint();
            luar = new LinearGradient(0.f, 0.f, 0.f, 256.f, 0xffffffff, 0xff000000, TileMode.CLAMP);
        }

        tmp00[1] = tmp00[2] = 1.f;
        tmp00[0] = hue;
        int rgb = Color.HSVToColor(tmp00);

        dalam = new LinearGradient(0.f, 0.f, 256.f, 0.f, 0xffffffff, rgb, TileMode.CLAMP);
        ComposeShader shader = new ComposeShader(luar, dalam, PorterDuff.Mode.MULTIPLY);

        paint.setShader(shader);

        canvas.drawRect(0.f, 0.f, 256.f, 256.f, paint);
    }

    void setHue(float hue) {
        this.hue = hue;
        invalidate();
    }
}
