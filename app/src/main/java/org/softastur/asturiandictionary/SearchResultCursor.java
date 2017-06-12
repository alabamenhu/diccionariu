package org.softastur.asturiandictionary;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by guifa on 5/7/15.
 */
public class SearchResultCursor extends MatrixCursor {

    static String[] partOfSpeech = new String[]{"","n.","v.","alv.","adj.","prep.","pron.","conj.","apoc.","art.","interj.","contr."};

    public static void setLocalizedPartsofSpeech(String[] strings) {
        partOfSpeech = strings;
    }

    public SearchResultCursor(String data) {
        super(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2}, 10);
        if(data != null) processData(data);
    }

    public void processData(String data) {
        System.out.println("asturianu searchresultcursor gotData: " + data);
        try {
            JSONObject jsonData = new JSONObject(data);
            JSONArray exactMatches = jsonData.optJSONArray("exact");
            JSONArray fuzzyMatches = jsonData.optJSONArray("fuzzy");

            if(exactMatches != null) {
                for(int i = 0; i < exactMatches.length(); i++) {
                    addRow(
                            new Object[]{
                                    exactMatches.getJSONObject(i).optInt("id", 0),
                                    exactMatches.getJSONObject(i).optString("lede", "foo"),
                                    partOfSpeech[
                                            exactMatches.getJSONObject(i).optInt("pos",0)
                                    ]
                            }
                    );
                }
            }

            if(fuzzyMatches != null) {
                for (int i = 0; i < fuzzyMatches.length(); i++) {
                    addRow(
                            new Object[]{
                                    fuzzyMatches.getJSONObject(i).optInt("id", 0),
                                    fuzzyMatches.getJSONObject(i).optString("lede", "bar"),
                                    partOfSpeech[
                                            fuzzyMatches.getJSONObject(i).optInt("pos",0)
                                    ]
                            }
                    );
                }
            }

        }catch(JSONException e) {
        }

    }
}
