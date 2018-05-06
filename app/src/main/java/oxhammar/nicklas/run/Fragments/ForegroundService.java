package oxhammar.nicklas.run.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import oxhammar.nicklas.run.Activities.MainActivity;
import oxhammar.nicklas.run.Constants;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

import static android.content.ContentValues.TAG;
import static java.lang.Math.log;
import static java.lang.Math.round;

public class ForegroundService extends Service implements LocationListener {

    LocationManager locationManager;

    private final IBinder mBinder = new ForegroundBinder();

    ArrayList<Location> locations;

    double distanceTravelled = 0;

    private static final long MIN_TIME = 3000;
    private static final float MIN_DISTANCE = 5;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)){

            locations = new ArrayList<Location>();

            buildNotification();

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        } /*else if (intent.getAction().equals(
            Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.d(TAG, "onStartCommand: foreground stopped?");
        stopForeground(true);
        stopSelf();
    }*/

        return START_STICKY;
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager){
        String name = getResources().getString(R.string.app_name);
        String description = "running";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(name, name, importance);
        mChannel.setDescription(description);
        notificationManager.createNotificationChannel(mChannel);
    }

    private void buildNotification(){

        NotificationManager mNotifyManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(mNotifyManager);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.LOCATION_ACTION)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Run");
                builder//.setContentTitle("Run")
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.stop_looking))
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .setContentIntent(pendingIntent)
                        .setColor(getResources().getColor(R.color.colorAccent))
                .setOngoing(true);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location.hasAccuracy()) {

            locations.add(location);

            if (locations.size() > 1) {

                distanceTravelled = distanceTravelled + ((locations.get(locations.size() - 2).distanceTo(locations.get(locations.size() - 1))) / 1000);
            }
        }

        //sendToFragment(location);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    public class ForegroundBinder extends Binder {
        ForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForegroundService.this;
        }
    }

   /* public void sendToFragment(Location location){
        Intent intent = new Intent("location-info");

        Gson gson = new Gson();
        String json = gson.toJson(location);

        intent.putExtra("locationJson", json);

        sendBroadcast(intent);
    }*/

    public ArrayList<LatLng> getLatLngList(){
        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();

        for (Location l : locations){
            latLngList.add(new LatLng(l.getLatitude(), l.getLongitude()));
        }
        return latLngList;
    }

    public Double getDistanceTravelled(){
        return distanceTravelled;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
    }
}
