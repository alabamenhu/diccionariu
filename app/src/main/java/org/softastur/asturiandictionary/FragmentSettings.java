package org.softastur.asturiandictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSettings.OnSettingsFragmentListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSettings extends Fragment implements RadioGroup.OnCheckedChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String KEY_PREF_OVERRIDE_LANGUAGE = "org.softastur.dictionary.overrideLanguage_boolean";
    public static final String KEY_PREF_NIGHTMODE = "org.softastur.dictionary.nightmode_int";
    public static final int VALUE_PREF_NIGHTMODE_SYSTEM = 0;
    public static final int VALUE_PREF_NIGHTMODE_AUTO = 1;
    public static final int VALUE_PREF_NIGHTMODE_DAY = 2;
    public static final int VALUE_PREF_NIGHTMODE_NIGHT = 3;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnSettingsFragmentListener listener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSettings newInstance(String param1, String param2) {
        FragmentSettings fragment = new FragmentSettings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        boolean overrideLanguage = sharedPref.getBoolean(KEY_PREF_OVERRIDE_LANGUAGE, false);
        RadioGroup radios = (RadioGroup) view.findViewById(R.id.radio_language);
        radios.check(
                overrideLanguage
                        ? R.id.radio_asturian
                        : R.id.radio_system
        );
        radios.setOnCheckedChangeListener(this);

        int nightmode = sharedPref.getInt(KEY_PREF_NIGHTMODE,VALUE_PREF_NIGHTMODE_SYSTEM);
        radios = (RadioGroup) view.findViewById(R.id.radio_nightmode);
        int current;
        switch(nightmode) {
            case VALUE_PREF_NIGHTMODE_SYSTEM:
                current = R.id.radio_system;
                break;
            case VALUE_PREF_NIGHTMODE_NIGHT:
                current = R.id.radio_night;
                break;
            case VALUE_PREF_NIGHTMODE_DAY:
                current = R.id.radio_day;
                break;
            case VALUE_PREF_NIGHTMODE_AUTO:
            default:
                current = R.id.radio_auto;
        }
        radios.check(current);
        radios.setOnCheckedChangeListener(this);
        return view;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (listener != null) {
            listener.onNeedsRestart();
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            listener = (OnSettingsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSettingsFragmentListener {
        // TODO: Update argument type and name
        public void onNeedsRestart();
    }

    public void onCheckedChanged(RadioGroup group, int id ) {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(group.getId() == R.id.radio_language) {
            boolean newValue;
            if (id == R.id.radio_system) {
                newValue = false;
                editor.putBoolean(KEY_PREF_OVERRIDE_LANGUAGE, false);
            } else if (id == R.id.radio_asturian) {
                newValue = true;
                editor.putBoolean(KEY_PREF_OVERRIDE_LANGUAGE, true);
            }
            editor.commit();

            if (listener != null) {
                listener.onNeedsRestart();
            }
        }else if (group.getId() == R.id.radio_nightmode) {
            int newValue;
            if (id == R.id.radio_night) {
                newValue = VALUE_PREF_NIGHTMODE_NIGHT;
            } else if (id == R.id.radio_day) {
                newValue = VALUE_PREF_NIGHTMODE_DAY;
            } else if (id == R.id.radio_auto) {
                newValue = VALUE_PREF_NIGHTMODE_AUTO;
            } else {
                newValue = VALUE_PREF_NIGHTMODE_SYSTEM;
            }
            editor.putInt(KEY_PREF_NIGHTMODE, newValue);
            editor.commit();
            if (listener != null) {
                listener.onNeedsRestart();
            }

        }
    }
}
