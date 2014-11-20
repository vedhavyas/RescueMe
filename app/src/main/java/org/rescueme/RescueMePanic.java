package org.rescueme;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class RescueMePanic extends Fragment {


    private Context context;

    public RescueMePanic() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_panic, container, false);
        context = rootView.getContext();
        ImageButton panicBtn = (ImageButton) rootView.findViewById(R.id.panicBtn);
        panicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox();
            }
        });
        return rootView;
    }


    private void showDialogBox() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(RescueMeConstants.DIALOG_TITLE);
        alertDialog.setMessage(RescueMeConstants.DIALOG_MESSAGE);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, RescueMeConstants.PANIC_FALSE_ALARM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RescueMeUtilClass.writeToLog("Emergency dialog Dismissed!!");
                dialog.cancel();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, RescueMeConstants.PANIC_CNF, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RescueMeUtilClass.writeToLog("Emergency Confirmed!!");
                RescueMeUtilClass.sendEmergencyAlerts(getActivity());
            }
        });

        alertDialog.show();

    }

}
