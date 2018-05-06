package oxhammar.nicklas.run.Activities;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

public class DisplayFinishedRunActivity extends AppCompatActivity implements OnMapReadyCallback {

    FinishedRun run;

    ArrayList<LatLng> latLngList;

    TextView finishedSpeedTextView;
    TextView finishedDurationTextView;
    TextView finishedDistanceTextView;



    LatLngBounds bounds;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_finished_run);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        Long runId = intent.getExtras().getLong("runId");

        DBHandler db = new DBHandler(this);

        String runJson = db.getRun(runId);
        Gson gson = new Gson();
        Type type = new TypeToken<FinishedRun>() {
        }.getType();

        run = gson.fromJson(runJson, type);

        latLngList = run.getLatLngList();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList){
            builder.include(latLng);
        }

        bounds = builder.build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.finishedRunMap);
        mapFragment.getMapAsync(this);

        finishedSpeedTextView = (TextView) findViewById(R.id.finishedSpeedTextView);
        finishedDurationTextView = (TextView) findViewById(R.id.finishedDurationTextView);
        finishedDistanceTextView = (TextView) findViewById(R.id.finishedDistanceTextView);

        finishedDurationTextView.setText(getString(R.string.duration) + run.getStringDuration());
        finishedDistanceTextView.setText(getString(R.string.distance) + run.getStringDistance());
        finishedSpeedTextView.setText(getString(R.string.average_speed) + run.getStringAverageSpeed());


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        zoomToLocation();
    }

    public void zoomToLocation(){

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                mMap.animateCamera(cameraUpdate);

            }
        });

        mMap.addMarker(new MarkerOptions()
            .position(latLngList.get(0))
            .title(getString(R.string.start))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.addMarker(new MarkerOptions()
                .position(latLngList.get(latLngList.size()-1))
                .title(getString(R.string.end)));

        drawLine();
    }

    public void drawLine(){
        Polyline line = mMap.addPolyline(new PolylineOptions()
        .addAll(latLngList)
        .width(10)
        .color(this.getResources().getColor(R.color.colorAccent)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }


    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
