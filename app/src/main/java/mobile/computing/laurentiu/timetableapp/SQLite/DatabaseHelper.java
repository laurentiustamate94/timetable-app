package mobile.computing.laurentiu.timetableapp.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import mobile.computing.laurentiu.timetableapp.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    //region Database initialization

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "timetable_app";

    // Table Names
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_GENERAL = "general";
    private static final String TABLE_SCHEDULE = "schedule";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // SETTINGS Table - column names
    private static final String KEY_CURRENT_YEAR = "current_year";
    private static final String KEY_CURRENT_SEMESTER = "current_semester";
    private static final String KEY_FIRST_MONDAY_FIRST_SEMESTER = "first_monday_first_semester";
    private static final String KEY_FIRST_MONDAY_SECOND_SEMESTER = "first_monday_second_semester";

    // GENERAL Table - column names
    private static final String KEY_ENTRY_TYPE = "entry_type";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";

    // SCHEDULE Table - column names
    private static final String KEY_WEEKDAY_NAME = "weekday_name";
    private static final String KEY_COURSE_NAME = "course_name";
    private static final String KEY_SHORT_COURSE_NAME = "short_course_name";
    private static final String KEY_ACTIVITY_TYPE = "activity_type";
    private static final String KEY_PARITY = "parity";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_END_HOUR = "end_hour";

    // Table Create Statements
    // SETTINGS Table create statement
    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS
            + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_CURRENT_YEAR + " TEXT,"
            + KEY_CURRENT_SEMESTER + " TEXT,"
            + KEY_FIRST_MONDAY_FIRST_SEMESTER + " DATETIME,"
            + KEY_FIRST_MONDAY_SECOND_SEMESTER + " DATETIME,"
            + KEY_CREATED_AT + " DATETIME"
            + ")";

    // GENERAL Table create statement
    private static final String CREATE_TABLE_GENERAL = "CREATE TABLE " + TABLE_GENERAL
            + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_CURRENT_YEAR + " TEXT,"
            + KEY_CURRENT_SEMESTER + " TEXT,"
            + KEY_ENTRY_TYPE + " INTEGER,"
            + KEY_START_DATE + " DATETIME,"
            + KEY_END_DATE + " DATETIME,"
            + KEY_CREATED_AT + " DATETIME"
            + ")";

    // SCHEDULE Table create statement
    private static final String CREATE_TABLE_SCHEDULE = "CREATE TABLE " + TABLE_SCHEDULE
            + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_WEEKDAY_NAME + " TEXT,"
            + KEY_COURSE_NAME + " TEXT,"
            + KEY_SHORT_COURSE_NAME + " TEXT,"
            + KEY_ACTIVITY_TYPE + " INTEGER,"
            + KEY_PARITY + " INTEGER,"
            + KEY_LOCATION + " TEXT,"
            + KEY_START_HOUR + " INTEGER,"
            + KEY_END_HOUR + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_SETTINGS);
        db.execSQL(CREATE_TABLE_GENERAL);
        db.execSQL(CREATE_TABLE_SCHEDULE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_SETTINGS));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_GENERAL));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_SCHEDULE));

        // create new tables
        onCreate(db);
    }

    //endregion

    public void populateSettingsTable(String currentYear,
                                      String currentSemester,
                                      String firstMondayFirstSemester,
                                      String firstMondaySecondSemester) {
        SQLiteDatabase dbContext = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CURRENT_YEAR, currentYear);
        values.put(KEY_CURRENT_SEMESTER, currentSemester);
        values.put(KEY_FIRST_MONDAY_FIRST_SEMESTER, firstMondayFirstSemester);
        values.put(KEY_FIRST_MONDAY_SECOND_SEMESTER, firstMondaySecondSemester);
        values.put(KEY_CREATED_AT, getDateTime());

        dbContext.insert(TABLE_SETTINGS, null, values);

        dbContext.close();
    }

    public void populateGeneralTable(JSONArray data) throws JSONException {
        SQLiteDatabase dbContext = this.getWritableDatabase();

        for (int i = 0; i < data.length(); i++) {
            JSONObject entryType = data.getJSONObject(i);

            ContentValues values = new ContentValues();
            values.put(KEY_ENTRY_TYPE, entryType.getString("type"));
            values.put(KEY_START_DATE, entryType.getString("startTime"));
            values.put(KEY_END_DATE, entryType.getString("endTime"));
            values.put(KEY_CREATED_AT, getDateTime());

            dbContext.insert(TABLE_GENERAL, null, values);
        }

        dbContext.close();
    }

    public void populateSchedule(JSONObject data) throws JSONException {
        String[] weekdays = new String[]{
                "monday",
                "tuesday",
                "wednesday",
                "thursday",
                "friday"
        };

        for (String weekday : weekdays) {
            if (data.get(weekday).toString().equals("null")) {
                continue;
            }

            populateWeekdaySchedule(weekday, data.getJSONArray(weekday));
        }
    }

    private void populateWeekdaySchedule(String weekday, JSONArray data) throws JSONException {
        SQLiteDatabase dbContext = this.getWritableDatabase();

        for (int i = 0; i < data.length(); i++) {
            JSONObject entryType = data.getJSONObject(i);

            ContentValues values = new ContentValues();
            values.put(KEY_WEEKDAY_NAME, weekday);
            values.put(KEY_COURSE_NAME, entryType.getString("name"));
            values.put(KEY_SHORT_COURSE_NAME, entryType.getString("shortName"));
            values.put(KEY_ACTIVITY_TYPE, intFromActivityType(entryType.getString("type")));
            values.put(KEY_PARITY, intFromParityType(entryType.getString("parity")));
            values.put(KEY_LOCATION, entryType.getString("location"));
            values.put(KEY_START_HOUR, entryType.getString("startHour"));
            values.put(KEY_END_HOUR, entryType.getString("endHour"));
            values.put(KEY_CREATED_AT, getDateTime());

            dbContext.insert(TABLE_SCHEDULE, null, values);
        }

        dbContext.close();
    }

    public boolean isScheduleTableEmpty() {
        SQLiteDatabase dbContext = this.getReadableDatabase();

        String sql = String.format("SELECT COUNT(%s) FROM %s", KEY_ID, TABLE_SCHEDULE);
        boolean empty = true;
        Cursor cursor = dbContext.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            empty = (cursor.getInt(0) == 0);
        }

        cursor.close();
        dbContext.close();

        return empty;
    }

    public String getParityFromDate(Date time) throws ParseException {
        SQLiteDatabase dbContext = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_SETTINGS + " limit 1";
        Cursor cursor = dbContext.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        String firstMondayString;
        if (cursor.getString(cursor.getColumnIndex(KEY_CURRENT_SEMESTER)).equals("sem1")) {

            firstMondayString = cursor.getString(
                    cursor.getColumnIndex(KEY_FIRST_MONDAY_FIRST_SEMESTER));
        } else if (cursor.getString(cursor.getColumnIndex(KEY_CURRENT_SEMESTER)).equals("sem2")) {

            firstMondayString = cursor.getString(
                    cursor.getColumnIndex(KEY_FIRST_MONDAY_SECOND_SEMESTER));
        } else {
            throw new ParseException("Settings table corrupted", 0);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Date firstMonday = dateFormat.parse(firstMondayString);

        long diffInMilliseconds = Math.abs(time.getTime() - firstMonday.getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);

        return (diffInDays / Constants.NUMBER_OF_DAYS_IN_WEEK) % 2 == 1 ? "even" : "odd";
    }

    public List<ScheduleDay> getSchedule(String weekday, String parity) {
        List<ScheduleDay> scheduleDays = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SCHEDULE
                + " WHERE " + KEY_WEEKDAY_NAME + " = " + "'" + weekday.toLowerCase() + "'"
                + " AND (" + KEY_PARITY + " = " + "'" + intFromParityType(parity.toLowerCase()) + "'"
                + " OR " + KEY_PARITY + " = " + "'" + intFromParityType("always") + "')";

        SQLiteDatabase dbContext = this.getReadableDatabase();
        Cursor cursor = dbContext.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ScheduleDay scheduleDay = new ScheduleDay(
                        cursor.getString(cursor.getColumnIndex(KEY_WEEKDAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_COURSE_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_SHORT_COURSE_NAME)),
                        activityTypeFromInt(cursor.getInt(cursor.getColumnIndex(KEY_ACTIVITY_TYPE))),
                        parityTypeFromInt(cursor.getInt(cursor.getColumnIndex(KEY_PARITY))),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getShort(cursor.getColumnIndex(KEY_START_HOUR)),
                        cursor.getShort(cursor.getColumnIndex(KEY_END_HOUR)));

                scheduleDays.add(scheduleDay);
            } while (cursor.moveToNext());
        }

        return scheduleDays;
    }

    //region Helper methods

    public void deleteDB() {
        SQLiteDatabase dbContext = this.getWritableDatabase();

        this.onUpgrade(dbContext, 1, 1);

        dbContext.close();
    }

    private String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private ScheduleDayActivityType activityTypeFromInt(int x) {
        switch (x) {
            case 0:
                return ScheduleDayActivityType.COURSE;
            case 1:
                return ScheduleDayActivityType.LABORATORY;
            case 2:
                return ScheduleDayActivityType.PROJECT;
            case 3:
                return ScheduleDayActivityType.SEMINAR;
        }

        return null;
    }

    private ScheduleDayParityType parityTypeFromInt(int x) {
        switch (x) {
            case 0:
                return ScheduleDayParityType.EVEN;
            case 1:
                return ScheduleDayParityType.ODD;
            case 2:
                return ScheduleDayParityType.ALWAYS;
        }

        return null;
    }

    private int intFromActivityType(String x) {
        switch (x) {
            case "course":
                return 0;
            case "laboratory":
                return 1;
            case "project":
                return 2;
            case "seminar":
                return 3;
        }

        return -1;
    }

    private int intFromParityType(String x) {
        switch (x) {
            case "even":
                return 0;
            case "odd":
                return 1;
            case "always":
                return 2;
        }

        return -1;
    }

    //endregion
}
