package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;


public class RescueMeContacts extends Fragment {


    private ListView contactsListView;
    private Context context;

    public RescueMeContacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_contacts, container, false);
        context = rootView.getContext();
        contactsListView = (ListView) rootView.findViewById(R.id.circlesListView);

        List<RescueMeUserModel> emergencyContacts = getContacts();

        RescueMeContactListAdapter circlesAdapter = new RescueMeContactListAdapter(context, emergencyContacts);
        contactsListView.setAdapter(circlesAdapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RescueMeUserModel contact = (RescueMeUserModel) parent.getItemAtPosition(position);
                startUtilActivity(contact.getId());
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_actionbar_menu, menu);
        MenuItem add = menu.findItem(R.id.add_circle_contact);
        add.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add_circle_contact) {
            startUtilActivity(null);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startUtilActivity(String extraData) {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        if (extraData == null) {
            intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.NEW_EMERGENCY_CONTACT);
        } else {
            intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_EMERGENCY_CONTACT);
            intent.putExtra(RescueMeConstants.COLUMN_ID, extraData);
        }
        startActivity(intent);
    }

    private List<RescueMeUserModel> getContacts() {
        RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTable_name(RescueMeConstants.CONTACTS_TABLE);
        return dbFactory.getAllContacts();
    }
}
