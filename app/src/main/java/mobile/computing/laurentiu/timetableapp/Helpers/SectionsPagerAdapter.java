package mobile.computing.laurentiu.timetableapp.Helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.text.ParseException;

import mobile.computing.laurentiu.timetableapp.Constants;
import mobile.computing.laurentiu.timetableapp.SQLite.DatabaseHelper;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private DatabaseHelper dbContext;

    public SectionsPagerAdapter(FragmentManager fm, DatabaseHelper dbContext) {
        super(fm);
        this.dbContext = dbContext;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment item = null;

        try {
            item = PlaceholderFragment.newInstance(position + 1, dbContext);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return item;
    }

    @Override
    public int getCount() {
        return Constants.NUMBER_OF_DAYS_IN_YEAR;
    }
}
