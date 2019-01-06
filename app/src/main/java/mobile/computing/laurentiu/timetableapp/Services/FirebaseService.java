package mobile.computing.laurentiu.timetableapp.Services;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobile.computing.laurentiu.timetableapp.Constants;
import mobile.computing.laurentiu.timetableapp.SQLite.DatabaseHelper;

public class FirebaseService implements IFirebaseService {

    private String mBaseUrl;
    private static Pattern bachelorPattern = Pattern.compile(Constants.BACHELOR_SCHEDULE_PATTERN);
    private static Pattern masterPattern = Pattern.compile(Constants.MASTER_SCHEDULE_PATTERN);

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private String currentYear = null;
    private String currentSemester = null;
    private DatabaseHelper dbContext = null;

    public static FirebaseService Instance = new FirebaseService();

    private FirebaseService() {
        mBaseUrl = Constants.FIREBASE_BASE_URL;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(final Activity activity) {
        dbContext = new DatabaseHelper(activity.getApplicationContext());

        mAuth.signInAnonymously()
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(activity,
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public Task<byte[]> getBachelorScheduleAsJson(final String studentInput) throws Exception {

        return getInitialSettings("bachelor", getSchoolYear(studentInput))
                .continueWithTask(new Continuation<byte[], Task<byte[]>>() {
                    @Override
                    public Task<byte[]> then(@NonNull Task<byte[]> parentTask) throws Exception {
                        String scheduleUrl = constructBachelorUrlFromInput(studentInput);
                        StorageReference task = mStorageRef.getStorage()
                                .getReferenceFromUrl(scheduleUrl);

                        return task.getBytes(Constants.ONE_MEGABYTE);
                    }
                });
    }

    @Override
    public Task<byte[]> getMasterScheduleAsJson(final String studentInput) throws Exception {
        return getInitialSettings("master", getSchoolYear(studentInput))
                .continueWithTask(new Continuation<byte[], Task<byte[]>>() {
                    @Override
                    public Task<byte[]> then(@NonNull Task<byte[]> parentTask) throws Exception {
                        String scheduleUrl = constructMasterUrlFromInput(studentInput);
                        StorageReference task = mStorageRef.getStorage()
                                .getReferenceFromUrl(scheduleUrl);

                        return task.getBytes(Constants.ONE_MEGABYTE);
                    }
                });
    }

    private Task<byte[]> getInitialSettings(final String education, final int year) {
        String initialSettingsUrl = mBaseUrl + "/settings.json";
        StorageReference initialSettingsTask = mStorageRef.getStorage()
                .getReferenceFromUrl(initialSettingsUrl);

        return initialSettingsTask.getBytes(Constants.ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        JSONObject data = null;
                        try {
                            data = new JSONObject(new String(bytes));
                            currentYear = data.getString("currentYear");
                            String firstMondayFirstSemester = data
                                    .getString("firstMondayFirstSemester");
                            String firstMondaySecondSemester = data
                                    .getString("firstMondaySecondSemester");

                            currentSemester = getCurrentSemester(
                                    firstMondayFirstSemester,
                                    firstMondaySecondSemester);
                            dbContext.populateSettingsTable(
                                    currentYear,
                                    currentSemester,
                                    firstMondayFirstSemester,
                                    firstMondaySecondSemester);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }).continueWithTask(new Continuation<byte[], Task<byte[]>>() {
                    @Override
                    public Task<byte[]> then(@NonNull Task<byte[]> task) {
                        String generalSettingsUrl = String.format(Locale.getDefault(),
                                "%s/%s/general.%s.%d.json",
                                mBaseUrl, currentYear, education, year);
                        StorageReference generalSettingsTask = mStorageRef.getStorage()
                                .getReferenceFromUrl(generalSettingsUrl);

                        return generalSettingsTask.getBytes(Constants.ONE_MEGABYTE)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        JSONObject data = null;
                                        try {
                                            data = new JSONObject(new String(bytes));
                                            dbContext.populateGeneralTable(
                                                    data.getJSONArray(currentSemester));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        dbContext.close();
                                    }
                                });
                    }
                });
    }

    private int getSchoolYear(String studentInput) throws Exception {
        Matcher matches = null;

        if (isInputBachelorValid(studentInput)) {
            matches = bachelorPattern.matcher(studentInput);

            if (!matches.find()) {
                if (matches.groupCount() != 7) {
                    throw new Exception("Bachelor pattern match error");
                }
            }
        }

        if (isInputMasterValid(studentInput)) {
            matches = masterPattern.matcher(studentInput);

            if (!matches.find()) {
                if (matches.groupCount() != 7) {
                    throw new Exception("Master pattern match error");
                }
            }
        }

        return Integer.parseInt(matches.group(2)); // year
    }

    private String getCurrentSemester(String firstMondayFirstSemester,
                                      String firstMondaySecondSemester) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        Date today = new Date();
        Date firstSemester = dateFormat.parse(firstMondayFirstSemester);
        Date secondSemester = dateFormat.parse(firstMondaySecondSemester);

        if (today.compareTo(firstSemester) < 0) {
            throw new ParseException("The general table is corrupted !", 0);
        }

        if (today.compareTo(firstSemester) >= 0 && today.compareTo(secondSemester) < 0) {
            return "sem1";
        }


        if (today.compareTo(secondSemester) >= 0) {
            return "sem2";
        }

        throw new ParseException("The general table is corrupted !", 0);
    }

    @Override
    public boolean isInputBachelorValid(String studentInput) {
        return Pattern.matches(Constants.BACHELOR_SCHEDULE_PATTERN, studentInput);
    }

    private String constructBachelorUrlFromInput(String studentInput) throws Exception {
        Matcher matches = bachelorPattern.matcher(studentInput);

        if (!matches.find()) {
            if (matches.groupCount() != 7) {
                throw new Exception("Bachelor pattern match error");
            }
        }

        return mBaseUrl // 314CAa
                + "/" + currentYear
                + "/bachelor"
                + "/" + currentSemester
                + "/" + matches.group(1) // faculty
                + "/" + matches.group(2) // year
                + "/" + matches.group(3) // group
                + "/" + matches.group(4).toUpperCase() // specialization
                + "/" + matches.group(5).toUpperCase() // series
                + "/" + matches.group(6).toLowerCase() // parity
                + "/" + "schedule.json";
    }

    @Override
    public boolean isInputMasterValid(String studentInput) {
        return Pattern.matches(Constants.MASTER_SCHEDULE_PATTERN, studentInput);
    }

    private String constructMasterUrlFromInput(String studentInput) throws Exception {
        Matcher matches = masterPattern.matcher(studentInput);

        if (!matches.find()) {
            if (matches.groupCount() != 5) {
                throw new Exception("Master pattern match error");
            }
        }

        return mBaseUrl // 32SCPDa
                + "/" + currentYear
                + "/master"
                + "/" + currentSemester
                + "/" + matches.group(1) // faculty
                + "/" + matches.group(2) // year
                + "/" + matches.group(3).toUpperCase() // group
                + "/" + matches.group(4).toLowerCase() // parity
                + "/" + "schedule.json";
    }
}
