package org.rescueme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Authored by Vedhavyas Singareddi on 11-09-2014.
 */
public class RescueMeContactListAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private List<RescueMeUserModel> contacts;

    public RescueMeContactListAdapter(Context context, List<RescueMeUserModel> contacts) {
        this.contacts = contacts;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (contacts == null) {
            return 0;
        } else {
            return contacts.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.contact_each_item_view,null);

        RescueMeUserModel contact = contacts.get(position);

        TextView nameView = (TextView)view.findViewById(R.id.nameView);
        TextView numberView = (TextView)view.findViewById(R.id.numberView);

        nameView.setText(contact.getName());
        numberView.setText(contact.getNumber());

        return view;
    }
}
