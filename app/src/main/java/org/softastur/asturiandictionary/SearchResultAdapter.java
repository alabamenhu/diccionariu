package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guifa on 5/7/15.
 */
public class SearchResultAdapter extends ArrayAdapter implements DefinitionDownloader.DefinitionDownloaderCallback {

    Context mContext;
    ArrayList mResults = new ArrayList<SearchResult>();
    String[] mPartOfSpeech;
    String mQuery;
    volatile long mMostRecentResult = 0;

    public SearchResultAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        mContext = context;
        mPartOfSpeech = context.getResources().getStringArray(R.array.part_of_speech_abbreviations);
    }

    public void doQuery(String query) {
        DefinitionDownloader downloader = new DefinitionDownloader();
        downloader.setTag(0);
        downloader.setCallback(this);
        try{
            downloader.execute(URLEncoder.encode(query,"UTF-8"));
        }catch(UnsupportedEncodingException e) {}
        mQuery = query;
    }

    public synchronized void onRetrieveData(String data, int tag, long time) {
        if(time < mMostRecentResult) return;
        mMostRecentResult = time;
        clear();
        try {
            JSONObject jsonData = new JSONObject(data);
            JSONArray exactMatches = jsonData.optJSONArray("exact");
            JSONArray partialMatches = jsonData.optJSONArray("partial");
            JSONArray fuzzyMatches = jsonData.optJSONArray("fuzzy");

            if(exactMatches != null) {
                for(int i = 0; i < exactMatches.length(); i++) {
                    add(
                            new SearchResult(
                                    exactMatches.getJSONObject(i).optString("lede", "foo"),
                                    exactMatches.getJSONObject(i).optInt("id", 0),
                                    SearchResult.EXACT,
                                    exactMatches.getJSONObject(i).optInt("pos",0)
                            )
                    );
                }
            }

            if(partialMatches != null) {
                for(int i = 0; i < partialMatches.length(); i++) {
                    add(
                            new SearchResult(
                                    partialMatches.getJSONObject(i).optString("lede", "foo"),
                                    partialMatches.getJSONObject(i).optInt("id", 0),
                                    SearchResult.PARTIAL,
                                    partialMatches.getJSONObject(i).optInt("pos",0)
                            )
                    );
                }
            }

            if(fuzzyMatches != null) {
                for (int i = 0; i < fuzzyMatches.length(); i++) {
                    add(
                            new SearchResult(
                                    exactMatches.getJSONObject(i).optString("lede", "foo"),
                                    exactMatches.getJSONObject(i).optInt("id", 0),
                                    SearchResult.FUZZY,
                                    exactMatches.getJSONObject(i).optInt("pos", 0)
                            )
                    );
                }
            }

             System.out.println("asturianu there are " + getCount() + "results");
            if(getCount() == 0) {
                add(
                        new SearchResult(
                            mContext.getResources().getString(R.string.no_search_results),
                            0,
                            SearchResult.NO_MATCH,
                            0
                    )
                );
            }

        }catch(JSONException e) {
        }

//        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return (long)((SearchResult)getItem(position)).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.search_result_row, null);
        SearchResult searchResult = (SearchResult) getItem(position);

        TextView lede = (TextView)view.findViewById(R.id.lede);
        TextView id = (TextView)view.findViewById(R.id.entry_id);
        TextView pos = (TextView)view.findViewById(R.id.part_of_speech);

        if(searchResult.type == SearchResult.EXACT) {
            lede.setText(
                    new FormattedStringBuilder()
                    .append(
                            searchResult.lede,
                            new Object[]{new StyleSpan(Typeface.BOLD),new ForegroundColorSpan(0xffddbb00)}
                    )
                    .build()
            );
        }else if(searchResult.type == SearchResult.PARTIAL) {
            lede.setText(
                    new FormattedStringBuilder()
                            .append(
                                    searchResult.lede.substring(0,mQuery.length() < searchResult.lede.length() ? mQuery.length() : searchResult.lede.length()),
                                    new Object[]{
                                            new StyleSpan(Typeface.BOLD),
                                            new ForegroundColorSpan(mContext.getResources().getColor(R.color.asturian_blue))
                                    }
                            )
                            .append(
                                    searchResult.lede.substring(mQuery.length() < searchResult.lede.length() ? mQuery.length() : searchResult.lede.length()),
                                    new Object[]{
                                            new ForegroundColorSpan(0xff333333)
                                    }
                            )
                            .build()
            );
        }else if(searchResult.type == SearchResult.FUZZY){
            lede.setText(searchResult.lede);
        }else {
            lede.setText(
                    new FormattedStringBuilder()
                            .append(
                                    searchResult.lede,
                                    new Object[]{new ForegroundColorSpan(0xff660000)}
                            )
                            .build()
            );
            lede.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        }


        switch(searchResult.type) {
            case SearchResult.EXACT:
                lede.setTextColor(mContext.getResources().getColor(R.color.asturian_dark_blue));
                break;
            case SearchResult.FUZZY:
                lede.setTextColor(0xff000000);
                break;
        }

        id.setText(Integer.toString(searchResult.id));

        if(searchResult.type != SearchResult.NO_MATCH) {
            pos.setText(mPartOfSpeech[searchResult.partOfSpeech]);
        }else{
            pos.setText("");
        }

        return view;
    }



    private class SearchResult {
        public String lede;
        public int id;
        public int type;
        public int partOfSpeech;

        public static final int EXACT = 0;
        public static final int PARTIAL = 1;
        public static final int FUZZY = 2;
        public static final int NO_MATCH = 3;

        public SearchResult(String _lede, int _id, int _type, int _pos) {
            lede = _lede;
            id = _id;
            type = _type;
            partOfSpeech = _pos;
        }
    }



}
