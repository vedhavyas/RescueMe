package org.rescueme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Authored by Vedhavyas Singareddi on 11-09-2014.
 */
public class RescueMeContactListAdapter extends BaseAdapter {

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
        ViewHolder holder;
        RescueMeUserModel contact = contacts.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_each_item_view, null);
            holder = new ViewHolder();
            holder.contactPic = (ImageView) convertView.findViewById(R.id.contactPic);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.number = (TextView) convertView.findViewById(R.id.number);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(contact.getName());
        holder.email.setText(contact.getEmail());
        holder.number.setText(contact.getNumber());

        return convertView;
    }

    class ViewHolder {
        ImageView contactPic;
        TextView name;
        TextView email;
        TextView number;
    }
}
