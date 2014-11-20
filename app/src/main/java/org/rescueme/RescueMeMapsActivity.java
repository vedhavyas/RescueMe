package org.rescueme;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class RescueMeMapsActivity extends FragmentActivity {

    private GoogleMap googleMap;
    private LatLng latLng = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_me_maps);

        context = getBaseContext();
        String jsonData = getIntent().getStringExtra(RescueMeConstants.JSON_STRING);
        RescueMeUtilClass.writeToLog("Json data : " + jsonData);

        try {
            JSONObject json = new JSONObject(jsonData);
            latLng = new LatLng((Double) json.get("lat"), (Double) json.get("long"));
        } catch (JSONException e) {
            RescueMeUtilClass.writeToLog("JSON exception when converting string to JSON object");
        }


        try {
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (latLng != null) {

                MarkerOptions marker = new MarkerOptions().position(latLng).title("Hello");
                googleMap.addMarker(marker);

                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(12).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
            if (googleMap == null) {
                RescueMeUtilClass.toastAndLog(context, "Unable to create Maps!!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
    }
}
