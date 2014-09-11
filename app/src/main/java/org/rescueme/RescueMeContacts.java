package org.rescueme;



import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class RescueMeContacts extends Fragment {


    private ListView circlesListView;
    private Context context;

    public RescueMeContacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_contacts, container, false);
        context = rootView.getContext();
        circlesListView = (ListView)rootView.findViewById(R.id.circlesListView);

        List<RescueMeUserModel> circles2 = new ArrayList<RescueMeUserModel>();
        circles2.add(new RescueMeUserModel("ved","veda@gmail.com","9663556657"));
        circles2.add(new RescueMeUserModel("Rushi","Rushi@gmail.com","987654321"));

        RescueMeContactListAdapter circlesAdapter = new RescueMeContactListAdapter(context,circles2);
        circlesListView.setAdapter(circlesAdapter);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_main_view,menu);
        MenuItem add = menu.findItem(R.id.add_circle_contact);
        add.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId==R.id.add_circle_contact){
            Toast.makeText(context,"Add contact triggered",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
