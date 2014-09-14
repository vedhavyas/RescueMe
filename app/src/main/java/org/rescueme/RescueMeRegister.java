package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RescueMeRegister extends Fragment {


    private EditText name;
    private EditText email;
    private EditText password;
    private EditText phoneNumber;
    private Context context;

    public RescueMeRegister() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_register, container, false);
        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.password);
        phoneNumber = (EditText) rootView.findViewById(R.id.phoneNumber);
        Button registerBtn = (Button) rootView.findViewById(R.id.registerBtn);
        context = rootView.getContext();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new registerTask().execute();
            }
        });
        ((RescueMe) getActivity()).setTitle(RescueMeConstants.REGISTER);

        return rootView;
    }

    private String isDataValid() {
        if (!isNameEmpty()) {
            if (validEmail()) {
                if (validPassword()) {
                    if (validNumber()) {
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.PHONE_FAIL;
                    }
                } else {
                    return RescueMeConstants.PASSWORD_FAIL;
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

    private boolean validPassword() {
        return password.getText().toString().length() >= RescueMeConstants.PASSWORD_MIN_LENGTH;

    }

    private boolean validNumber() {
        return phoneNumber.getText().toString().length() == RescueMeConstants.PHONE_NUMBER_LENGTH && phoneNumber.getText().toString().matches("[0-9]+");

    }

    private boolean isNameEmpty() {
        return name.getText().toString().isEmpty();
    }

    public class registerTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = isDataValid();
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {

                RescueMeUserModel user = new RescueMeUserModel(name.getText().toString(),
                        password.getText().toString(), email.getText().toString(),
                        phoneNumber.getText().toString());

                RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
                dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
                if (dbFactory.registerUser(user) > 0) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.FAILED;
                }
            } else {
                return result;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                Toast.makeText(context, RescueMeConstants.LOGIN_NOW, Toast.LENGTH_SHORT).show();
                ((RescueMe) getActivity()).loadFragment(RescueMeConstants.LOGIN, null);
            } else {
                Toast.makeText(getActivity().getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
        }

    }

}
