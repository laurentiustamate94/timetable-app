package mobile.computing.laurentiu.timetableapp.Services;

import android.app.Activity;

import com.google.android.gms.tasks.Task;

public interface IFirebaseService {

    void onCreate(Activity activity);

    boolean isInputBachelorValid(String studentInput);

    boolean isInputMasterValid(String studentInput);

    Task<byte[]> getBachelorScheduleAsJson(final String studentInput) throws Exception;

    Task<byte[]> getMasterScheduleAsJson(final String studentInput) throws Exception;
}
