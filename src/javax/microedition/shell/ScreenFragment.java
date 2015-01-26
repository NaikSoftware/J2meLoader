package javax.microedition.shell;

import javax.microedition.lcdui.Displayable;

import ua.naiksoftware.j2meloader.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScreenFragment extends Fragment {

	private Displayable displayable;

	public ScreenFragment() {
	}

	public ScreenFragment(Displayable d) {
		displayable = d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return displayable.getDisplayableView();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}

	public Displayable getDisplayable() {
		return displayable;
	}
}
