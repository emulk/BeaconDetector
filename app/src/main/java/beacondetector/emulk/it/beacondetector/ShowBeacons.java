package beacondetector.emulk.it.beacondetector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 23/07/16.
 */
public class ShowBeacons extends Activity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ShowBeacons";

    String protocol = "eddystone";
    Spinner spinner;
    List<String> categories = new ArrayList<String>();

    ShowBeaconAdapter adapter;
    private BeaconDB myDB;
    private ArrayList<BeaconStructure> results = new ArrayList<BeaconStructure>();
    private Cursor allRows = null;
    private ListView listViewEddystone;
    private Tracker mTracker;
    private BeaconStructure beaconView;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    //a seconda del beacon che clicco ci sara' da :
    //aprire il DB e leggere tutti i protocolli simili
    //visualizzarli e far vedere il tempo che e' passato fermo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_beacons);

        listViewEddystone = (ListView) findViewById(R.id.listView);
        listViewEddystone.setClickable(false);


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

        Intent intent = getIntent();
        protocol = intent.getStringExtra(MainActivity.EXTRA_MESSAGE).toLowerCase().trim();


        //creo un istanza del DB
        openDB();

        //clean db: pulisce i db lasciandoci soltanto le ultime 100 righe
        myDB.CleanDb();

        if (protocol.equalsIgnoreCase("Eddystone")) {
            categories.add("Eddystone");
            categories.add("iBeacon");
        } else {
            categories.add("iBeacon");
            categories.add("Eddystone");

        }


        protocolList();


        showResults();

    }


    public void protocolList() {
        try {
            spinner = (Spinner) findViewById(R.id.beaconProtocol);
            // Spinner click listener
            spinner.setOnItemSelectedListener(this);
            // Spinner Drop down elements


            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter.notifyDataSetChanged();

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        protocol = item;
        showResults();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    /**
     * La funzione prende tutti i record dai rispoettivi db e ne mostra soltanto 100, gli altri li elimina
     */
    public void showResults() {
        //visualizzo a video tutti i beacon che ho visto
        try {

            results.clear();

            if (protocol.equalsIgnoreCase("eddystone")) {
                allRows = myDB.LastQueryEddystone();

                if (allRows != null && allRows.getCount() != 0) {
                    allRows.moveToFirst();
                    do {
                        beaconView = new BeaconStructure();
                        beaconView.setNamespaceLayout("NameSpace");
                        beaconView.setInstanceLayout("Instance");

                        beaconView.setBeaconProtocol(allRows.getString(BeaconDB.Col_ProtocolName));
                        beaconView.setBeaconName(allRows.getString(BeaconDB.Col_BeaconName));
                        beaconView.setBeaconID(allRows.getString(BeaconDB.Col_NameSpace));
                        beaconView.setInstance(allRows.getString(BeaconDB.Col_Instance));
                        beaconView.setDistance(allRows.getString(BeaconDB.Col_Distance));
                        beaconView.setRssi(allRows.getString(BeaconDB.Col_RssI));
                        beaconView.setTxPower(allRows.getString(BeaconDB.Col_TxPower));
                        beaconView.setLastAliveOn(allRows.getString(BeaconDB.Col_LastAliveOn));
                        results.add(beaconView);

                    } while (allRows.moveToNext());
                    // notifyDataSetChanged();

                }
            } else if (protocol.equalsIgnoreCase("ibeacon")) {

                allRows = myDB.LastQueryiBeacon();

                if (allRows != null && allRows.getCount() != 0) {
                    allRows.moveToFirst();
                    do {

                        beaconView = new BeaconStructure();
                        beaconView.setNamespaceLayout("UUID");
                        beaconView.setInstanceLayout("Major/Minor");

                        beaconView.setBeaconProtocol(allRows.getString(BeaconDB.Col_ProtocolName));
                        beaconView.setBeaconName(allRows.getString(BeaconDB.Col_BeaconName));
                        beaconView.setBeaconID(allRows.getString(BeaconDB.Col_NameSpace));
                        beaconView.setInstance(allRows.getString(BeaconDB.Col_Instance));
                        beaconView.setDistance(allRows.getString(BeaconDB.Col_Distance));
                        beaconView.setRssi(allRows.getString(BeaconDB.Col_RssI));
                        beaconView.setTxPower(allRows.getString(BeaconDB.Col_TxPower));
                        beaconView.setLastAliveOn(allRows.getString(BeaconDB.Col_LastAliveOn));
                        results.add(beaconView);


                    } while (allRows.moveToNext());
                }

            }


            //visualizzo a video tutti i beacon che ho visto
            runOnUiThread(new Runnable() {
                public void run() {
                    try {

                        adapter = new ShowBeaconAdapter(ShowBeacons.this, results);
                        adapter.notifyDataSetChanged();
                        listViewEddystone.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }

                }
            });


        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }


    }

    /*
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        //protocol = item;
        //showResults();


        // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
*//*
    public void onNothingSelected(AdapterView<?> arg0) {
    }
*/

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
        //qualcosa alla distruzione del gioco
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
}