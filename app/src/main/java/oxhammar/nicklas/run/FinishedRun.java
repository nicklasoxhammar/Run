package oxhammar.nicklas.run;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;

/**
 * Created by Nick on 2018-03-14.
 */

public class FinishedRun {

    private Date date;
    private Long duration;
    private Double distance;
    private ArrayList<LatLng> latLngList;

    private long id;

    public FinishedRun(Date date, Long duration, Double distance, ArrayList<LatLng> latLngList) {

        this.date = date;
        this.duration = duration;
        this.distance = distance;
        this.latLngList = latLngList;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDuration() {

        long millis = duration;

        return " - " + String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));

    }

    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }

    public String getStringAverageSpeed() {

        double durationInHours = (double) duration / (1000 * 60 * 60);

        double averageSpeed = distance / durationInHours;

        if ((int) round(averageSpeed) > 0) {
            return " - " + String.valueOf((int) round(averageSpeed) + " km/h");
        } else {
            return " - 0 km/h";
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {

        return id;
    }

    public String getStringDistanceAndDate() {

        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date);

        return (String.valueOf((double) round(distance * 10) / 10) + " km   |   " + dateString);
    }

    public String getStringDistance() {
        return " - " + String.valueOf((double) round(distance * 10) / 10) + " km";
    }


}
