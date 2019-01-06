package mobile.computing.laurentiu.timetableapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class DoItYourselfActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_it_yourself);

        TextView mTextView = findViewById(R.id.text_view);
        mTextView.setClickable(true);
        mTextView.setMovementMethod (LinkMovementMethod.getInstance());
        mTextView.setText(Html.fromHtml("We currently do not have this schedule" +
                "<br><br><br>" +
                "You can contribute if you want to " +
                "<a href=\"https://github.com/laurentiustamate94/timetable-app\">our repo</a>" +
                "<br><br><br>" +
                "Thank you!"));
    }

    public void backToMainActivityOnClick(View view) {
        this.finish();
    }
}
