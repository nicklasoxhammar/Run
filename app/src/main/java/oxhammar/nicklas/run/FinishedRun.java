package oxhammar.nicklas.run;

import android.content.res.Resources;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;
import static java.lang.Math.round;

/**
 * Created by Nick on 2018-03-14.
 */

public class FinishedRun {

    Date date;
    Long duration;
    Double distance;
    ArrayList<LatLng> latLngList;

    long id;

    public FinishedRun(Date date, Long duration, Double distance, ArrayList<LatLng> latLngList){

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

        return " - " + String.format(String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)));

    }


    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }

    public String getStringAverageSpeed() {

        double durationInHours = (double) duration / (1000 * 60 * 60);

        double averageSpeed = distance / durationInHours;

        if((int) round(averageSpeed) > 0) {
            return " - " + String.valueOf((int) round(averageSpeed) + " km/h");
        }else{
            return " - 0 km/h";
        }
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){

        return id;
    }

    public String getStringDistanceAndDate(){

        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(date);

        return (String.valueOf((double)round(distance * 10) / 10) + " km   |   " + dateString);
    }

    public String getStringDistance(){
        return " - " + String.valueOf((double)round(distance * 10) / 10) + " km";
    }



}
