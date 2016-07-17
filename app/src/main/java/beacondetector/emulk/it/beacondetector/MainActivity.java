package beacondetector.emulk.it.beacondetector;

import android.bluetooth.BluetoothAdapter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, SensorEventListener {
    private static final String TAG = "MainActivity";
    final double alpha = 0.8;
    ArrayList<BeaconStructure> results = new ArrayList<BeaconStructure>();
    double ax, ay, az;
    /*DB*/
    BeaconDB myDB;
    Cursor cursorlast;
    TextView urlLink;
    private ListView lv1;
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


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        lv1 = (ListView) findViewById(R.id.listView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //[[Google Analytics ]]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        //[[Endo Google Analytics

        //ca-app-pub-4209540176643828/4024024590
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4209540176643828/4024024590");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //creo un istanza del DB
        openDB();


        //se il bluetooth non è attivo, lo attivo
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), "BlueTooth enable", Toast.LENGTH_LONG).show();
            Log.d(TAG, "BlueTooth enable");
        }


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

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.nameBeacon).setOnTouchListener(mDelayHideTouchListener);


        //((TextView)MainActivity.this.findViewById(R.id.beaconDistance)).setText(distance);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        //listview example
        // definisco un array di stringhe
        /*
        String[] nameproducts = new String[] { "Product1", "Product2", "Product3" };

        // definisco un ArrayList
        final ArrayList <String> listp = new ArrayList<String>();
        for (int i = 0; i < nameproducts.length; ++i) {
            listp.add(nameproducts[i]);
        }
        // recupero la lista dal layout
        final ListView mylist = (ListView) findViewById(R.id.listView);

        // creo e istruisco l'adattatore
        final ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listp);

        // inietto i dati
        mylist.setAdapter(adapter);
        */

        //ArrayList<BeaconStructure> beaconResults = GetBeaconResults() ;


    }


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

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        results.clear();
        for (Beacon beacon : beacons) {
            myDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

            Log.d("Beacon Service Uuid", beacon.getServiceUuid() + " uuid");
            Log.d("Beacon Type Code", beacon.getBeaconTypeCode() + " type");
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

                    Log.d(TAG, "Telemetry Data");

                    Log.d(TAG, "The above beacon is sending telemetry version " + telemetryVersion +
                            ", has been up for : " + uptime + " seconds" +
                            ", has a battery level of " + batteryMilliVolts + " mV" +
                            ", and has transmitted " + pduCount + " advertisements.");
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

                cursorlast = myDB.LastQueryEddystone();
                if (cursorlast.moveToLast()) {
                    long id = cursorlast.getLong(BeaconDB.Col_ROWID);
                    String namespaceDB = cursorlast.getString(BeaconDB.Col_NameSpace);
                    int distanceDB = cursorlast.getInt(BeaconDB.Col_Distance);
                    Log.d(TAG, id + " " + namespaceDB + " " + distanceDB);
                }


                Log.d(TAG, "Insert row in DB");
                /*Inserisco una riga nella tabella eddystone*/
                //myDB.insertRowEddystoneTable(ProtocolName, BeaconName, namespaceId, instanceId, (int) distance, Rssi, mTxPowerTMP, myDate);
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

                Log.d(TAG, RSSIString);

                Log.d("RangingActivity", "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");


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
                Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");


                BeaconStructure beaconView = new BeaconStructure();
                beaconView.setBeaconProtocol(ProtocolName);
                beaconView.setUrlId(url);
                beaconView.setDistance(distanceString);
                beaconView.setRssi(RSSIString);
                beaconView.setTxPower(mTxPower);
                beaconView.setLastAliveOn(myDate);
                results.add(beaconView);

                // myDB.insertRowUrlTable(ProtocolName, BeaconName, url, myDate);

                /*
                urlLink = (TextView) findViewById(R.id.urlId);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            ((TextView) MainActivity.this.findViewById(R.id.AppName)).setText("Eddystone URL");
                            ((TextView) MainActivity.this.findViewById(R.id.urlId)).setText(urlDescription);
                            ((TextView) MainActivity.this.findViewById(R.id.aliveId)).setText(myDate);
                            urlLink.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(urlIntent);
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });*/

            } else if (beacon.getServiceUuid() != 0 && beacon.getBeaconTypeCode() == 533) {
                ProtocolName = "iBeacon";
                //bluetoothName emulkBeacon bluetoothAdress DE:F0:70:9E:E9:B8 datafields []
                // ExtraDataFiels[] id1 ebefd083-70a2-47c8-9837-e7b5634df524 id2 10 id3 1 manufacter 76
                //battery level beacon.getDataFields().get(0)
                Log.d(TAG, "Ho appena visto un iBeacon");
               /* Log.d(TAG, "bluetoothName " + beacon.getBluetoothName() + " bluetoothAdress " + beacon.getBluetoothAddress()
                        + " datafields " + beacon.getDataFields() + " ExtraDataFiels" + beacon.getExtraDataFields() + " id1 " + beacon.getId1()
                        + " id2 " + beacon.getId2() + " id3 " + beacon.getId3() + " manufacter " + beacon.getManufacturer());*/

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


                //myDB.insertRowiBeaconTable(ProtocolName, BeaconName, BeaconUUID, BeaconMajor, BeaconMinor, (int) distance, Rssi, mTxPowerTMP, myDate);

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv1.setAdapter(new BeaconAdapter(MainActivity.this, results));
                    }
                });

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            ((TextView) MainActivity.this.findViewById(R.id.AppName)).setText("iBeacon");
                            ((TextView) MainActivity.this.findViewById(R.id.beaconLayout)).setText("Beacon Name");
                            ((TextView) MainActivity.this.findViewById(R.id.namespaceLayout)).setText("UUID");
                            ((TextView) MainActivity.this.findViewById(R.id.instanceLayout)).setText("Major/Minor");
                            (MainActivity.this.findViewById(R.id.urlLayout)).setVisibility(View.INVISIBLE);


                            ((TextView) MainActivity.this.findViewById(R.id.nameBeacon)).setText(BeaconName);
                            ((TextView) MainActivity.this.findViewById(R.id.namespaceID)).setText(BeaconUUID);
                            ((TextView) MainActivity.this.findViewById(R.id.instanceID)).setText(BeaconMajor);
                            ((TextView) MainActivity.this.findViewById(R.id.beaconDistance)).setText(distanceString);
                            ((TextView) MainActivity.this.findViewById(R.id.rssiView)).setText(RSSIString);
                            ((TextView) MainActivity.this.findViewById(R.id.TxPower)).setText(mTxPower);
                            ((TextView) MainActivity.this.findViewById(R.id.telemetryData)).setText(telemetryData);
                            ((TextView) MainActivity.this.findViewById(R.id.aliveId)).setText(myDate);

                            (MainActivity.this.findViewById(R.id.urlId)).setVisibility(View.INVISIBLE);


                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }


                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            //se non ho le caratteristiche di eddystone/iBeacon/EddystoneURL setto tutti i campi invisibili
                            ((TextView) MainActivity.this.findViewById(R.id.beaconLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.namespaceLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.instanceLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.distanceLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.rssiLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.txPowerLayout)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.telemetryLayout)).setVisibility(View.INVISIBLE);


                            ((TextView) MainActivity.this.findViewById(R.id.nameBeacon)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.namespaceID)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.instanceID)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.beaconDistance)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.rssiView)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.TxPower)).setVisibility(View.INVISIBLE);
                            ((TextView) MainActivity.this.findViewById(R.id.telemetryData)).setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });*/

            }

        }

        //visualizzo a video tutti i beacon che ho visto
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    lv1.setAdapter(new BeaconAdapter(MainActivity.this, results));
                    lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            /*Toast.makeText(getApplicationContext(),
                                    "Click ListItem Number " + position, Toast.LENGTH_LONG)
                                    .show();*/
                        }
                    });


                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }


            }
        });
    }


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

    @Override
    public void onResume() {
        super.onResume();

        String name = "MainActivity";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(false);
        }

        //accelerometr
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(true);
        }
        //accelerometr
        mSensorManager.unregisterListener(this);
    }

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

    public void onSensorChanged(SensorEvent event) {
        double gravity[] = new double[3];


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            ax = event.values[0] - gravity[0];
            ay = event.values[1] - gravity[1];
            az = event.values[2] - gravity[2];

            Log.d(TAG, "Accelerometr " + ax + " " + ay + " " + az);
        }
    }


}
