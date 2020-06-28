package org.softastur.asturiandictionary.entries;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.softastur.asturiandictionary.ConjugationAdapter;
import org.softastur.asturiandictionary.R;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.softastur.asturiandictionary.entries.Entry.AWAITING_QUERY;
import static org.softastur.asturiandictionary.entries.Entry.ENTRY_LOADED;
import static org.softastur.asturiandictionary.entries.Entry.LOADING_ENTRY;

/**
 * Created by guifa on 5/29/17.
 */

public class Forms extends android.support.v4.app.Fragment implements Entry.EntryReceiver {
    JSONObject data;
    Entry entry;
    boolean LOG = true;

    View loadingDisplay;
    View nounDisplay;
    View adjectiveDisplay;
    View verbDisplay;
    View notApplicableDisplay;

    private boolean landscape;

    private ConjugationAdapter indicative;
    private ConjugationAdapter subjunctive;
    private ConjugationAdapter potential;

    public static Forms newInstance() {
        return Forms.newInstanceWithData("");
    }

    public static Forms newInstanceWithData(String data) {
        Forms fragment = new Forms();
        Bundle args = new Bundle();
        args.putString("data",data);
        fragment.setArguments(args);
        return fragment;
    }

    // vanity method to in case it ends up being easier this way
    public static Forms newInstanceWithData(JSONObject data) {
        return newInstanceWithData(data.toString());
    }

    public Forms() {
        // Empty constructor
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        entry = (Entry) getParentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            try {
                data = new JSONObject(savedInstanceState.getString("DATA"));
            } catch (JSONException e) {
                // we should never get here, but it's a good ultimate fallback
                //data = entry.getFormsData();
            }
        }else if (getArguments() != null) {
            try {
                data = new JSONObject(getArguments().getString("DATA"));
            } catch (JSONException e) {
                // we should never get here, but it's a good ultimate fallback
                //data = entry.getFormsData();
            }
        }
        //preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        //editor = preferences.edit();

        indicative = new ConjugationAdapter(getContext(),R.layout.conjugation_table_2x3,new ArrayList());
        subjunctive = new ConjugationAdapter(getContext(),R.layout.conjugation_table_2x3,new ArrayList());
        potential = new ConjugationAdapter(getContext(),R.layout.conjugation_table_2x3,new ArrayList());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        landscape = getResources().getBoolean(R.bool.is_landscape);
        return inflater.inflate(R.layout.fragment_entry_forms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        _log(0,".onViewCreated()");
        nounDisplay = view.findViewById(R.id.nounScroll);
        verbDisplay = view.findViewById(R.id.verbScroll);
        adjectiveDisplay = view.findViewById(R.id.adjectiveScroll);
        loadingDisplay = view.findViewById(R.id.loading);
        notApplicableDisplay = view.findViewById(R.id.not_applicable);
        updateDisplay();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("asturianu - Forms.onSaveInstanceState();");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("DATA",data == null ? "" : data.toString());
    }

    private void updateDisplay() {
        if (nounDisplay == null || verbDisplay == null || adjectiveDisplay == null ||
                loadingDisplay == null || notApplicableDisplay == null) {
            _log(0,".updateDisplay(); "
                    + (nounDisplay == null ? "nounDisplay == null; " : "")
                    + (verbDisplay == null ? "verbDisplay == null; " : "")
                    + (adjectiveDisplay == null ? "adjectiveDisplay == null; " : "")
                    + (loadingDisplay == null ? "loadingDisplay == null; " : "")
                    + (notApplicableDisplay == null ? "notApplicableDisplay == null; " : "")
            );
            return;
        }

        switch(entry.getStatus()) {
            case AWAITING_QUERY:
                _log(0,"Definitions.updateDisplay()->AwaitingQuery");
                // viewpager that contains this fragment will be hidden,
                // so we don't need to do anything at all
                break;

            case LOADING_ENTRY:
                _log(0,".updateDisplay()->LoadingEntry");
                nounDisplay.setVisibility(GONE);
                adjectiveDisplay.setVisibility(GONE);
                verbDisplay.setVisibility(GONE);
                notApplicableDisplay.setVisibility(GONE);
                loadingDisplay.setVisibility(VISIBLE);
                break;

            case ENTRY_LOADED:
                _log(0,".updateDisplay()->EntryLoaded; pos = " + entry.getData().optInt("pos",0));
                switch(entry.getData().optInt("pos",0)) {
                    case 2:
                        showVerbForms();
                        verbDisplay.setVisibility(VISIBLE);
                        break;
                    case 1: // nouns
                    case 3: // adjectives
                    default: // all else
                        notApplicableDisplay.setVisibility(VISIBLE);
                }
                loadingDisplay.setVisibility(GONE);

            //textDisplay.setText(formatDefinitions());
                break;
            default:
        }

    }

    private void showVerbForms() {
        if(getView() == null) {return;}
        LinearLayout layout = (LinearLayout)getView().findViewById(R.id.verb_forms);

        data = entry.getData().optJSONObject("forms");


        View ind_pres = getConjugationView(R.string.present,data.optJSONObject("pres_ind"));
        View ind_pret = getConjugationView(R.string.indefinite_preterite,data.optJSONObject("pret_ind"));
        View ind_imp = getConjugationView(R.string.imperfect_preterite,data.optJSONObject("imp_ind"));
        View ind_plup = getConjugationView(R.string.pluperfect,data.optJSONObject("plup_ind"));

        View sub_pres = getConjugationView(R.string.present,data.optJSONObject("pres_sub"));
        View sub_imp = getConjugationView(R.string.imperfect_preterite,data.optJSONObject("imp_sub"));

        View pot_fut = getConjugationView(R.string.future,data.optJSONObject("pres_pot"));
        View pot_cond = getConjugationView(R.string.conditional,data.optJSONObject("past_pot"));


        if(landscape) {
            LinearLayout indicativeA = new LinearLayout(getContext());
            LinearLayout indicativeB = new LinearLayout(getContext());
            LinearLayout subjunctive = new LinearLayout(getContext());
            LinearLayout potential = new LinearLayout(getContext());

            indicativeA.setOrientation(LinearLayout.HORIZONTAL);
            indicativeB.setOrientation(LinearLayout.HORIZONTAL);
            subjunctive.setOrientation(LinearLayout.HORIZONTAL);
            potential.setOrientation(LinearLayout.HORIZONTAL);

            indicativeA.addView(ind_pres);
            indicativeA.addView(ind_pret);

            indicativeB.addView(ind_imp);
            indicativeB.addView(ind_plup);

            subjunctive.addView(sub_pres);
            subjunctive.addView(sub_imp);

            potential.addView(pot_fut);
            potential.addView(pot_cond);

            layout.addView(indicativeA,2);
            layout.addView(indicativeB,3);
            layout.addView(subjunctive,5);
            layout.addView(potential,7);

        }else{
            // vertically only
            layout.addView(ind_pres,2);
            layout.addView(ind_pret,3);
            layout.addView(ind_imp ,4);
            layout.addView(ind_plup,5);

            layout.addView(sub_pres,7);
            layout.addView(sub_imp ,8);

            layout.addView(pot_fut ,10);
            layout.addView(pot_cond,11);
        }
        /*layout.addView(
                getConjugationView(R.string.present,data.optJSONObject("pres_ind")),
                2
        );
        layout.addView(
                getConjugationView(R.string.indefinite_preterite,data.optJSONObject("pret_ind")),
                3
        );
        layout.addView(
                getConjugationView(R.string.imperfect_preterite,data.optJSONObject("imp_ind")),
                4
        );
        layout.addView(
                getConjugationView(R.string.pluperfect,data.optJSONObject("plup_ind")),
                5
        );
        layout.addView(
                getConjugationView(R.string.present,data.optJSONObject("pres_sub")),
                7
        );
        layout.addView(
                getConjugationView(R.string.imperfect_preterite,data.optJSONObject("imp_sub")),
                8
        );
        layout.addView(
                getConjugationView(R.string.future,data.optJSONObject("pres_pot")),
                10
        );
        layout.addView(
                getConjugationView(R.string.conditional,data.optJSONObject("past_pot")),
                11
        );*/

    }

    private View getConjugationView(int label, JSONObject data) {
        data = data != null ? data : new JSONObject();

        View view = getActivity().getLayoutInflater().inflate(R.layout.conjugation_table_2x3,null);

        ((TextView)view.findViewById(R.id.label)).setText(getResources().getString(label));

        ((TextView)view.findViewById(R.id.first_singular)).setText(stringFromJSON(data.optJSONArray("1")));
        ((TextView)view.findViewById(R.id.second_singular)).setText(stringFromJSON(data.optJSONArray("2")));
        ((TextView)view.findViewById(R.id.third_singular)).setText(stringFromJSON(data.optJSONArray("3")));
        ((TextView)view.findViewById(R.id.first_plural)).setText(stringFromJSON(data.optJSONArray("4")));
        ((TextView)view.findViewById(R.id.second_plural)).setText(stringFromJSON(data.optJSONArray("5")));
        ((TextView)view.findViewById(R.id.third_plural)).setText(stringFromJSON(data.optJSONArray("6")));

        return view;
    }

    private static final String SEPARATOR = "\n";
    private String stringFromJSON(JSONArray array) {
        if(array == null) return "";

        int length = array.length();
        String result = array.optString(0,"");

        // old style for because we need to use i
        for(int i = 1; i < length; i++) result += SEPARATOR + array.optString(i,"");

        return result;
    }

    public void update() {
        updateDisplay();
    }
    int id = 0;
    public void setID(int i) {
        id = i;
    }
    private void _log(int l, String s) {
        if(LOG) {
            switch(l) {
                case 0: Log.v("asturianu - [" + id + "] Forms.",s); break;
                case 1: Log.d("asturianu - [" + id + "] Forms.",s); break;
                case 2: Log.w("asturianu - [" + id + "] Forms.",s); break;
                case 3: Log.e("asturianu - [" + id + "] Forms.",s); break;
            }
        }
    }
}

