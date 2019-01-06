package mobile.computing.laurentiu.timetableapp;

import android.content.Intent;
import android.support.annotation.NonNull;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageException;

import org.json.JSONException;
import org.json.JSONObject;

import mobile.computing.laurentiu.timetableapp.Helpers.LoaderAppCompatActivity;
import mobile.computing.laurentiu.timetableapp.SQLite.DatabaseHelper;
import mobile.computing.laurentiu.timetableapp.Services.FirebaseService;

public class RegisterActivity extends LoaderAppCompatActivity {

    private AutoCompleteTextView mBachelorView;
    private EditText mMasterView;

    private DatabaseHelper dbContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getLoaderManager().initLoader(0, null, this);

        setupEditorActions();

        mRegisterView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        dbContext = new DatabaseHelper(getApplicationContext());
        FirebaseService.Instance.onCreate(this);
    }

    //region Button interactions

    private void setupEditorActions() {
        TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                boolean shouldStart = id == EditorInfo.IME_ACTION_DONE
                        || id == EditorInfo.IME_NULL;

                if (shouldStart) {
                    attemptRegister();
                }

                return !shouldStart;
            }
        };

        mBachelorView = findViewById(R.id.bachelor);
        mBachelorView.setOnEditorActionListener(listener);

        mMasterView = findViewById(R.id.master);
        mMasterView.setOnEditorActionListener(listener);

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    attemptRegister();
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this,
                            "Keyboard hiding crashed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //endregion

    //region Input validation

    private boolean hasInvalidStudentInput() {
        // Reset errors.
        mBachelorView.setError(null);
        mMasterView.setError(null);

        // Store values at the time of the register attempt.
        String bachelor = mBachelorView.getText().toString();
        String master = mMasterView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid group.
        if (TextUtils.isEmpty(bachelor) && TextUtils.isEmpty(master)) {
            mBachelorView.setError(getString(R.string.error_field_required));
            mMasterView.setError(getString(R.string.error_field_required));
            focusView = mBachelorView;
            cancel = true;
        } else if (!TextUtils.isEmpty(bachelor) && !FirebaseService.Instance.isInputBachelorValid(bachelor)) {
            mBachelorView.setError(getString(R.string.error_invalid_bachelor));
            focusView = mBachelorView;
            cancel = true;
        } else if (!TextUtils.isEmpty(master) && !FirebaseService.Instance.isInputMasterValid(master)) {
            mMasterView.setError(getString(R.string.error_invalid_master));
            focusView = mMasterView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt download and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
        }

        return cancel;
    }

    //endregion

    private void attemptRegister() {
        if (hasInvalidStudentInput()) {
            return;
        }

        OnSuccessListener<byte[]> onSuccess = new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                JSONObject data = null;
                try {
                    data = new JSONObject(new String(bytes));
                    dbContext.populateSchedule(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dbContext.close();
                showProgress(false);
                finish();
            }
        };

        OnFailureListener onFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(exception instanceof StorageException) {
                    Intent diyIntent = new Intent(
                            RegisterActivity.this,
                            DoItYourselfActivity.class);
                    diyIntent
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    startActivity(diyIntent);

                    dbContext.close();
                    showProgress(false);
                    finish();
                }
                else {
                    Toast.makeText(RegisterActivity.this,
                            "Something went wrong",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        String bachelorStudentInput = mBachelorView.getText().toString();
        String masterStudentInput = mMasterView.getText().toString();

        try {
            if(!TextUtils.isEmpty(bachelorStudentInput)) {
                FirebaseService.Instance.getBachelorScheduleAsJson(bachelorStudentInput)
                        .addOnSuccessListener(onSuccess)
                        .addOnFailureListener(onFailure);
            }
            else if(!TextUtils.isEmpty(masterStudentInput)) {
                FirebaseService.Instance.getMasterScheduleAsJson(masterStudentInput)
                        .addOnSuccessListener(onSuccess)
                        .addOnFailureListener(onFailure);
            }
            else {
                throw new Exception("Something went wrong !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
