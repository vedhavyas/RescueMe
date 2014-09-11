package org.rescueme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Vedavyas Singareddy on 11-09-2014.
 */
public class RescueMeCircleListAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private List<RescueMeUserModel> circles;

    public RescueMeCircleListAdapter(Context context, List<RescueMeUserModel> circles){
        this.circles = circles;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return circles.size();
    }

    @Override
    public Object getItem(int position) {
        return circles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
