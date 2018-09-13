package oxhammar.nicklas.run.fragments;

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
import android.support.annotation.NonNull;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import oxhammar.nicklas.run.activities.MainActivity;
import oxhammar.nicklas.run.Constants;
import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

import static android.content.ContentValues.TAG;
import static java.lang.Math.round;


public class CurrentRunFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 3000;
    private static final float MIN_DISTANCE = 1;
    private static final int REQUEST_LOCATION = 1;

    ForegroundService service;
    FinishedRunsFragment finishedRunsfragment;
    FusedLocationProviderClient locationProvider;
    OnFragmentInteractionListener listener;

    boolean bound = false;
    private boolean runStarted;
    boolean gpsAlertIsShowing = false;

    private DBHandler db;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private ArrayList<LatLng> latLngList;

    private Button runButton;
    private TextView speedTextView;
    private TextView timerTextView;
    private TextView distanceTextView;

    private long runTime = 0;
    private long millis;

    //Runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = System.currentTimeMillis() - runTime;

            timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)));

            timerHandler.postDelayed(this, 500);
        }
    };


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
        return inflater.inflate(R.layout.fragment_current_run, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        runButton = getView().findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopRun(view);
            }
        });
        runStarted = false;

        speedTextView = getView().findViewById(R.id.speedTextView);
        timerTextView = getView().findViewById(R.id.timerTextView);
        distanceTextView = getView().findViewById(R.id.distanceTextView);

    }

    @Override
    public void onResume() {
        super.onResume();

        startLocationUpdates();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();
        getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        getContext().stopService(new Intent(getContext(), ForegroundService.class));

        if (bound) {
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkGpsEnabled();
            }
        }
    };


    public void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            setupMap();
        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
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
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            this.googleMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);

        if (googleMap != null) {
            googleMap.moveCamera(cameraUpdate);
            updateCameraBearing(googleMap, location.getBearing());
        }

        if (runStarted) {
            String speedString = String.valueOf(round(location.getSpeed() * 3.6)) + " km/h";
            speedTextView.setText(speedString);
            String distanceString = String.valueOf((double) round(service.getDistanceTravelled() * 10) / 10) + " km";
            distanceTextView.setText(distanceString);
        }

    }


    public void startStopRun(View view) {

        if (!checkGpsEnabled()) {
            return;
        }

        if (!runStarted) {
            // Bind to ForegroundService
            Intent intent = new Intent(getContext(), ForegroundService.class);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

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

            latLngList = new ArrayList<>();
        } else {

            if (bound) {
                latLngList = service.getLatLngList();
            }
            getContext().stopService(new Intent(getContext(), ForegroundService.class));
            getActivity().unbindService(serviceConnection);
            bound = false;

            timerHandler.removeCallbacks(timerRunnable);

            if (latLngList.size() > 2) {
                buildAlertMessageSaveRun();
            } else {
                resetTextViews();
            }
        }

    }

    public void resetTextViews() {

        runStarted = false;
        timerTextView.setText(getResources().getString(R.string.timer_text_view));
        speedTextView.setText(getResources().getString(R.string.speed_text_view));
        distanceTextView.setText(getResources().getString(R.string.distance_text_view));

        runButton.setText(getResources().getString(R.string.start_run_button));
        runButton.setBackground(getResources().getDrawable(R.drawable.start_button));

        speedTextView.setTextColor(getResources().getColor(R.color.colorGray));
        distanceTextView.setTextColor(getResources().getColor(R.color.colorGray));
        timerTextView.setTextColor(getResources().getColor(R.color.colorGray));

    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if (googleMap == null) return;
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
            if (!gpsAlertIsShowing) {
                buildAlertMessageNoGps();
            }
            return false;
        }
        return true;
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundService.ForegroundBinder binder = (ForegroundService.ForegroundBinder) service;
            CurrentRunFragment.this.service = binder.getService();
            bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected: service disconnected!");
            bound = false;
            service = null;
        }
    };


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

                        FinishedRun finishedRun = new FinishedRun(Calendar.getInstance().getTime(), millis, service.getDistanceTravelled(), latLngList);
                        finishedRun.setId(db.addRun());

                        Gson gson = new Gson();
                        String json = gson.toJson(finishedRun);

                        if (db.updateRun(json, finishedRun)) {
                            Toast.makeText(getContext(), getResources().getString(R.string.run_added), Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).addFinishedRun();
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
