package beacondetector.emulk.it.beacondetector;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by asd on 23/07/16.
 */
public class Trilateration extends Activity {


    private static final String TAG = "ShowBeacons";
    private BeaconDB myDB;
    List<String> radiusBeacon = new ArrayList<String>();
    private Location myLocation;
    private Location myLocation1;

    private Double distanceA;
    private Double distanceB;
    private Double distanceC;

    private Location beaconA = new Location("beaconA");
    private Location beaconB = new Location("beaconB");
    private Location beaconC = new Location("beaconC");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.trilateration);

        openDB();

        Cursor allRowsEddystone = myDB.getDistinctEddystonRows();
        Cursor allRowsiBeacons = myDB.getDistinctiBeaconsRows();

        final TextView coordinatePositions = (TextView) this.findViewById(R.id.coordinate);

        beaconA.setLongitude(0.5);
        beaconA.setLatitude(1.5);

        beaconB.setLongitude(2.5);
        beaconB.setLatitude(5.5);

        beaconC.setLongitude(5.5);
        beaconC.setLatitude(3.5);

        distanceA = 2.;
        distanceB = 2.;
        distanceC = 2.;



        myLocation = getLocationWithTrilateration(beaconA, beaconB, beaconC, distanceA, distanceB, distanceC);



        //String coordinate = myLocation1.getLongitude() + " / " + myLocation1.getLatitude();

        //coordinatePositions.setText(coordinate);

        Button button = (Button) findViewById(R.id.buttonLocate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myLocation1 = myTrilateration(beaconA, beaconB, beaconC, distanceA, distanceB, distanceC);
                String coordinate = myLocation1.getLongitude() + " / " + myLocation1.getLatitude();

                coordinatePositions.setText(coordinate);

            }
        });
    }

    /**
     * @param beaconA
     * @param beaconB
     * @param beaconC
     * @param distanceA
     * @param distanceB
     * @param distanceC
     * @return
     */
    public static Location getLocationWithTrilateration(Location beaconA, Location beaconB, Location beaconC, double distanceA, double distanceB, double distanceC) {

        double Ax = beaconA.getLongitude();
        double Ay = beaconA.getLatitude();
        double Bx = beaconB.getLongitude();
        double By = beaconB.getLatitude();
        double Cx = beaconC.getLongitude();
        double Cy = beaconC.getLatitude();


        double W, Z, Dx, Dy, DyFilter;
        W = pow(distanceA, 2) - pow(distanceB, 2) - pow(Ay, 2) - pow(Ax, 2) + pow(By, 2) + pow(Bx, 2);
        Z = pow(distanceB, 2) - pow(distanceC, 2) - pow(By, 2) - pow(Bx, 2) + pow(Cy, 2) + pow(Cx, 2);

        Dx = (W * (Cx - Bx) - Z * (Bx - Ax)) / (2 * ((By - Ay) * (Cx - Bx) - (Cy - By) * (Bx - Ax)));
        Dy = (W - 2 * Dx * (By - Ay)) / (2 * (Bx - Ax));
        //`DyFilter` is a second measure of `Dy` to mitigate errors
        DyFilter = (Z - 2 * Dx * (Cy - By)) / (2 * (Cx - Bx));

        Dy = (Dy + DyFilter) / 2;

        Location foundLocation = new Location("Location");

        foundLocation.setLongitude(Dx);
        foundLocation.setLatitude(Dy);

        return foundLocation;
    }

    /**
     * @param beaconA
     * @param beaconB
     * @param beaconC
     * @param distanceA
     * @param distanceB
     * @param distanceC
     * @return
     */
    public Location myTrilateration(Location beaconA, Location beaconB, Location beaconC, double distanceA, double distanceB, double distanceC) {

        double Ax = beaconA.getLongitude();
        double Ay = beaconA.getLatitude();
        double Bx = beaconB.getLongitude();
        double By = beaconB.getLatitude();
        double Cx = beaconC.getLongitude();
        double Cy = beaconC.getLatitude();
        double Ar = distanceA;
        double Br = distanceB;
        double Cr = distanceC;

        double euclidenDistanceAB = sqrt(pow((Bx - Ax), 2) + pow((By - Ay), 2));

        double exx = (Bx - Ax) / euclidenDistanceAB;
        double exy = (By - Ay) / euclidenDistanceAB;

        //signed magnitude of the x component
        double ix = exx * (Cx - Ax);
        double iy = exy * (Cy - Ay);

        //the unit vector in the y direction.
        double eyx = (Cx - Ax - ix * exx) / sqrt(pow(Cx - Ax - ix * exx, 2) + pow(Cy - Ay - iy * exy, 2));
        double eyy = (Cy - Ay - iy * exy) / sqrt(pow(Cy - Ax - ix * exx, 2) + pow(Cy - Ay - iy * exy, 2));

        //the signed magnitude of the y component
        double jx = eyx * (Cx - Ax);
        double jy = eyy * (Cy - Ay);

        //coordinates
        double Dx = (pow(Ar, 2) - pow(Br, 2) + pow(euclidenDistanceAB, 2)) / (2 * euclidenDistanceAB);
        double Dy = (pow(Ar, 2) - pow(Cr, 2) + pow(iy, 2) + pow(jy, 2)) / (2 * jy) - ix * Dx / jx;

        Location userLocation = new Location("UserLocation");
        userLocation.setLongitude(Dx);
        userLocation.setLatitude(Dy);

        return userLocation;
    }

    // apro il database
    private void openDB() {
        myDB = new BeaconDB(this);
        myDB.open();
    }

    private void closeDB() {
        //myDB.close();

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
        closeDB();

    }
}