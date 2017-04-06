package beacondetector.emulk.it.beacondetector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by root on 26/02/17.
 */

public class TargetedAD extends Activity implements BeaconConsumer, RangeNotifier {

    private Button btnHit;
    private TextView txtJson;
    private TextView sold;
    private TextView maleSold;
    private TextView femaleSold;
    private TextView happySold;
    private TextView sadSold;
    private ProgressDialog pd;
    private EditText text;

    private BeaconManager mBeaconManager;
    private double distance = 0;
    private int numRecords = 0;

    private StringBuilder buffer = new StringBuilder();
    private boolean update = false;

    private HttpURLConnection connection = null;
    private BufferedReader reader = null;

    //iBeacon
    private String BeaconMajor = "";
    private String BeaconMinor = "";
    private String BeaconUUID = "";

    //Eddystone
    private String namespaceId = "";
    private String instanceId = "";

    private String lastTimeStamp = "0";

    private List<Expressivity> bayesClassifier = new ArrayList();
    //lista finale con i valori medi
    List<Expressivity> finalList = new ArrayList<>();

    //lista attuale con i valori medi
    List<Expressivity> actualExpresionList = new ArrayList<>();


    private boolean firstExecution = true;

    //probabilità

    private double totMale = 0;

    private double probSold = 0;
    private double probMaleSold = 0;
    private double probFemaleSold = 0;
    private double probHappSold = 0;
    private double probSadSold = 0;


    private double actualProbSold = 0;
    private double actualProbMaleSold = 0;
    private double actualProbHappSold = 0;
    private double actualProbSadSold = 0;

    private int totRecords = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.targetedad);

        btnHit = (Button) findViewById(R.id.getJson);
        text = (EditText) findViewById(R.id.jsonUrl);

        final String jsonUrl = text.getText().toString();

        sold = (TextView) findViewById(R.id.sold);
        maleSold = (TextView) findViewById(R.id.maleSold);
        femaleSold = (TextView) findViewById(R.id.femaleSold);
        happySold = (TextView) findViewById(R.id.happySold);
        sadSold = (TextView) findViewById(R.id.sadSold);

        //se sono a due metri dal beacon posso processare i dati
        /* new Timer().scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            new JsonTask().execute(text.getText().toString());
        }
        }, 0, 1000);*/

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * start beacon listener
                 */
                if (text.length() > 0 && text != null) {
                    startBeaconListener();
                }


            }
        });


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
        //mBeaconManager.setForegroundScanPeriod(5000l);
        //mBeaconManager.setBackgroundScanPeriod(50000l);
        mBeaconManager.setBackgroundBetweenScanPeriod(3000l);
        mBeaconManager.setForegroundBetweenScanPeriod(3000l);
        mBeaconManager.setBackgroundScanPeriod(1000l);
        mBeaconManager.setForegroundScanPeriod(1000l);
        mBeaconManager.bind(this);
    }

    /**
     * @param beacons
     * @param region
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {

            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // Eddystone-UID frame
                //ogni id rappresenta un negozio
                Identifier namespaceIdTMP = beacon.getId1();
                final Identifier instanceIdTMP = beacon.getId2();
                namespaceId = namespaceIdTMP.toString().substring(2);
                instanceId = instanceIdTMP.toString().substring(2);


                distance = beacon.getDistance();
                if (distance <= 1.5) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new JsonTask().execute(text.getText().toString());

                            if (update) {
                                printProbValue();
                                //lo faccio eseguire sul thread che ha creato l'ui
                                //txtJson.setText(buffer.toString());
                            }
                            try {
                                synchronized (this) {
                                    wait(5 * 1000);
                                }
                            } catch (Exception e) {
                                //catch exception
                            }
                        }
                    });
                }

            } else if (beacon.getServiceUuid() != 0 && beacon.getBeaconTypeCode() == 533) {
                //iBeacon
                //ogni id rappresenta un negozio

                BeaconUUID = (beacon.getId1() + "").toUpperCase();
                BeaconMajor = beacon.getId2() + "";
                BeaconMinor = beacon.getId3() + "";

                distance = beacon.getDistance();
                if (distance <= 1.5) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new JsonTask().execute(text.getText().toString());

                            if (update) {
                                printProbValue();
                                //lo faccio eseguire sul thread che ha creato l'ui
                                //txtJson.setText(buffer.toString());
                            }
                            try {
                                synchronized (this) {
                                    wait(10 * 1000);
                                }
                            } catch (Exception e) {
                                //catch exception
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Stampo le probabilità che il nuovo utente ha di acquistare
     * utilizzando lo storico
     */
    public void printProbValue() {

        probFemaleSold = (1 - probMaleSold / totRecords);

        sold.setText("Probabilità di vendita: " + probSold / totRecords);

        //la probabilita che un maschio compri

        //maleSold.setText("Probabilità che un maschio compri: " + (probMaleSold / totRecords) * (probSold / totRecords));
        maleSold.setText("Probabilità che un maschio compri: " + ((probMaleSold / totMale) * (totMale / totRecords) / (probSold / totRecords)));

        femaleSold.setText("Probabilità che una feminna compri: " + probFemaleSold * (probSold / totRecords));

        happySold.setText("Probabilità che una persona felice compri: " + (probHappSold / totRecords) * (probSold / totRecords));

        sadSold.setText("Probabilità che una persona triste compri: " + (probSadSold / totRecords) * (probSold / totRecords));

    }

    /**
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        /*protected void onPreExecute() {
            //creare un loop che richiede i dati ogni secondo
            super.onPreExecute();

            pd = new ProgressDialog(TargetedAD.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }*/

        //doInBackground
        protected String doInBackground(String... params) {

            System.setProperty("http.keepAlive", "false");

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);


            long appTimeStamp = calendar.getTimeInMillis();
            try {
                URL url = new URL(params[0] + "?lastTimestamp=" + lastTimeStamp);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setReadTimeout(2 * 1000);
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";

                buffer = new StringBuilder();
                //lego la risposta con il json all'interno
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);
                }

                JSONObject jsonObject = new JSONObject(buffer.toString());
                JSONArray expressions = jsonObject.getJSONArray("records");
                if (expressions.length() > 0 ) {
                    numRecords = expressions.length();
                    update = true;

                    //scorro ogni record
                    for (int i = 0; i < expressions.length(); i++) {
                        JSONObject expression = expressions.getJSONObject(i);
                        Expressivity exp = new Expressivity();


                        exp.setFaceNum(Integer.parseInt(expression.getString("faceNum")));
                        exp.setFaceX(Integer.parseInt(expression.getString("faceX")));
                        exp.setFaceY(Integer.parseInt(expression.getString("faceY")));
                        exp.setFaceSize(Integer.parseInt(expression.getString("faceSize")));
                        exp.setFaceYaw(Integer.parseInt(expression.getString("faceYaw")));
                        exp.setFacePitch(Integer.parseInt(expression.getString("facePitch")));
                        exp.setFaceRoll(Integer.parseInt(expression.getString("faceRoll")));
                        exp.setAge(Integer.parseInt(expression.getString("age")));
                        exp.setGender(Integer.parseInt(expression.getString("gender")));
                        exp.setNeutral(Integer.parseInt(expression.getString("neutral")));
                        exp.setHappiness(Integer.parseInt(expression.getString("happiness")));
                        exp.setSurprise(Integer.parseInt(expression.getString("surprise")));
                        exp.setAnger(Integer.parseInt(expression.getString("anger")));
                        exp.setSadness(Integer.parseInt(expression.getString("sadness")));
                        exp.setPositive(Integer.parseInt(expression.getString("positive")));
                        exp.setExpression(Integer.parseInt(expression.getString("expression")));
                        exp.setOffer(randInt(0,6));
                        exp.setSold(randInt(0, 1));
                        lastTimeStamp = expression.getString("timeStamp");
                        //prendo in considerazione il record

                        bayesClassifier.add(i, exp);

                       /* happiness = Integer.parseInt(expression.getString("happiness"));
                        surprise = Integer.parseInt(expression.getString("surprise"));
                        age = Integer.parseInt(expression.getString("age"));

                        if (BeaconUUID.equalsIgnoreCase("29D95B69-0F70-42C2-90A8-8B614A6FB59D")) {
                            //negozio di abbigliamenti, manda una mail se l'utente è felice e sorpreso
                            if (happiness >= 25 && surprise >= 25) {

                                //send mail suggerendo l'articolo da comprare

                            }
                            if (age >= 20 && age <= 40) {
                                //proponi articoli addatti a questa eta'
                            }
                        }*/

                    }

                    //simulo di avere uno storico di 10 clienti
                    for (int i = 0; i < bayesClassifier.size(); i++) {
                        if (bayesClassifier.get(i).getGender() == 1) {
                            totMale++;
                            if (bayesClassifier.get(i).getSold() == 1) {
                                probMaleSold++;
                            }
                        }

                        if (bayesClassifier.get(i).getSold() == 1) {
                            probSold++;

                        }
                        if (bayesClassifier.get(i).getHappiness() > 15) {
                            probHappSold++;
                        }

                        if (bayesClassifier.get(i).getSadness() > 15) {
                            probSadSold++;
                        }
                        totRecords++;
                    }

                    bayesClassifier.clear();


                } else {
                    update = false;
                }

            } catch (
                    Exception e)

            {
                e.printStackTrace();
            } finally

            {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;

        }

       /* @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            txtJson.setText(result);
        }*/

    }

    /**
     * generate a pseudo casual number in range
     *
     * @param min
     * @param max
     * @return
     */
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }


    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //qualcosa alla distruzione del gioco
        mBeaconManager.unbind(this);
        if (connection != null) {
            connection.disconnect();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
        //qualcosa alla distruzione del gioco
        mBeaconManager.unbind(this);
        if (connection != null) {
            connection.disconnect();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
