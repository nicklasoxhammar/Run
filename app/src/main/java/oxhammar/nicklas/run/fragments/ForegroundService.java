package oxhammar.nicklas.run.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import oxhammar.nicklas.run.activities.MainActivity;
import oxhammar.nicklas.run.Constants;
import oxhammar.nicklas.run.R;


public class ForegroundService extends Service implements LocationListener {

    LocationManager locationManager;

    private final IBinder binder = new ForegroundBinder();

    ArrayList<Location> locations;

    double distanceTravelled = 0;

    private static final long MIN_TIME = 3000;
    private static final float MIN_DISTANCE = 5;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().matches(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            locations = new ArrayList<>();

            buildNotification();

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            }
        }
        return START_STICKY;
    }


    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        String name = getResources().getString(R.string.app_name);
        String description = "running";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(name, name, importance);
        mChannel.setDescription(description);
        notificationManager.createNotificationChannel(mChannel);
    }

    private void buildNotification() {
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
        builder
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
    }


    class ForegroundBinder extends Binder {
        ForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForegroundService.this;
        }
    }


    public ArrayList<LatLng> getLatLngList() {
        ArrayList<LatLng> latLngList = new ArrayList<>();

        for (Location l : locations) {
            latLngList.add(new LatLng(l.getLatitude(), l.getLongitude()));
        }
        return latLngList;
    }

    public Double getDistanceTravelled() {
        return distanceTravelled;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
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
}
