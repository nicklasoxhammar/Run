package oxhammar.nicklas.run.activities;

import android.content.Intent;
import android.os.Debug;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_finished_run);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DBHandler db = new DBHandler(this);

        Intent intent = getIntent();
        Long runId = (long) 0;

        if (intent.getExtras() != null) {
            runId = intent.getExtras().getLong("runId");
        } else {
            finish();
        }

        String runJson = db.getRun(runId);
        Gson gson = new Gson();
        Type type = new TypeToken<FinishedRun>() {
        }.getType();

        run = gson.fromJson(runJson, type);

        latLngList = run.getLatLngList();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList) {
            builder.include(latLng);
        }

        bounds = builder.build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.finishedRunMap);
        mapFragment.getMapAsync(this);

        setUpTextViews();

    }

    private void setUpTextViews() {
        finishedSpeedTextView = findViewById(R.id.finishedSpeedTextView);
        finishedDurationTextView = findViewById(R.id.finishedDurationTextView);
        finishedDistanceTextView = findViewById(R.id.finishedDistanceTextView);

        String durationText = getString(R.string.duration) + run.getStringDuration();
        finishedDurationTextView.setText(durationText);
        String distanceText = getString(R.string.distance) + run.getStringDistance();
        finishedDistanceTextView.setText(distanceText);
        String speedText = getString(R.string.average_speed) + run.getStringAverageSpeed();
        finishedSpeedTextView.setText(speedText);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        zoomToLocation();
    }

    public void zoomToLocation() {
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                googleMap.animateCamera(cameraUpdate);
            }
        });

        googleMap.addMarker(new MarkerOptions()
                .position(latLngList.get(0))
                .title(getString(R.string.start))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.addMarker(new MarkerOptions()
                .position(latLngList.get(latLngList.size() - 1))
                .title(getString(R.string.end)));

        drawLine();
    }

    public void drawLine() {
        googleMap.addPolyline(new PolylineOptions()
                .addAll(latLngList)
                .width(10)
                .color(this.getResources().getColor(R.color.colorAccent)));

    }


    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent myIntent = new Intent(this, MainActivity.class);
                this.startActivity(myIntent);

                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
