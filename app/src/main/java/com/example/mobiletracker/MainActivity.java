package com.example.mobiletracker;

import static java.lang.Math.log10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobiletracker.model.Sample;
import com.example.mobiletracker.retrofit.RetrofitService;
import com.example.mobiletracker.retrofit.SampleApi;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    // Widgets
    TextView dbMeter;
    TextView longitude;
    TextView latitude;

    Button playButton;

    Chronometer timer;

    // Recording
    public static int MICROPHONE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;

    // Location
    LocationManager locationManager;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERMISSIONS_ALL = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise widgets
        timer = findViewById(R.id.chronometer);
        dbMeter = findViewById(R.id.decibelMeter);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        playButton = findViewById(R.id.playButton);

        // Check if mic is present
        if (isMicrophonePresent()) {
            getMicrophonePermission();
        }

        // Location

        // Request the location with LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(PERMISSIONS, PERMISSIONS_ALL);

        // From Location Test
        requestLocation();
    }

    // Get latest file

    private File getLatestFileFromDir(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }




    // Recording function. Press button to start recording and stops automatically after X amount of time.
    public void onButtonRecordPressed(View v) {

        /* Get Location (Long and Lat) */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double result1 = location.getLongitude();
        double result2 = location.getLatitude();

        result1 = result1*100;
        result1 = (double)((int) result1);
        result1 = result1 /100;

        result2 = result2*100;
        result2 = (double)((int) result2);
        result2 = result2 /100;

        double lon = result1;
        double lat = result2;

        try {
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioChannels(2);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.prepare();
            mediaRecorder.start();
            double soundlevel = mediaRecorder.getMaxAmplitude();
            System.out.println("Sound Level: " + soundlevel);

            Toast.makeText(this, "Recording has started", Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        timer.stop();

                        double processedSoundLevel = mediaRecorder.getMaxAmplitude();
                        System.out.println("Sound Level: " + processedSoundLevel);

                        mediaRecorder.stop();
                        mediaRecorder.release();

                        /* Get File Name */
                        String directoryPath = "storage/emulated/0/Android/data/com.example.mobiletracker/files/Music/";
                        File latestFilePath = getLatestFileFromDir(directoryPath);
                        String fileName = latestFilePath.getName();
                        System.out.println("Latest File: " + fileName);

                        dbMeter.setText(Double.toString(processedSoundLevel));

                        // Add sample to pool
                        String rmsAverage = Double.toString(processedSoundLevel);

                        // Making the post request with the Retrofit HTTP client
                        RetrofitService retrofitService = new RetrofitService();
                        SampleApi sampleApi = retrofitService.getRetrofit().create(SampleApi.class);

                        Sample sample = new Sample();

                        sample.setSample(fileName);
                        sample.setLongitude(lon);
                        sample.setLatitude(lat);
                        sample.setDb(rmsAverage);

                        sampleApi.postSample(sample)
                                .enqueue(new Callback<Sample>() {
                                    @Override
                                    public void onResponse(Call<Sample> call, Response<Sample> response) {
                                        Toast.makeText(MainActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<Sample> call, Throwable t) {
                                        Toast.makeText(MainActivity.this, "Post Failed", Toast.LENGTH_SHORT).show();
                                        System.out.println("Call: " + call);
                                        System.out.println("T: " + t);
                                    }
                                });
                    } catch (Exception e) {

                    }
                }
            }, 10001);

        } catch (Exception e) {

        }
    }

    // Checks for mic permissions and whether or not the mic is present

    private boolean isMicrophonePresent() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    // Choosing a path to save the sample to. (Local storage)

    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, new Date().toString() + ".mp3");
        return file.getPath();
    }


    // Request location permissions. Get Longitude and Latitude. Using the LocationManager Class.

    @Override
    public void onLocationChanged(@NotNull Location location) {
        Log.d("My Log", "Got Location: " + location.getLatitude() + ", " + location.getLongitude());

        locationManager.removeUpdates(this);

        double result1 = location.getLongitude();
        double result2 = location.getLatitude();

        result1 = result1*100;
        result1 = (double)((int) result1);
        result1 = result1 /100;

        double lon = result1;

        result2 = result2*100;
        result2 = (double)((int) result2);
        result2 = result2 /100;

        double lat = result2;

        longitude.setText(Double.toString(lon));
        latitude.setText(Double.toString(lat));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Request Location Now
            requestLocation();
        }
    }

    public void requestLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000, this);
            }
        }
    }
}

