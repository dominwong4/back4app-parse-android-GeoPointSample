package testing.gps_location;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button b,upload,grab,grabAsia;
    private TextView t;
    private LocationManager locationManager;
    private LocationListener listener;
    public double longitude=0,latitude=0;
    private ParseObject placeObject,grabLocationObject;
    private List<double[]> fetched_data;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("2SCAirBxkoL0lq9Kim22kDZaZD8871cXGgpJnQCI")
                .clientKey("dfHXibtxLGzifzmZobkdNnxUIAeGp7AvxckKg4Tq")
                .server("https://parseapi.back4app.com/").build()
        );
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

        t = (TextView) findViewById(R.id.textView);
        b = (Button) findViewById(R.id.button);


        fetched_data = new ArrayList<double[]>();
        placeObject = new ParseObject("Monster");
        grabLocationObject = new ParseObject("Monster");
        upload = (Button) findViewById(R.id.upload_location);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("GPS",longitude+" "+latitude);
                ParseGeoPoint point = new ParseGeoPoint(latitude,longitude);
                placeObject.put("Location",point);
                placeObject.saveInBackground();

            }
        });

        grab = (Button)findViewById(R.id.grabLocation);
        grab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(MainActivity.this,"Fetching Nearest Location"
                        ,"Please Wait",true);
                ParseGeoPoint location = new ParseGeoPoint(latitude,longitude);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Monster");
                query.whereNear("Location",location);
                query.setLimit(8);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        Log.d("GPS",""+objects.size());
                        fetched_data.clear();
                        for(int i= 0 ;i<objects.size();i++){
                            double tempLat = objects.get(i).getParseGeoPoint("Location").getLatitude();
                            double tempLng = objects.get(i).getParseGeoPoint("Location").getLongitude();
                            Log.d("GPS","Lat :"+tempLat+" Lng :"+tempLng);
                            double[] temp = new double[] {tempLat,tempLng};
                            fetched_data.add(temp);
                        }
                        progressDialog.dismiss();
                        startGoogleMap();
                    }
                });
            }
        });
        grabAsia = (Button) findViewById(R.id.grabAsiaLocation);
        grabAsia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(MainActivity.this,"Fetching Asia Location"
                ,"Please Wait",true);
                ParseGeoPoint southwestOfSF = new ParseGeoPoint(69.244773,30.498611);
                ParseGeoPoint northeastOfSF = new ParseGeoPoint(4.971406,148.273765);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Monster");
                query.whereWithinGeoBox("Location", southwestOfSF, northeastOfSF);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        fetched_data.clear();
                        for(int i= 0 ;i<objects.size();i++){
                            double tempLat = objects.get(i).getParseGeoPoint("Location").getLatitude();
                            double tempLng = objects.get(i).getParseGeoPoint("Location").getLongitude();
                            Log.d("GPS","Lat :"+tempLat+" Lng :"+tempLng);
                            double[] temp = new double[] {tempLat,tempLng};
                            fetched_data.add(temp);
                        }
                        progressDialog.dismiss();
                        startGoogleMap();
                    }
                });
            }
        });
        upload.setEnabled(false);
        grab.setEnabled(false);
        grabAsia.setEnabled(false);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                t.setText("Coordinates: \n " + location.getLongitude() + " " + location.getLatitude());
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                upload.setEnabled(true);
                grab.setEnabled(true);
                grabAsia.setEnabled(true);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        configure_button();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                    configure_button();
                break;
            default:
                break;
        }
    }
    void configure_button(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);

            }
        });
    }
    void startGoogleMap(){
        Intent i = new Intent(MainActivity.this,MapsActivity.class);

            i.putExtra("location",(Serializable)fetched_data);

        startActivity(i);
    }
}


