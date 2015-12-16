package wso2.org.beconlogpublisher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

//=================================================================================

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


//====================================================================================

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Queue;

public class BeaconReceiverActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener ,LocationListener{
    protected static final String TAG2 = "MonitoringActivity";
    private BeaconManager beaconManager;
    private Queue<BeaconDataRecord> queue;

    //===================minudika==============================
    private static final String TAG=BeaconReceiverActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation ;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates =false;
     LocationManager locationManager;

    private static int UPDATE_INTERVAL = 10000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;

    TextView txtLongitude;
    TextView txtLatitude;
    Button btnUpdateLocation;

    Context context=this;

    //=========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_becon_receiver);

        txtLongitude=(TextView)findViewById(R.id.textView_Longitude);
        txtLatitude=(TextView)findViewById(R.id.textView_Latitude);
        btnUpdateLocation=(Button)findViewById(R.id.btnUpdateLocation);

        locationManager=(LocationManager)context.getSystemService(LOCATION_SERVICE);
        checkPlayServices();
        buildGoogleApiClient();


        createLocationRequest();

        mRequestingLocationUpdates=true;

        btnUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLocationRequest();
                showLocation();
            }
        });


       /* int cnt=0,cnt2=0;
        while(cnt<50) {
            long startTime = System.currentTimeMillis();
            while (true) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > 2000) {
                    break;
                }
            }
            if(cnt%2==0)
                showLocation();
            else {
                txtLatitude.setText("****");
                txtLongitude.setText("****");
            }
            cnt++;
            createLocationRequest();
        }*/

    }

    private void showLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
       // mLastLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(mLastLocation!=null){

            Double latitude = mLastLocation.getLatitude();
            Double longitude = mLastLocation.getLongitude();
            txtLatitude.setText(Double.toString(latitude));
            txtLongitude.setText(Double.toString(longitude));
            Log.d("location :","Longitute : "+longitude+" , Latitude :"+latitude);
        }
        else{
            Toast.makeText(getApplicationContext(),"Last Location is null",Toast.LENGTH_LONG).show();
            Log.d("ERROR :","ERROR");
        }
    }


    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        showLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed : ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();;
        checkPlayServices();
    }

    private void togglePeriodicLocationUpdates(){
        if(!mRequestingLocationUpdates){
            startLocationUpdates();
            Log.d(TAG,"Periodic locaion updates started!");
        }
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    protected void startLocationUpdates(){
        Intent alarm = new Intent(BeaconReceiverActivity.this, BeaconReceiverActivity.class);
        PendingIntent recurringAlarm =
                PendingIntent.getBroadcast(context,
                        1,
                        alarm,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, recurringAlarm);
    }


    @Override
    public void onLocationChanged(Location location) {

        mLastLocation=location;
        Toast.makeText(getApplicationContext(),"Location Chanhed.",Toast.LENGTH_LONG).show();
        showLocation();
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
