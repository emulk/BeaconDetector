package beacondetector.emulk.it.beacondetector;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, SensorEventListener {

    public final static String EXTRA_MESSAGE = "beacondetector.emulk.it.beacondetector.MESSAGE";
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    final double alpha = 0.8;
    int ax, ay, az, tempAx, tempAy, tempAz;
    String protocol = null;
    private ArrayList<BeaconStructure> results = new ArrayList<BeaconStructure>();
    /*DB*/
    private BeaconDB myDB;
    private ListView genericLV;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Tracker mTracker;
    private String ProtocolName = null;
    private String BeaconName = null;
    private String namespaceId = null;
    private String instanceId = null;
    //iBeacon
    private String BeaconMajor = null;
    private String BeaconMinor = null;
    private String BeaconUUID = null;
    private String distanceString = null;
    private String RSSIString = null;
    private String mTxPower = null;
    private int mTxPowerTMP = 0;
    private String telemetryData = null;
    private String url = null;
    private String urlDescription = null;
    private String myDate = null;
    private double distance = 0;
    private int Rssi = 0;
    private BeaconManager mBeaconManager;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        genericLV = (ListView) findViewById(R.id.listView);

        //************************************ accelerometro **********************************************+
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //******************************* [ [Google Analytics ] ] ************************************
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        //[[Endo Google Analytics

        //************************ adMob chiave pubblica: ca-app-pub-4209540176643828/4024024590 ***************************************************
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4209540176643828/4024024590");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //attivo il bluetooth
        activateBluetooth();

        //creo un istanza del DB
        openDB();

        /**
         * start beacon listener
         */
        startBeaconListener();


        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(this, ShowBeacons.class);

        switch (id) {
            case R.id.eddyston:
                protocol = "eddystone";
                intent.putExtra(EXTRA_MESSAGE, protocol);
                startActivity(intent);
                return true;
            case R.id.ibeacon:
                protocol = "ibeacon";
                intent.putExtra(EXTRA_MESSAGE, protocol);
                startActivity(intent);
                return true;
            case R.id.trilateration:
                intent = new Intent(this, Trilateration.class);
                startActivity(intent);
                return true;
            case R.id.indoor:
                intent = new Intent(this, IndoorLocation.class);
                startActivity(intent);
                return true;
            case R.id.targetedAD:
                intent = new Intent(this, TargetedAD.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }

    /***
     * Attiva il bluetotth se non è attivo
     */
    public void activateBluetooth() {

        //se il bluetooth non è attivo, lo attivo
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), "BlueTooth enable", Toast.LENGTH_LONG).show();
            //Log.d(TAG, "BlueTooth enable");
        }

    }

    /**
     *
     */
    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    /**
     * @param ax
     * @param ay
     * @param az
     */
    public void accelerometrChange(int ax, int ay, int az) {
        tempAx = ax;
        tempAy = ay;
        tempAz = az;

    }

    /**
     * @param beacons
     * @param region
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        results.clear();
        for (Beacon beacon : beacons) {
            myDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                ProtocolName = "Eddystone";
                Identifier namespaceIdTMP = beacon.getId1();

                // Do we have telemetry data?
                if (beacon.getExtraDataFields().size() > 0) {

                    long telemetryVersion = beacon.getExtraDataFields().get(0);
                    long batteryMilliVolts = beacon.getExtraDataFields().get(1);
                    long pduCount = beacon.getExtraDataFields().get(3);
                    long uptime = beacon.getExtraDataFields().get(4);

                    telemetryData = "Yes";

                } else {
                    telemetryData = "No";
                }

                final Identifier instanceIdTMP = beacon.getId2();
                BeaconName = beacon.getBluetoothName();
                namespaceId = namespaceIdTMP.toString().substring(2);
                instanceId = instanceIdTMP.toString().substring(2);
                distance = beacon.getDistance();
                distanceString = distance + "m";
                distanceString = distanceString.substring(0, 4) + " m";
                Rssi = beacon.getRssi();
                RSSIString = Rssi + " dBm";

                mTxPowerTMP = beacon.getTxPower();
                //prendo gli ultimi valori

                if (ax != tempAx || ay != tempAy || az != tempAz) {
                    /*Inserisco una riga nella tabella eddystone, nel db*/
                    myDB.insertRowEddystoneTable(ProtocolName, BeaconName, namespaceId, instanceId, (int) distance, Rssi, mTxPowerTMP, myDate);
                    accelerometrChange(ax, ay, az);
                }

                BeaconStructure beaconView = new BeaconStructure();
                beaconView.setNamespaceLayout("NameSpace");
                beaconView.setInstanceLayout("Instance");

                beaconView.setBeaconProtocol(ProtocolName);
                beaconView.setBeaconName(BeaconName);
                beaconView.setBeaconID(namespaceId);
                beaconView.setInstance(instanceId);
                beaconView.setDistance(distanceString);
                beaconView.setRssi(RSSIString);
                beaconView.setTxPower(mTxPower);
                beaconView.setLastAliveOn(myDate);
                results.add(beaconView);

                mTxPower = mTxPowerTMP + " dBm";

/*
                Log.d("RangingActivity", "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");*/


            } else if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                ProtocolName = "Eddystone URL";
                Rssi = beacon.getRssi();
                RSSIString = Rssi + " dBm";

                mTxPowerTMP = beacon.getTxPower();
                mTxPower = mTxPowerTMP + " dBm";
                url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                urlDescription = url;
                BeaconName = beacon.getBluetoothName();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                /* Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");*/


                BeaconStructure beaconView = new BeaconStructure();
                beaconView.setBeaconProtocol(ProtocolName);
                beaconView.setUrlId(url);
                beaconView.setDistance(distanceString);
                beaconView.setRssi(RSSIString);
                beaconView.setTxPower(mTxPower);
                beaconView.setLastAliveOn(myDate);
                results.add(beaconView);

                // myDB.insertRowUrlTable(ProtocolName, BeaconName, url, myDate);


            } else if (beacon.getServiceUuid() != 0 && beacon.getBeaconTypeCode() == 533) {
                ProtocolName = "iBeacon";
                //bluetoothName emulkBeacon bluetoothAdress DE:F0:70:9E:E9:B8 datafields []
                // ExtraDataFiels[] id1 ebefd083-70a2-47c8-9837-e7b5634df524 id2 10 id3 1 manufacter 76
                //battery level beacon.getDataFields().get(0)

                BeaconName = beacon.getBluetoothName();
                BeaconUUID = (beacon.getId1() + "").toUpperCase();
                BeaconMajor = beacon.getId2() + "";
                BeaconMinor = beacon.getId3() + "";
                BeaconMajor = BeaconMajor + " / " + BeaconMinor;
                distance = beacon.getDistance();
                distanceString = distance + "m";
                distanceString = distanceString.substring(0, 4) + " m";
                Rssi = beacon.getRssi();
                RSSIString = Rssi + " dBm";

                mTxPowerTMP = beacon.getTxPower();
                mTxPower = mTxPowerTMP + " dBm";
                telemetryData = "No";

                BeaconStructure beaconView = new BeaconStructure();
                beaconView.setNamespaceLayout("UID");
                beaconView.setInstanceLayout("Major/Minor");
                beaconView.setBeaconProtocol(ProtocolName);
                beaconView.setBeaconName(BeaconName);
                beaconView.setBeaconID(BeaconUUID);
                beaconView.setInstance(BeaconMajor);
                beaconView.setDistance(distanceString);
                beaconView.setRssi(RSSIString);
                beaconView.setTxPower(mTxPower);
                beaconView.setLastAliveOn(myDate);
                results.add(beaconView);


                if (ax != tempAx || ay != tempAy || az != tempAz) {
                    myDB.insertRowiBeaconTable(ProtocolName, BeaconName, BeaconUUID, BeaconMajor, (int) distance, Rssi, mTxPowerTMP, myDate);
                    //myDB.insertRowEddystoneTable(ProtocolName, BeaconName, namespaceId, instanceId, (int) distance, Rssi, mTxPowerTMP, myDate);
                    //Log.d(TAG, "Inserimento nel table iBeacon "+ax+" "+ay+" "+az);
                    accelerometrChange(ax, ay, az);
                }
            }
        }

        //visualizzo a video tutti i beacon che ho visto
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    genericLV.setAdapter(new BeaconAdapter(MainActivity.this, results));
                    genericLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Intent i = new Intent(view.getContext(), ShowBeacons.class);
                            BeaconStructure protocolName = (BeaconStructure) genericLV.getItemAtPosition(position);
                            protocol = protocolName.getBeaconProtocol();
                            i.putExtra(EXTRA_MESSAGE, protocol);
                            startActivity(i);
                        }
                    });


                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }

            }
        });
    }

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://beacondetector.emulk.it.beacondetector/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(false);
        }
    }

    /**
     *
     */
    public void startBeaconListener() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        // Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v"));
        //Detecte iBeacon frame
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        mBeaconManager.bind(this);
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        activateBluetooth();

        startBeaconListener();

        String name = "MainActivity";
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(false);
        }

        //accelerometr
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     *
     */
    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://beacondetector.emulk.it.beacondetector/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        mBeaconManager.unbind(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //qualcosa alla distruzione del gioco
        mBeaconManager.unbind(this);
        closeDB();
    }

    // apro il database
    private void openDB() {
        myDB = new BeaconDB(this);
        myDB.open();
    }

    private void closeDB() {
        myDB.close();

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        double gravity[] = new double[3];

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            double tax = event.values[0] - gravity[0];
            double tay = event.values[1] - gravity[1];
            double taz = event.values[2] - gravity[2];

            ax = (int) tax;
            ay = (int) tay;
            az = (int) taz;
            // Log.d(TAG, "Accelerometr " + ax + " " + ay + " " + az);
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
