package mobile.computing.laurentiu.timetableapp.Helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mobile.computing.laurentiu.timetableapp.Constants;
import mobile.computing.laurentiu.timetableapp.R;
import mobile.computing.laurentiu.timetableapp.SQLite.DatabaseHelper;
import mobile.computing.laurentiu.timetableapp.SQLite.ScheduleDay;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_DAY = "day";
    private static final String ARG_WEEKDAY = "weekday";
    private static final String ARG_PARITY = "parity";
    private static final String ARG_SCHEDULE = "schedule";

    public static PlaceholderFragment newInstance(int sectionNumber,
                                                  DatabaseHelper dbContext) throws ParseException {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        SimpleDateFormat dayFormat = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault());
        SimpleDateFormat weekdayFormat = new SimpleDateFormat(
                "EEEE",
                Locale.getDefault());

        Calendar calendar = getCurrentDateForPosition(sectionNumber);
        String weekday = weekdayFormat.format(calendar.getTime());
        String parity = dbContext.getParityFromDate(calendar.getTime());

        args.putString(ARG_DAY, dayFormat.format(calendar.getTime()));
        args.putString(ARG_WEEKDAY, weekday);
        args.putString(ARG_PARITY, parity);
        args.putString(ARG_SCHEDULE, getScheduleHtmlString(weekday, parity, dbContext));
        fragment.setArguments(args);

        return fragment;
    }

    private static String getScheduleHtmlString(
            String weekday,
            String parity,
            DatabaseHelper dbContext) {
        List<ScheduleDay> schedules = dbContext.getSchedule(weekday, parity);

        String scheduleHtmlString = "";

        for (ScheduleDay schedule : schedules) {
            String startHour = schedule.getStartHour() == 8 || schedule.getStartHour() == 9
                    ? "0" + String.valueOf(schedule.getStartHour())
                    : String.valueOf(schedule.getStartHour());

            scheduleHtmlString += String.format(
                    "<b>%s</b>\t-\t<b>%s</b>\t|\t%s\t|\t%s\t|\t%s<br><br>",
                    startHour,
                    schedule.getEndHour(),
                    schedule.getLocation(),
                    schedule.getActivityType(),
                    schedule.getShortCourseName());
        }

        if(schedules.isEmpty()) {
            scheduleHtmlString = "Nothing for today! Enjoy the <b><u>free time</u></b> !!";
        }

        return scheduleHtmlString;
    }

    private static Calendar getCurrentDateForPosition(int sectionNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());

        // we want the current date to be in the middle of the year
        // so we calculate the positive/negative difference when we swipe
        int difference = sectionNumber - (Constants.NUMBER_OF_DAYS_IN_YEAR / 2);
        // after that we add it to our current date and this will
        // give us the date for a given item in the agenda when swiping
        calendar.add(Calendar.DATE, difference);

        return calendar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agenda, container, false);
        TextView textView = rootView.findViewById(R.id.section_label);

        String format = "<b>%s</b>, <b>%s</b> and this week is <b>%s</b><br><br><br>%s";
        String label = String.format(format,
                getArguments().getString(ARG_DAY),
                getArguments().getString(ARG_WEEKDAY),
                getArguments().getString(ARG_PARITY),
                getArguments().getString(ARG_SCHEDULE));
        textView.setText(Html.fromHtml(label));

        return rootView;
    }
}
