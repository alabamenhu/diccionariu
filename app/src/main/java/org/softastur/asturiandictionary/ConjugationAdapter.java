package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guifa on 5/8/15.
 */
public class ConjugationAdapter extends ArrayAdapter {

    Context mContext;
    ArrayList mResults = new ArrayList<ConjugationTable>();
    String[] mPartOfSpeech;
    String mQuery;
    long mMostRecentResult = 0;

    public ConjugationAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        mContext = context;
        mPartOfSpeech = context.getResources().getStringArray(R.array.part_of_speech_abbreviations);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.conjugation_table_2x3, null);
        ConjugationTable table = (ConjugationTable) getItem(position);

        TextView first_singular  = (TextView) view.findViewById(R.id.first_singular);
        TextView second_singular = (TextView) view.findViewById(R.id.second_singular);
        TextView third_singular  = (TextView) view.findViewById(R.id.third_singular);
        TextView first_plural    = (TextView) view.findViewById(R.id.first_plural);
        TextView second_plural   = (TextView) view.findViewById(R.id.second_plural);
        TextView third_plural    = (TextView) view.findViewById(R.id.third_plural);
        TextView label           = (TextView) view.findViewById(R.id.label);

        first_singular.setText(TextUtils.join("\n", table.first_singular));
        second_singular.setText(TextUtils.join("\n",table.second_singular));
        third_singular.setText(TextUtils.join("\n", table.third_singular));
        first_plural.setText(TextUtils.join("\n", table.first_plural));
        second_plural.setText(TextUtils.join("\n", table.second_plural));
        third_plural.setText(TextUtils.join("\n", table.third_plural));

        label.setText(table.label);

/*        ArrayList<Integer> col1 = new ArrayList<Integer>();
        col1.add(Integer.valueOf(first_singular.getWidth()));
        col1.add(Integer.valueOf(second_singular.getWidth()));
        col1.add(Integer.valueOf(third_singular.getWidth()));

        int col1Width = maxInt(getTextViewWidth(first_singular),getTextViewWidth(second_singular), getTextViewWidth(third_singular));
        int col2Width = maxInt(getTextViewWidth(first_plural),getTextViewWidth(second_plural), getTextViewWidth(third_plural));

        int row1Height = maxInt(getTextViewHeight(first_singular),getTextViewHeight(first_plural));
        int row2Height = maxInt(getTextViewHeight(second_singular),getTextViewHeight(second_plural));
        int row3Height = maxInt(getTextViewHeight(third_singular),getTextViewHeight(third_plural));

        System.out.println("Sizes are C1:" + col1Width + " C2:" + col2Width);

        first_singular.setLayoutParams(new TableRow.LayoutParams(col1Width,row1Height));
        second_singular.setLayoutParams(new TableRow.LayoutParams(col1Width,row2Height));
        third_singular.setLayoutParams(new TableRow.LayoutParams(col1Width,row3Height));
        first_plural.setLayoutParams(new TableRow.LayoutParams(col2Width,row1Height));
        second_plural.setLayoutParams(new TableRow.LayoutParams(col2Width,row2Height));
        third_plural.setLayoutParams(new TableRow.LayoutParams(col2Width,row3Height));
*/
        return view;
    }
    public static class ConjugationTable {
        String label;
        String[] first_singular;
        String[] second_singular;
        String[] third_singular;
        String[] first_plural;
        String[] second_plural;
        String[] third_plural;

        public ConjugationTable(
                String _label,
                String[] _first_singular,
                String[] _second_singular,
                String[] _third_singular,
                String[] _first_plural,
                String[] _second_plural,
                String[] _third_plural
        ) {
            label = _label;
            first_singular = _first_singular;
            second_singular  = _second_singular;
            third_singular = _third_singular;
            first_plural = _first_plural;
            second_plural = _second_plural;
            third_plural = _third_plural;
        }
    }

    private int maxInt(int... numbers) {
        int max = 0;
        for(int number : numbers) {
            System.out.println(number);
            if(number > max) max = number;
        }
        return max;
    }

    public static int getTextViewHeight(TextView textView) {
        WindowManager wm = (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        }else{
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }
    public static int getTextViewWidth(TextView textView) {
        WindowManager wm = (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        }else{
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredWidth();
    }
}
