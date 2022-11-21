package com.example.mobiletracker;

import org.junit.Test;

import static org.junit.Assert.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.mobiletracker.model.Sample;
import com.example.mobiletracker.retrofit.RetrofitService;
import com.example.mobiletracker.retrofit.SampleApi;

import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

//    LocationManager locationManager;
//
//    @Test
//    public void buttonClick(View v) {
//
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        String lon = Double.toString(location.getLongitude());
//        String lat = Double.toString(location.getLatitude());
//
//        // Making the post request
//        RetrofitService retrofitService = new RetrofitService();
//        SampleApi sampleApi = retrofitService.getRetrofit().create(SampleApi.class);
//
//        Sample sample = new Sample();
//
//        sample.setSample("Sample Test 4");
//        sample.setLongitude(lon);
//        sample.setLatitude(lat);
//        sample.setDb("2");
//
//        sampleApi.save(sample)
//                .enqueue(new Callback<Sample>() {
//                    @Override
//                    public void onResponse(Call<Sample> call, Response<Sample> response) {
//                        Toast.makeText(MainActivity.this, "Post Success", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(Call<Sample> call, Throwable t) {
//                        Toast.makeText(MainActivity.this, "Post Failed", Toast.LENGTH_SHORT).show();
//                        Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, "Error", t);
//                    }
//                });
//    }
}