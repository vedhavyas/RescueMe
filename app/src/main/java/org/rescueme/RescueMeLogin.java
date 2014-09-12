package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;


public class RescueMeLogin extends Fragment {

    private EditText email;
    private EditText password;
    private SharedPreferences prefs;
    private Button logInBtn;
    private Button register;
    private ImageButton fbLogInBtn;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private Context context;

    public RescueMeLogin() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_login, container, false);

        //initialize variables
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.password);
        logInBtn = (Button) rootView.findViewById(R.id.logInBtn);
        fbLogInBtn = (ImageButton)rootView.findViewById(R.id.fbLoginBtn);
        register = (Button) rootView.findViewById(R.id.loginRegisterBtn);
        context = rootView.getContext();

        //initialize functions
        ((RescueMe)getActivity()).setTitle(RescueMeConstants.LOGIN);
        setFbLoginListener();

        //set onClickListners
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegistrationFragment();
            }
        });
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogIn().execute(v);
            }
        });
        fbLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleFacebook.login(fbLoginListener);
            }
        });

        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    private void loadRegistrationFragment(){
        ((RescueMe) getActivity()).loadFragment(RescueMeConstants.REGISTER);
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private boolean isEmailValid() {
        return email.getText().toString().contains("@");
    }

    private boolean isPasswordEmpty() {
        return password.getText().toString().isEmpty();
    }

    private void setFbLoginListener() {
        fbLoginListener = new OnLoginListener() {
            @Override
            public void onLogin() {
                Toast.makeText(context, RescueMeConstants.FB_LOGIN_SUCCESS, Toast.LENGTH_SHORT)
                        .show();
                loadAuthenticatedActivity();
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                Toast.makeText(context, RescueMeConstants.FB_NOT_ACCEPT_PERMISSIONS, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onThinking() {

            }

            @Override
            public void onException(Throwable throwable) {
                Toast.makeText(context, RescueMeConstants.FB_EXCEPTION_LOGIN, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onFail(String s) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private class LogIn extends AsyncTask<View, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(View... params) {
            if (isEmailValid()) {
                if (!isPasswordEmpty()) {
                    RescueMeUserModel user = new RescueMeUserModel(email.getText().toString()
                            , password.getText().toString());
                    RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
                    dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
                    if (dbFactory.loginUser(user)) {
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.LOGIN_FAIL;
                    }
                } else {
                    return RescueMeConstants.PASSWORD_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                Toast.makeText(context, RescueMeConstants.LOGIN_SUCCESS, Toast
                        .LENGTH_SHORT).show();
                prefs.edit().putBoolean(RescueMeConstants.LOGIN, true).apply();
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
