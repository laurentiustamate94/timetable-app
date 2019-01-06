package mobile.computing.laurentiu.timetableapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mobile.computing.laurentiu.timetableapp.SQLite.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        DatabaseHelper dbContext = new DatabaseHelper(getApplicationContext());

        if(!dbContext.isScheduleTableEmpty()) {
            Intent agendaIntent = new Intent(this, AgendaActivity.class);
            agendaIntent
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(agendaIntent);
            this.finish();
        }

        dbContext.close();
    }

    public void registerOnClick(View view) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
