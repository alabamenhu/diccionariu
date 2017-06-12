package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by guifa on 5/1/15.
 */
public class MultipleMatchAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener {
    Context context;
    JSONObject matches;
    JSONArray exact;
    JSONArray fuzzy;

    public MultipleMatchAdapter(Context context, int resource, JSONObject results) {
        super(context, resource, new ArrayList());
        try {
            matches = new JSONObject(results.toString());
            exact = matches.optJSONArray("exact");
            fuzzy = matches.optJSONArray("fuzzy");
            if(exact == null) { exact = new JSONArray(); }
            if(fuzzy == null) { fuzzy = new JSONArray(); }
        }catch(JSONException e) {}

        this.context = context;
    }

    public Object getItem(int index) {
        try {
            if (index < exact.length()) {
                return exact.getJSONObject(index);
            }else{
                return fuzzy.getJSONObject(index - exact.length());
            }
        }catch(JSONException e) {
            return new JSONObject();
        }
    }

    public int getCount() {
        return exact.length() + fuzzy.length();
    }

    public void resetWithJson(JSONObject results) {
        try {
            matches = new JSONObject(results.toString());
            exact = matches.optJSONArray("exact");
            fuzzy = matches.optJSONArray("fuzzy");
            if(exact == null) { exact = new JSONArray(); }
            if(fuzzy == null) { fuzzy = new JSONArray(); }
        }catch(JSONException e) {}
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.search_result_row, null);
        JSONObject data = (JSONObject) getItem(position);



        ((TextView)view.findViewById(R.id.entry_id)).setText(
                Integer.toString(
                        data.optInt("id", 0)
                )
        );
        ((TextView)view.findViewById(R.id.lede)).setText(
                data.optString("lede", "")
        );

        String[] pos = context.getResources().getStringArray(R.array.part_of_speech_abbreviations);
        ((TextView)view.findViewById(R.id.part_of_speech)).setText(
                pos[data.optInt("pos",0)]
        );

        return view;
    }








    public void onItemClick(AdapterView adapterView, View view, int index, long fubar) {

    }

}
