package org.softastur.asturiandictionary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.softastur.asturiandictionary.ConjugationAdapter.ConjugationTable;
import java.util.ArrayList;


public class DictionaryConjugationView extends AppCompatActivity implements GenericDownloader.DownloaderCallback {

    private ConjugationAdapter indicative;
    private ConjugationAdapter subjunctive;
    private ConjugationAdapter potential;

    int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_conjugation_view);
        //  getActionBar().hide();
        //getSupportActionBar().hide();

        mId = getIntent().getIntExtra("id",0);

        if(mId == 0) { // bail
        }

        GenericDownloader downloader = new GenericDownloader();
        downloader.setCallback(this);
        downloader.setTag(0);
        downloader.execute("http://asturianu.elahorcado.net/view_conjugations_json.php?id=" + mId);

        indicative = new ConjugationAdapter(this,R.layout.conjugation_table_2x3,new ArrayList());
        subjunctive = new ConjugationAdapter(this,R.layout.conjugation_table_2x3,new ArrayList());
        potential = new ConjugationAdapter(this,R.layout.conjugation_table_2x3,new ArrayList());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dictionary_conjugation_view, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRetrieveData(String data, int tag, long time) {
        try {
            populateTables2(new JSONObject(data));

            findViewById(R.id.loading).setVisibility(View.GONE);
            findViewById(R.id.conjugations).setVisibility(View.VISIBLE);
        }catch(JSONException e) {

        }

    }

    private void populateTables2(JSONObject data) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.conjugations);

        layout.addView(
                getConjugationView(getResources().getString(R.string.present),data.optJSONObject("pres_ind")),
                2
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.indefinite_preterite),data.optJSONObject("pret_ind")),
                3
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.imperfect_preterite),data.optJSONObject("imp_ind")),
                4
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.pluperfect),data.optJSONObject("plup_ind")),
                5
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.present),data.optJSONObject("pres_sub")),
                7
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.imperfect_preterite),data.optJSONObject("imp_sub")),
                8
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.future),data.optJSONObject("pres_pot")),
                10
        );
        layout.addView(
                getConjugationView(getResources().getString(R.string.conditional),data.optJSONObject("past_pot")),
                11
        );



    }


    private void populateTables(JSONObject data) {
        JSONObject pres_ind = data.optJSONObject("pres_ind");
        JSONObject pret_ind = data.optJSONObject("pret_ind");
        JSONObject imp_ind  = data.optJSONObject("imp_ind");
        JSONObject plup_ind = data.optJSONObject("plup_ind");
        JSONObject pres_sub = data.optJSONObject("pres_sub");
        JSONObject imp_sub  = data.optJSONObject("imp_sub");
        JSONObject pres_pot = data.optJSONObject("pres_pot");
        JSONObject pret_pot = data.optJSONObject("pret_pot");


        indicative.add(
                new ConjugationTable(
                        getResources().getString(R.string.present),
                        stringArrayFromJSON(pres_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(pres_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(pres_ind.optJSONArray("3rd_p"))
                )
        );

        indicative.add(
                new ConjugationTable(
                        getResources().getString(R.string.indefinite_preterite),
                        stringArrayFromJSON(pret_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(pret_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(pret_ind.optJSONArray("3rd_p"))
                )
        );

        indicative.add(
                new ConjugationTable(
                        getResources().getString(R.string.imperfect_preterite),
                        stringArrayFromJSON(imp_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(imp_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(imp_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(imp_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(imp_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(imp_ind.optJSONArray("3rd_p"))
                )
        );

        indicative.add(
                new ConjugationTable(
                        getResources().getString(R.string.pluperfect),
                        stringArrayFromJSON(plup_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(plup_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(plup_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(plup_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(plup_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(plup_ind.optJSONArray("3rd_p"))
                )
        );

        subjunctive.add(
                new ConjugationTable(
                        getResources().getString(R.string.present),
                        stringArrayFromJSON(pres_sub.optJSONArray("1st_s")),
                        stringArrayFromJSON(pres_sub.optJSONArray("2nd_s")),
                        stringArrayFromJSON(pres_sub.optJSONArray("3rd_s")),
                        stringArrayFromJSON(pres_sub.optJSONArray("1st_p")),
                        stringArrayFromJSON(pres_sub.optJSONArray("2nd_p")),
                        stringArrayFromJSON(pres_sub.optJSONArray("3rd_p"))
                )
        );
        subjunctive.add(
                new ConjugationTable(
                        getResources().getString(R.string.imperfect_preterite),
                        stringArrayFromJSON(imp_sub.optJSONArray("1st_s")),
                        stringArrayFromJSON(imp_sub.optJSONArray("2nd_s")),
                        stringArrayFromJSON(imp_sub.optJSONArray("3rd_s")),
                        stringArrayFromJSON(imp_sub.optJSONArray("1st_p")),
                        stringArrayFromJSON(imp_sub.optJSONArray("2nd_p")),
                        stringArrayFromJSON(imp_sub.optJSONArray("3rd_p"))
                )
        );
        potential.add(
                new ConjugationTable(
                        getResources().getString(R.string.future),
                        stringArrayFromJSON(pres_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(pres_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(pres_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(pres_ind.optJSONArray("3rd_p"))
                )
        );
        potential.add(
                new ConjugationTable(
                        getResources().getString(R.string.conditional),
                        stringArrayFromJSON(pret_ind.optJSONArray("1st_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("2nd_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("3rd_s")),
                        stringArrayFromJSON(pret_ind.optJSONArray("1st_p")),
                        stringArrayFromJSON(pret_ind.optJSONArray("2nd_p")),
                        stringArrayFromJSON(pret_ind.optJSONArray("3rd_p"))
                )
        );

    }

    private String[] stringArrayFromJSON(JSONArray array) {
        if(array == null) return new String[]{};
        int length = array.length();
        String[] result = new String[length];
        for(int i = 0; i < length; i++) result[i] = array.optString(i,"");
        return result;
    }

    private static final String SEPARATOR = "\n";
    private String stringFromJSON(JSONArray array) {
        if(array == null) return "";

        int length = array.length();
        String result = array.optString(0,"");

        for(int i = 1; i < length; i++) result += SEPARATOR + array.optString(i,"");

        return result;
    }

    private View getConjugationView(String label, JSONObject data) {
        label = label != null ? label : "";
        data = data != null ? data : new JSONObject();

        View view = getLayoutInflater().inflate(R.layout.conjugation_table_2x3,null);

        ((TextView)view.findViewById(R.id.label)).setText(label);

        ((TextView)view.findViewById(R.id.first_singular)).setText(stringFromJSON(data.optJSONArray("1st_s")));
        ((TextView)view.findViewById(R.id.second_singular)).setText(stringFromJSON(data.optJSONArray("2nd_s")));
        ((TextView)view.findViewById(R.id.third_singular)).setText(stringFromJSON(data.optJSONArray("3rd_s")));
        ((TextView)view.findViewById(R.id.first_plural)).setText(stringFromJSON(data.optJSONArray("1st_p")));
        ((TextView)view.findViewById(R.id.second_plural)).setText(stringFromJSON(data.optJSONArray("2nd_p")));
        ((TextView)view.findViewById(R.id.third_plural)).setText(stringFromJSON(data.optJSONArray("3rd_p")));

        return view;
    }
}
