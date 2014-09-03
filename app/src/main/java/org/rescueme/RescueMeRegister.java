package org.rescueme;



import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class RescueMeRegister extends Fragment {


    private EditText name;
    private EditText email;
    private EditText password;
    private EditText phoneNumber;
    private Button registerBtn;

    public RescueMeRegister() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_register, container, false);
        name = (EditText)rootView.findViewById(R.id.name);
        email = (EditText)rootView.findViewById(R.id.email);
        password = (EditText)rootView.findViewById(R.id.password);
        phoneNumber = (EditText)rootView.findViewById(R.id.phoneNumber);
        registerBtn = (Button)rootView.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ((RescueMe)getActivity()).setTitle(RescueMeConstants.REGISTER);

        return rootView;
    }

    public class RegisterTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }




}
