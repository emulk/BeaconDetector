package beacondetector.emulk.it.beacondetector;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Created by emulk on 29/10/16.
 */

public class IndoorLocation extends AppCompatActivity implements BeaconConsumer, RangeNotifier {
    private static final String TAG = "Indoor location";

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
    private String myDate = null;
    private double distance = 0;
    private int Rssi = 0;

    private float temperature = 0;

    private boolean firstTime = true;
    private boolean firstTimeTemperatura = true;

    private BeaconManager mBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.indoor_location);
        /**
         * start beacon listener
         */
        startBeaconListener();


        SensorManager mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor AmbientTemperatureSensor
                = mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (AmbientTemperatureSensor != null) {
            mySensorManager.registerListener(
                    AmbientTemperatureSensorListener,
                    AmbientTemperatureSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private final SensorEventListener AmbientTemperatureSensorListener
            = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                temperature = event.values[0];
            }
        }

    };

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
     * @param beacons
     * @param region
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceIdTMP = beacon.getId1();
                namespaceId = namespaceIdTMP.toString().substring(2);
                if (namespaceId.equalsIgnoreCase("8b0ca750095477cb3e77")) {
                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audio.getRingerMode() != 0) {
                        audio.setRingerMode(0);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(IndoorLocation.this);
                                    builder.setTitle("Camera da letto");
                                    builder.setMessage("Modalità silenzioso");
                                    builder.setPositiveButton(android.R.string.ok, null);
                                    builder.show();
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }
            } else if (beacon.getServiceUuid() != 0 && beacon.getBeaconTypeCode() == 533) {
                BeaconName = beacon.getBluetoothName();
                BeaconUUID = (beacon.getId1() + "").toUpperCase();
                BeaconMajor = beacon.getId2() + "";
                BeaconMinor = beacon.getId3() + "";
                if (BeaconUUID.equalsIgnoreCase("953CA831-E1B0-4E4F-A3B0-FFD21C04C7C9")) {
                    if (firstTime) {
                        firstTime = false;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(IndoorLocation.this);
                                    builder.setTitle("Porta di ingreso");
                                    builder.setMessage("Ricordati la spazzatura");
                                    builder.setPositiveButton(android.R.string.ok, null);
                                    builder.show();
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }

            }

            if (temperature > 20) {
                if (firstTimeTemperatura) {
                    firstTimeTemperatura = false;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(IndoorLocation.this);
                                builder.setTitle("La temperatura è di ");
                                builder.setMessage(temperature + " Ricordati la lista della spesa");
                                builder.setPositiveButton(android.R.string.ok, null);
                                builder.show();
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

