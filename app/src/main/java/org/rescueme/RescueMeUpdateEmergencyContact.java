package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class RescueMeUpdateEmergencyContact extends Fragment {


    RescueMeDBFactory dbFactory;
    private EditText name;
    private EditText email;
    private EditText phoneNumber;
    private Context context;
    private String id;

    public RescueMeUpdateEmergencyContact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_update_emergency_contact, container, false);

        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        phoneNumber = (EditText) rootView.findViewById(R.id.phoneNumber);
        Button updateContactBtn = (Button) rootView.findViewById(R.id.updateContactBtn);
        context = rootView.getContext();
        id = getArguments().getString(RescueMeConstants.COLUMN_ID);

        dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTable_name(RescueMeConstants.CONTACTS_TABLE);
        RescueMeUserModel contact = dbFactory.getContact(id);
        name.setText(contact.getName());
        email.setText(contact.getEmail());
        phoneNumber.setText(contact.getNumber());

        updateContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new updateContactTask().execute();
            }
        });
        return rootView;
    }

    private String isDataValid() {
        if (!isNameEmpty()) {
            if (validEmail()) {
                if (validNumber()) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.PHONE_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        } else {
            return RescueMeConstants.NAME_EMPTY;
        }

    }

    private boolean validEmail() {
        return email.getText().toString().contains(RescueMeConstants.EMAIL_REGEX);

    }

    private boolean validNumber() {
        return phoneNumber.getText().toString().length() == RescueMeConstants.PHONE_NUMBER_LENGTH && phoneNumber.getText().toString().matches("[0-9]+");

    }

    private boolean isNameEmpty() {
        return name.getText().toString().isEmpty();
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(RescueMeConstants.SELECT_TAG, 2);
        startActivity(intent);
    }

    public class updateContactTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = isDataValid();
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                RescueMeUserModel contact = new RescueMeUserModel(name.getText().toString(),
                        email.getText().toString(),
                        phoneNumber.getText().toString());
                contact.setId(id);
                if (dbFactory.updateEmergencyContact(contact) > 0) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.UPDATE_EMERGENCY_CONTACT_FAIL;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                Toast.makeText(context, RescueMeConstants.UPDATE_EMERGENCY_CONTACT_SUCCESS, Toast.LENGTH_SHORT);
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
