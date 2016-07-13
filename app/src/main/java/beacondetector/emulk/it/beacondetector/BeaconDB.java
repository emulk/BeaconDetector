package beacondetector.emulk.it.beacondetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by asd on 08/07/16.
 */
public class BeaconDB {
    public static final String KEY_ROWID = "_id";
    //nomi colonne Protocolo Eddystone
    public static final String ProtocolName = "ProtocolName";
    public static final String BeaconName = "BeaconName";
    public static final String NameSpace = "NameSpace";
    public static final String Instance = "Instance";
    public static final String Distance = "Distance";
    public static final String RssI = "RssI";
    public static final String TxPower = "TxPower";
    public static final String LastAlive = "LastAlive";
    //Nomi colonne protocollo Eddystone URL
    public static final String UrlLink = "UrlLink";
    //Nomi colonne protocollo iBeacon
    public static final String Major = "Major";
    public static final String Minor = "Minor";
    //colonne
    public static final int Col_ROWID = 0;
    public static final int Col_ProtocolName = 1;
    public static final int Col_BeaconName = 2;
    public static final int Col_NameSpace = 3;
    public static final int Col_Instance = 4;
    public static final int Col_Distance = 5;
    public static final int Col_RssI = 6;
    public static final int Col_TxPower = 7;
    public static final int Col_LastAliveOn = 8;
    public static final int Col_UrlLink = 9;
    public static final int Col_Major = 10;
    public static final int Col_Minor = 11;

    public static final String[] ALL_KEYSEDDYSTONE = new String[]{
            KEY_ROWID,
            ProtocolName,
            BeaconName,
            NameSpace,
            Instance,
            Distance,
            RssI,
            TxPower,
            LastAlive
    };

    public static final String[] ALL_KEYSEDDYSTONEURL = new String[]{
            KEY_ROWID,
            ProtocolName,
            BeaconName,
            UrlLink,
            LastAlive
    };

    public static final String[] ALL_KEYSiBEACON = new String[]{
            KEY_ROWID,
            ProtocolName,
            BeaconName,
            NameSpace,
            Major,
            Minor,
            Distance,
            RssI,
            TxPower,
            LastAlive
    };
    /*per il debug, mi indica l'attività in esecuzione*/
    private static final String TAG = "BeaconDB";

    /*DB info, name and table name*/
    public static final String DATABASE_NAME = "Beacon Detector";
    public static final String DATABASE_TABLEEDDYSTONE = "Eddystone_Table";
    public static final String DATABASE_TABLEURL = "Eddystone_URL_Table";
    public static final String DATABASE_TABLEiBEACON = "iBeacon_Table";

    /*Track the DB version scheme, if the scheme change increment the database version number*/
    public static final int DATABASE_VERSION = 1;

    /*Query SQL per creare la tabella del protocollo EddystoneUID*/
    private static final String DATABASE_CREATE_SQL_EDDYSTONE = "create table "
            + DATABASE_TABLEEDDYSTONE + " (" + KEY_ROWID
            + " integer primary key autoincrement, "
            + ProtocolName + " string , " + BeaconName + " string , "  + NameSpace + " string , "
            + Instance + " string , " + Distance + " integer ," + RssI + " integer, " + TxPower + " integer , " + LastAlive + " string"

            + ");";

    /*Query SQL per creare la tabella del protocollo EddystoneURL*/
    private static final String DATABASE_CREATE_SQL_URL = "create table "
            + DATABASE_TABLEURL + " (" + KEY_ROWID
            + " integer primary key autoincrement, "
            + ProtocolName + " string , " + BeaconName + " string , "  + UrlLink + " string , " + LastAlive + " string"

            + ");";

    /*Query SQL per creare la tabella del protocollo iBeacon*/
    private static final String DATABASE_CREATE_SQL_iBEACON = "create table "
            + DATABASE_TABLEiBEACON + " (" + KEY_ROWID
            + " integer primary key autoincrement, "
            + ProtocolName + " string , " + BeaconName + " string , "  + NameSpace + " string , "
            + Major + "integer , " + Minor + "integer , " + Distance + " integer ," + RssI + " integer, " + TxPower + " integer , " + LastAlive + " string"

            + ");";

    // Context of application who uses us.
    private final Context context ;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    // ///////////////////////////////////////////////////////////////////
    // Public methods:
    // ///////////////////////////////////////////////////////////////////
    public BeaconDB(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public BeaconDB open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }


    // inserisce una nuova righa all'interno del database
    public long insertRowEddystoneTable( String protocolName, String beaconName, String nameSpace, String instance, int distance, int rssI, int txPower, String lastAlive) {

        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(ProtocolName, protocolName);
        initialValues.put(BeaconName, beaconName);
        initialValues.put(NameSpace, nameSpace);
        initialValues.put(Instance, instance);
        initialValues.put(Distance, distance);
        initialValues.put(RssI, rssI);
        initialValues.put(TxPower, txPower);
        initialValues.put( LastAlive, lastAlive );

        // Insert it into the database.
        return db.insert(DATABASE_TABLEEDDYSTONE, null, initialValues);
    }

    // inserisce una nuova righa all'interno del database
    public long insertRowUrlTable( String protocolName, String beaconName,  String url,  String lastAlive) {

        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(ProtocolName, protocolName);
        initialValues.put(BeaconName, beaconName);
        initialValues.put(UrlLink, url);
        initialValues.put( LastAlive, lastAlive );

        // Insert it into the database.
        return db.insert(DATABASE_TABLEURL, null, initialValues);
    }

    // inserisce una nuova righa all'interno del database
    public long insertRowiBeaconTable( String protocolName, String beaconName, String nameSpace, String major,String minor, int distance, int rssI, int txPower, String lastAlive) {

        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(ProtocolName, protocolName);
        initialValues.put(BeaconName, beaconName);
        initialValues.put(NameSpace, nameSpace);
        initialValues.put(Major, major);
        initialValues.put(Minor, minor);
        initialValues.put(Distance, distance);
        initialValues.put(RssI, rssI);
        initialValues.put(TxPower, txPower);
        initialValues.put( LastAlive, lastAlive );

        // Insert it into the database.
        return db.insert(DATABASE_TABLEiBEACON, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key) adn database table name
    public boolean deleteRow(long rowId, String dataBaseTable) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(dataBaseTable, where, null) != 0;
    }


    /*Restituisce l'ultima riga della tabella eddystone uid*/
    public Cursor LastQueryEddystone() {
        Cursor cursorlast = db.query(true, DATABASE_TABLEEDDYSTONE, ALL_KEYSEDDYSTONE, null,
                null, null, null, null, null);

        if (cursorlast.moveToFirst() == cursorlast.moveToLast()) {
            cursorlast.moveToFirst();
            return cursorlast;
        } else {
            cursorlast.moveToLast();
            return cursorlast;
        }

    }

    /*Restituisce l'ultima riga della tabella eddystone url*/
    public Cursor LastQueryEddystoneUrl() {
        Cursor cursorlast = db.query(true, DATABASE_TABLEURL, ALL_KEYSEDDYSTONEURL, null,
                null, null, null, null, null);

        if (cursorlast.moveToFirst() == cursorlast.moveToLast()) {
            cursorlast.moveToFirst();
            return cursorlast;
        } else {
            cursorlast.moveToLast();
            return cursorlast;
        }

    }

    /*Restituisce l'ultima riga della tabella iBeacon*/
    public Cursor LastQueryiBeacon() {
        Cursor cursorlast = db.query(true, DATABASE_TABLEiBEACON, ALL_KEYSiBEACON, null,
                null, null, null, null, null);

        if (cursorlast.moveToFirst() == cursorlast.moveToLast()) {
            cursorlast.moveToFirst();
            return cursorlast;
        } else {
            cursorlast.moveToLast();
            return cursorlast;
        }

    }






































    // ///////////////////////////////////////////////////////////////////
    // Private Helper Classes:
    // ///////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading. Used to
     * handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL_EDDYSTONE);
            _db.execSQL(DATABASE_CREATE_SQL_URL);
            _db.execSQL(DATABASE_CREATE_SQL_iBEACON);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLEEDDYSTONE);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLEURL);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLEiBEACON);

            // Recreate new database:
            onCreate(_db);
        }
    }



}
