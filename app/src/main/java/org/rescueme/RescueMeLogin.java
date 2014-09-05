package org.rescueme;



import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RescueMeLogin extends Fragment {

    private EditText email;
    private EditText password;
    private Button logIn;
    private Button register;

    public RescueMeLogin() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_login, container, false);
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.password);
        logIn = (Button) rootView.findViewById(R.id.logInBtn);
        ((RescueMe)getActivity()).setTitle(RescueMeConstants.LOGIN);
        register = (Button)rootView.findViewById(R.id.loginRegisterBtn);
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
        return rootView;
    }

    private class LogIn extends AsyncTask<View,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(View... params) {

            try {
                if(isDataValid()){
                    Thread.sleep(100);
                    return true;
                }else{
                    return false;
                }
            } catch (InterruptedException e) {
                //just for a testing
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if(success){
                Intent intent = new Intent(getActivity().getBaseContext(),RescueMeMainView.class);
                startActivity(intent);
            }else{
                Toast.makeText(getActivity().getBaseContext(),RescueMeConstants.EMAIL_FAIL,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registrationFragment(){
        ((RescueMe)getActivity()).loadFragment(RescueMeConstants.REGISTER);
    }

    private boolean isDataValid(){
        return email.getText().toString().contains("@");
    }
}
