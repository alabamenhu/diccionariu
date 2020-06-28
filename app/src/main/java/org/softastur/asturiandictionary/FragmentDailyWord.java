package org.softastur.asturiandictionary;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import org.softastur.asturiandictionary.entries.Entry;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by guifa on 9/6/17.
 */

public class FragmentDailyWord extends Entry implements  DatePickerDialog.OnDateSetListener {
    private Calendar currentDate = Calendar.getInstance();
    private View dailyHeader;
    private TextView dateText;
    private View rightArrow;


    public static FragmentDailyWord newInstance(int action, int id) {
        FragmentDailyWord fragment = new FragmentDailyWord();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    private int getDailyWordID() {
        final int wordCount = 51829; // current maximum as of 22 August 2017; note that as things
        // increase it will be more difficult to maintain historical
        // consistency if people want to do a history of daily words
        final int month = currentDate.get(Calendar.MONTH) + 1; // zero base
        final int day = currentDate.get(Calendar.DAY_OF_MONTH);
        final int year = currentDate.get(Calendar.YEAR);

        // this calculation is 100% arbitrary,
        int id = (day * month * year) +
                (day * month) + (month * year) + (year * day) +
                day + month + year;

        id = (id % (wordCount-1)) + 1;
        System.out.println("asturianu - the daily ID for "+day+"/"+month+"/"+year+" is "+id);
        return id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("asturianu - Daily->onCreate() begins");
        super.onCreate(savedInstanceState);
        if(status == AWAITING_QUERY || status == LOADING_ENTRY) {
            status = NEEDS_TO_LOAD_ENTRY;
            wordID = getDailyWordID();
        }
        if(wordID == 0) {
            wordID = getDailyWordID();
        }
        System.out.println("asturianu - Daily->onCreate() ends");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("asturianu - Daily.onCreateView()");
        return inflater.inflate(R.layout.fragment_daily_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        // attach connections here

        dateText = (TextView) view.findViewById(R.id.date);
        dailyHeader = view.findViewById(R.id.daily);
        rightArrow = view.findViewById(R.id.right_arrow);
        view.findViewById(R.id.left_arrow).setOnClickListener(this);
        view.findViewById(R.id.right_arrow).setOnClickListener(this);
        view.findViewById(R.id.date).setOnClickListener(this);
        dailyWord();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) { // at the moment there are no other buttons, but this may change
            case R.id.favorite_button:
                onClickedFavorite();
                break;
            case R.id.left_arrow:
                adjustDate(-1);
                break;
            case R.id.right_arrow:
                adjustDate(1);
                break;
            case R.id.date:
                showDatePickerDialog();
                break;
            default:
        }
    }

    @Override
    public void updateView() {
        super.updateView();
        if(dateText != null) dateText.setText(getDateString());
        Calendar today = Calendar.getInstance();
        if(currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && currentDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
            if(rightArrow != null) rightArrow.setVisibility(View.INVISIBLE);
        }else{
            if(rightArrow != null) rightArrow.setVisibility(View.VISIBLE);
        }

    }
    @Override
    protected void setDisplay(int ignore) {
        // This will always show an entry, so we will always show the display.  In case any call
        // is accidentally made, we override it, although it shouldn't ever happen.
        super.setDisplay(DISPLAY_DICTIONARY);
    }

    private void adjustDate(int byDays) {
        currentDate.add(Calendar.DATE,byDays);
        Calendar today = Calendar.getInstance();
        if(today.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR)
                || (today.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
                        && today.get(Calendar.MONTH) < currentDate.get(Calendar.MONTH))
                || (today.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
                        && today.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)
                        && today.get(Calendar.DAY_OF_MONTH) < currentDate.get(Calendar.DAY_OF_MONTH))) {
            currentDate = today;
        }

        lazyLoad(getDailyWordID(),getString(R.string.entry_unknown_word),0);
        System.out.println("asturianu - adjusted the date by " + byDays);
    }

    public void dailyWord() {
        currentDate = Calendar.getInstance();
        lazyLoad(getDailyWordID(),getString(R.string.entry_unknown_word),0);
    }

    private String getDateString() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 && Locale.getDefault().toString().equals("ast")) {
            int day = currentDate.get(Calendar.DAY_OF_MONTH);
            int month = currentDate.get(Calendar.MONTH);
            int year = currentDate.get(Calendar.YEAR);
            String[] months = {" de xineru de "," de febreru de "," de marzu de ", " d’abril de "," de xunu de ",
                    " de xunetu de ", " d’agostu de ", " de setiembre de ", " d’ochobre de ", " de payares de ", " d’avientu de "};
            return day + months[month] + year;
        }

        String format;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMMdyyyy");
            System.out.println("asturianu: best format found to be " + format + " (locale: " + Locale.getDefault().toString() + ")");
        }else{
            format = "yyyy.MM.dd";
        }
        return DateFormat.format(format,currentDate).toString();
    }
    public void showDatePickerDialog() {
        DatePickerDialog date = new DatePickerDialog(getContext(),R.style.DatePickerStyle,this,currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH)+1,currentDate.get(Calendar.DAY_OF_MONTH));
        date.show();
    }

    public void onDateSet(DatePicker i, int y, int m, int d) {
        currentDate.set(y,m,d);
        lazyLoad(getDailyWordID(),getString(R.string.entry_unknown_word),0);
    }

}
