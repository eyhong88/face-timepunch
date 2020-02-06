package com.sbux.wfm.face.punch.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

        // list of constants for referencing the db's internal values
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "time_punch.db";

        public static final String TABLE_NAME = "time_punch";
        public static final String TIMETRACKER_COLUMN_ID = "_id";
        public static final String TIMETRACKER_COLUMN_PARTNER = "partnerNbr";
        public static final String TIMETRACKER_COLUMN_PUNCH_TYPE = "punchType";
        public static final String TIMETRACKER_COLUMN_PUNCH_DTM = "punchDtm";
        public static final String TIMETRACKER_COLUMN_TEMP_FACE_ID = "tempFaceId";

        public static final String USER_TABLE_NAME = "partner_face";
        public static final String PROFILE_COLUMN_ID = "_id";
        public static final String PROFILE_COLUMN_USERNAME = "partnerNbr";
        public static final String PROFILE_COLUMN_FIRST = "firstName";
        public static final String PROFILE_COLUMN_LAST = "lastName";
        public static final String PROFILE_COLUMN_PERSON_ID = "faceId";

        // stored local variables for the OpenHelper and the database it opens
//        public DatabaseOpenHelper openHelper;
        public SQLiteDatabase database;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
                database = this.getWritableDatabase();
        }
        // when the db is created. this method is called by onUpgrade when the
        // version number is changed
        public void onCreate(SQLiteDatabase database) {
                // execSQL actually creates the schema of the db we defined in the
                // method above
                database.execSQL("CREATE TABLE " + TABLE_NAME + "( "
                        + TIMETRACKER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TIMETRACKER_COLUMN_PARTNER + " TEXT, "
                        + TIMETRACKER_COLUMN_PUNCH_TYPE + " TEXT, "
                        + TIMETRACKER_COLUMN_PUNCH_DTM + " TEXT,"
                        + TIMETRACKER_COLUMN_TEMP_FACE_ID + " TEXT)");

                database.execSQL("CREATE TABLE " + USER_TABLE_NAME + "( "
                        + PROFILE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PROFILE_COLUMN_USERNAME + " TEXT, "
                        + PROFILE_COLUMN_FIRST + " TEXT, "
                        + PROFILE_COLUMN_LAST + " TEXT, "
                        + PROFILE_COLUMN_PERSON_ID + " TEXT )");
        }

        // when the version number is changed, this method is called
        public void onUpgrade(SQLiteDatabase database, int oldVersion,
                              int newVersion) {
                // drops table if exists, and then calls onCreate which implements
                // our new schema
                database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                database.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
                onCreate(database);
        }

        // method called by onActivityResult which actually saves user entered data
        // into the db
        public void savePunch(String partnerNbr, String punchType, String punchDtm, String tempFaceId) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TIMETRACKER_COLUMN_PARTNER, partnerNbr);
                contentValues.put(TIMETRACKER_COLUMN_PUNCH_TYPE, punchType);
                contentValues.put(TIMETRACKER_COLUMN_PUNCH_DTM, punchDtm);
                contentValues.put(TIMETRACKER_COLUMN_TEMP_FACE_ID, tempFaceId);
                database.insert(TABLE_NAME, null, contentValues);
        }

        // method that saves the users profile information
        public void saveUserProfile(String userName, String firstName,
                                    String lastName, String personId) {

//                // Clears the legacy profile information first
//                database.execSQL("DELETE FROM partner_face;");

                // Writes the new profile information
                ContentValues contentValues = new ContentValues();
                contentValues.put(PROFILE_COLUMN_USERNAME, userName);
                contentValues.put(PROFILE_COLUMN_FIRST, firstName);
                contentValues.put(PROFILE_COLUMN_LAST, lastName);
                contentValues.put(PROFILE_COLUMN_PERSON_ID, personId);
                long result = database.insert(USER_TABLE_NAME, null, contentValues);
                System.out.println("INSERT RESULT: " + result);
        }

        // null because there are no selection args since we are just selecting
        // everything
        public Cursor getUserProfileInfo() {
                return database.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
        }

        // null because there are no selection args since we are just selecting
        // everything
        public Cursor getAllPunches() {
                return database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY "
                        + TIMETRACKER_COLUMN_ID + " DESC", null);
        }

        // null because there are no selection args since we are just selecting
        // everything
        public Cursor getPunchesForPartner(String partnerNbr) {
                return database.rawQuery("SELECT * FROM time_punch WHERE partnerNbr = '"
                        + partnerNbr + "';", null);
        }

        public void deleteSinglePunch(String partnerNbr) {
                database.execSQL("DELETE FROM time_punch WHERE partnerNbr = '"
                        + partnerNbr + "';");
        }

        // null because there are no selection args since we are just selecting
        // everything
        public Cursor getPartner(String partnerNbr) {
                return database.rawQuery("SELECT * FROM partner_face WHERE partnerNbr = '"
                        + partnerNbr + "';", null);
        }
        public Cursor getPartnerByPersonId(String personId) {
                return database.rawQuery("SELECT * FROM partner_face WHERE faceId = '"
                        + personId + "';", null);
        }

        public int deletePartner(String partnerNbr) {
                int result = database.delete(USER_TABLE_NAME, "1", null);
                return result;
//                database.execSQL("DELETE FROM partner_face WHERE partnerNbr = '"
//                        + partnerNbr + "';");
        }
}