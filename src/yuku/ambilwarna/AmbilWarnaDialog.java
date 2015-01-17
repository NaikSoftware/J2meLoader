package yuku.ambilwarna;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.util.Log;
import android.view.*;
import android.widget.*;
/* change this import */
import ua.naiksoftware.j2meloader.R;

public class AmbilWarnaDialog {
	private static final String TAG = AmbilWarnaDialog.class.getSimpleName();
	
	public interface OnAmbilWarnaListener {
		void onCancel(AmbilWarnaDialog dialog);
		void onOk(AmbilWarnaDialog dialog, int color);
	}
	
	AlertDialog dialog;
	OnAmbilWarnaListener listener;
	View viewHue;
	AmbilWarnaKotak viewKotak;
	ImageView panah;
	View viewWarnaLama;
	View viewWarnaBaru;
	ImageView viewKeker;
	
	float satudp;
	int warnaLama;
	float hue;
	float sat;
	float val;
	
	public AmbilWarnaDialog(Context context, int color, OnAmbilWarnaListener listener) {
		this.listener = listener;
		this.warnaLama = color;
		Color.colorToHSV(color, tmp01);
		hue = tmp01[0];
		sat = tmp01[1];
		val = tmp01[2];
		
		satudp = context.getResources().getDimension(R.dimen.ambilwarna_satudp);
		Log.d(TAG, "satudp = " + satudp);
		
		View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
		viewHue = view.findViewById(R.id.ambilwarna_viewHue);
		viewKotak = (AmbilWarnaKotak) view.findViewById(R.id.ambilwarna_viewKotak);
		panah = (ImageView) view.findViewById(R.id.ambilwarna_panah);
		viewWarnaLama = view.findViewById(R.id.ambilwarna_warnaLama);
		viewWarnaBaru = view.findViewById(R.id.ambilwarna_warnaBaru);
		viewKeker = (ImageView) view.findViewById(R.id.ambilwarna_keker);

		letakkanPanah();
		letakkanKeker();
		viewKotak.setHue(hue);
		viewWarnaLama.setBackgroundColor(color);
		viewWarnaBaru.setBackgroundColor(color);

		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE 
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {
					
					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > 256.f) y = 256.f;
					
					hue = 360.f - 360.f / 256.f * y;
					
					// update view
					viewKotak.setHue(hue);
					letakkanPanah();
					viewWarnaBaru.setBackgroundColor(getWarna());
					
					return true;
				}
				return false;
			}
		});
		viewKotak.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE 
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {
					
					float x = event.getX();
					float y = event.getY();
					
					if (x < 0.f) x = 0.f;
					if (x > 256.f) x = 256.f;
					if (y < 0.f) y = 0.f;
					if (y > 256.f) y = 256.f;

					sat = (1.f / 256.f * x);
					val = 1.f - (1.f / 256.f * y);

					// update view
					letakkanKeker();
					viewWarnaBaru.setBackgroundColor(getWarna());
					
					return true;
				}
				return false;
			}
		});
		
		dialog = new AlertDialog.Builder(context)
		.setView(view)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onOk(AmbilWarnaDialog.this, getWarna());
				}
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
				}
			}
		})
		.create();
		
	}
	
	@SuppressWarnings("deprecation")
	protected void letakkanPanah() {
		float y = 256.f - (hue * 256.f / 360.f);
		
		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) panah.getLayoutParams();
		layoutParams.y = (int) (satudp * (y - 4));
		panah.setLayoutParams(layoutParams);
	}

	@SuppressWarnings("deprecation")
	protected void letakkanKeker() {
		float x = sat * 256.f;
		float y = (1.f - val) * 256.f;
		
		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) viewKeker.getLayoutParams();
		layoutParams.x = (int) (satudp * (x - 5));
		layoutParams.y = (int) (satudp * (y - 5));
		viewKeker.setLayoutParams(layoutParams);
	}

	float[] tmp01 = new float[3];
	private int getWarna() {
		tmp01[0] = hue;
		tmp01[1] = sat;
		tmp01[2] = val;
		return Color.HSVToColor(tmp01);
	}

	public void show() {
		dialog.show();
	}
}
