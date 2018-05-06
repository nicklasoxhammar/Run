package oxhammar.nicklas.run.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import oxhammar.nicklas.run.Activities.MainActivity;
import oxhammar.nicklas.run.Constants;
import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

import static android.content.ContentValues.TAG;
import static java.lang.Math.log;
import static java.lang.Math.round;


public class CurrentRunFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    ForegroundService mService;
    boolean mBound = false;

    boolean gpsAlertIsShowing = false;

    private DBHandler db;

    private static final int REQUEST_LOCATION = 1;

    private FusedLocationProviderClient locationProvider;
    LocationManager locationManager;

    FinishedRunsFragment finishedRunsfragment;

    ArrayList<LatLng> latLngList;

    boolean runStarted;

    Button runButton;

    TextView speedTextView;
    TextView timerTextView;
    TextView distanceTextView;

    private static final long MIN_TIME = 3000;
    private static final float MIN_DISTANCE = 1;

    private GoogleMap mMap;

    long runTime = 0;
    long millis;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = System.currentTimeMillis() - runTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };



    private OnFragmentInteractionListener mListener;

    public CurrentRunFragment() {
        // Required empty public constructor
    }


    public static CurrentRunFragment newInstance() {
        CurrentRunFragment fragment = new CurrentRunFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DBHandler(getContext());

        locationProvider = LocationServices.getFusedLocationProviderClient(getContext());

        finishedRunsfragment = (FinishedRunsFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.finishedRunFragment);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_run, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        runButton = (Button) getView().findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopRun(view);
            }
        });
        runStarted = false;


        speedTextView = (TextView) getView().findViewById(R.id.speedTextView);
        timerTextView = (TextView) getView().findViewById(R.id.timerTextView);
        distanceTextView = (TextView) getView().findViewById(R.id.distanceTextView);

    }

    private final BroadcastReceiver mYourBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkGpsEnabled();
            }
        }
    };




    public void setupMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onResume() {
        super.onResume();

        startLocationUpdates();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        //intentFilter.addAction("location-info");
        getContext().registerReceiver(mYourBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();
        getContext().unregisterReceiver(mYourBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        getContext().stopService(new Intent(getContext(), ForegroundService.class));

        if(mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }

        super.onDestroy();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            setupMap();
        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResult.length == 1 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                getActivity().finish();
                System.exit(0);
            }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
    }


     @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);

        if(mMap != null) {
            mMap.moveCamera(cameraUpdate);
            updateCameraBearing(mMap, location.getBearing());
        }

        if(runStarted == true) {
            speedTextView.setText(String.valueOf(round(location.getSpeed() * 3.6)) + " km/h");

            distanceTextView.setText(String.valueOf((double)round(mService.getDistanceTravelled() * 10) / 10) + " km");
            }


        //locationManager.removeUpdates(this);
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    public void startStopRun(View view){

        if(!checkGpsEnabled()){
            return;
        }

        if (runStarted == false){

            // Bind to ForegroundService
            Intent intent = new Intent(getContext(), ForegroundService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            Intent startIntent = new Intent(getContext(), ForegroundService.class);
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            getActivity().startService(startIntent);

            runStarted = true;

            runButton.setText(getResources().getString(R.string.stop_run_button));
            runButton.setBackground(getResources().getDrawable(R.drawable.stop_button));
            runTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);

            speedTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            distanceTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            timerTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

            latLngList = new ArrayList<LatLng>();
        }else{

            if (mBound){
                latLngList = mService.getLatLngList();
            }

            /*Intent startIntent = new Intent(getContext(), ForegroundService.class);
            startIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            getActivity().startService(startIntent);*/

            getContext().stopService(new Intent(getContext(), ForegroundService.class));
            getActivity().unbindService(mConnection);
            mBound = false;

            timerHandler.removeCallbacks(timerRunnable);


            if(latLngList.size() > 2) {
                buildAlertMessageSaveRun();
            }else{
                resetTextViews();
            }
        }

    }

    public void resetTextViews(){

        runStarted = false;
        timerTextView.setText("0:00");
        speedTextView.setText("0 km/h");
        distanceTextView.setText("0.0 km");

        runButton.setText(getResources().getString(R.string.start_run_button));
        runButton.setBackground(getResources().getDrawable(R.drawable.start_button));

        speedTextView.setTextColor(getResources().getColor(R.color.colorGray));
        distanceTextView.setTextColor(getResources().getColor(R.color.colorGray));
        timerTextView.setTextColor(getResources().getColor(R.color.colorGray));


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }


    public boolean checkGpsEnabled() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if(!gpsAlertIsShowing) {
                buildAlertMessageNoGps();
            }
            return false;
        }

        return true;
    }

    private void buildAlertMessageNoGps() {

        gpsAlertIsShowing = true;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.cancel();
                        gpsAlertIsShowing = false;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        gpsAlertIsShowing = false;
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageSaveRun() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.save_run_alert))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        FinishedRun finishedRun = new FinishedRun(Calendar.getInstance().getTime(), millis, mService.getDistanceTravelled(), latLngList);
                        finishedRun.setId(db.addRun("getidstring"));

                        Gson gson = new Gson();
                        String json = gson.toJson(finishedRun);

                        if(db.updateRun(json, finishedRun)){
                            Toast.makeText(getContext(), getResources().getString(R.string.run_added), Toast.LENGTH_SHORT).show();
                            ((MainActivity)getActivity()).addFinishedRun();
                        }


                        resetTextViews();

                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        resetTextViews();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundService.ForegroundBinder binder = (ForegroundService.ForegroundBinder) service;
            mService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected: service disconnected!");
            mBound = false;
            mService = null;
        }
    };

}
