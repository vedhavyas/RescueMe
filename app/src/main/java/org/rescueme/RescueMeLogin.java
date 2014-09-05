package org.rescueme;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class RescueMeLogin extends Fragment {

    private EditText email;
    private EditText password;
    private SharedPreferences prefs;

    public RescueMeLogin() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_login, container, false);
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.password);
        Button logIn = (Button) rootView.findViewById(R.id.logInBtn);
        ((RescueMe)getActivity()).setTitle(RescueMeConstants.LOGIN);
        Button register = (Button) rootView.findViewById(R.id.loginRegisterBtn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrationFragment();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogIn().execute(v);
            }
        });
        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return rootView;
    }

    private class LogIn extends AsyncTask<View,Void,String>{
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(View... params) {
            if(isEmailValid()){
                if(!isPasswordEmpty()){
                    RescueMeUserModel user = new RescueMeUserModel(email.getText().toString(),password.getText().toString());
                    RescueMeDBFactory dbFactory = new RescueMeDBFactory(getActivity().getBaseContext(),RescueMeConstants.USER_TABLE);
                    if(dbFactory.loginUser(user)){
                        return RescueMeConstants.SUCCESS;
                    }else{
                        return RescueMeConstants.LOGIN_FAIL;
                    }
                }else{
                    return RescueMeConstants.PASSWORD_FAIL;
                }
            }else{
                return RescueMeConstants.EMAIL_FAIL;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if(result.equalsIgnoreCase(RescueMeConstants.SUCCESS)){
                prefs.edit().putBoolean(RescueMeConstants.LOGIN,true).apply();
                Intent intent = new Intent(getActivity().getBaseContext(),RescueMeMainView.class);
                startActivity(intent);
            }else{
                Toast.makeText(getActivity().getBaseContext(),result,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registrationFragment(){
        ((RescueMe)getActivity()).loadFragment(RescueMeConstants.REGISTER);
    }

    private boolean isEmailValid(){
        return email.getText().toString().contains("@");
    }

    private boolean isPasswordEmpty() { return password.getText().toString().isEmpty(); }

}
