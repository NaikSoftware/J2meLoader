package ua.naiksoftware.j2meloader;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import java.util.List;
import javax.microedition.shell.ConfigActivity;

/**
 * 
 * @author Naik
 */
public class AppsListFragment extends ListFragment {
	
	private List<AppItem> apps;
	
	public AppsListFragment() {
	}
	
	public AppsListFragment(List<AppItem> apps) {
		this.apps = apps;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AppItem item = apps.get(position);
		Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse(item.getPath()), getActivity(), ConfigActivity.class);
		startActivityForResult(i, 0);
	}
}
