package org.rescueme;



import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class RescueMeCircles extends Fragment {


    private ListView circlesListView;
    private Context context;

    public RescueMeCircles() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_circles, container, false);
        context = rootView.getContext();
        circlesListView = (ListView)rootView.findViewById(R.id.circlesListView);

        RescueMeUserModel[] circles = {
                                    new RescueMeUserModel("ved","veda@gmail.com","9663556657"),
                                    new RescueMeUserModel("Rushi","Rushi@gmail.com","987654321")
        };

        ArrayAdapter<RescueMeUserModel> circlesAdapter = new ArrayAdapter<RescueMeUserModel>(context,android.R.layout.simple_list_item_1,circles);
        circlesListView.setAdapter(circlesAdapter);

        return rootView;
    }


}
