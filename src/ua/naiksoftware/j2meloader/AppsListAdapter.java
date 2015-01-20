package ua.naiksoftware.j2meloader;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import ua.naiksoftware.j2meloader.R;

/**
 *
 * @author Naik
 */
public class AppsListAdapter extends BaseAdapter {

	private List<AppItem> list;
	private final LayoutInflater li;

	public AppsListAdapter(Context context, List<AppItem> arr) {
		if (arr != null) {
			list = arr;
		}
		li = LayoutInflater.from(context);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup viewGroup) {
    	ViewHolder holder;
        if (view == null) {
            view = li.inflate(R.layout.list_row_jar, null);
            holder = new  ViewHolder();
            holder.icon = (ImageView) view.findViewById(R.id.list_image);
            holder.name = (TextView) view.findViewById(R.id.list_title);
			holder.author = (TextView) view.findViewById(R.id.list_author);
			holder.version = (TextView) view.findViewById(R.id.list_version);
            view.setTag(holder);
        } else {
        	holder = (ViewHolder) view.getTag();
        }
        AppItem item = list.get(position);

        holder.icon.setImageResource(item.getImageId());
        holder.name.setText(item.getTitle());
		holder.author.setText(item.getAuthor());
		holder.version.setText(item.getVersion());
        
        return view;
    }

	private static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView author;
		TextView version;
	}
}
