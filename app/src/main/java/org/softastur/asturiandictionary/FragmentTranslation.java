package org.softastur.asturiandictionary;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentTranslation.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentTranslation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTranslation
        extends
            Fragment
        implements
        GenericDownloader.DownloaderCallback,
            View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String ESLEMA_BEGIN_TAG = "name=\"texto_trad\">";
    private String ESLEMA_END_TAG = "</textarea>";

    private static final int ASTURIAN_TO_CASTILIAN = 0;
    private static final int CASTILIAN_TO_ASTURIAN = 1;

    private int direction = CASTILIAN_TO_ASTURIAN;

    private Button translationDirection;
    private Button translateButton;
    private TextView originalTextView;
    private TextView translatedTextView;
    private LinearLayout loadingIndicator;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentTranslation.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTranslation newInstance(String param1, String param2) {
        FragmentTranslation fragment = new FragmentTranslation();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentTranslation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void onRetrieveData(String data, int tag, long time) {
        String result = "";
        System.out.print(data);
        try {
            int start =  data.indexOf(ESLEMA_BEGIN_TAG) + ESLEMA_BEGIN_TAG.length();
            int end = data.indexOf(ESLEMA_END_TAG,start);
            result = data.substring(start,end);
        }catch(Exception e) {
        }
        loadingIndicator.setVisibility(View.GONE);
        translatedTextView.setText(Html.fromHtml(result));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_translation, container, false);

        translateButton = (Button) view.findViewById(R.id.translate_button);
        translateButton.setOnClickListener(this);

        translationDirection = (Button) view.findViewById(R.id.translation_direction);
        translationDirection.setOnClickListener(this);

        originalTextView = (TextView) view.findViewById(R.id.text_to_translate);
        translatedTextView = (TextView) view.findViewById(R.id.translated_text);
        loadingIndicator = (LinearLayout) view.findViewById(R.id.loading_translation);
        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    public void onClick(View view) {
        System.out.println("asturianu - got aclick in the translation fragment");
        if(view.getId() == R.id.translation_direction) {
            if(direction == ASTURIAN_TO_CASTILIAN) {
                direction = CASTILIAN_TO_ASTURIAN;
            }else{
                direction = ASTURIAN_TO_CASTILIAN;
            }

            switch(direction) {
                case ASTURIAN_TO_CASTILIAN:
                    translationDirection.setText(getResources().getString(R.string.translate_asturian_to_castilian));
                    break;
                case CASTILIAN_TO_ASTURIAN:
                    translationDirection.setText(getResources().getString(R.string.translate_castilian_to_asturian));
                    break;
                default:
            }

        }else if(view.getId() == R.id.translate_button) {
            doTranslation();

        }

    }

    public void onTranslationFragmentClick(View view) {
        onClick(view);
    }

    private void doTranslation() {

        loadingIndicator.setVisibility(View.VISIBLE);
        translatedTextView.setText("");

        String translationType;
        switch(direction) {
            case ASTURIAN_TO_CASTILIAN:
                translationType = "ast-es";
                break;
            case CASTILIAN_TO_ASTURIAN:
                translationType = "es-ast";
                break;
            default:
                translationType = "es-ast";
        }

        GenericDownloader downloader = new GenericDownloader();
        downloader.setCallback(this);
        downloader.setTag(0);
        downloader.addPostParameter("referrer", "traductor.php");
        downloader.addPostParameter("unknown", "off.php");
        downloader.addPostParameter("texto", originalTextView.getText().toString() );
        downloader.addPostParameter("url","");
        downloader.addPostParameter("file","");
        downloader.addPostParameter("output",translationType);
        downloader.addPostParameter("submit","Traducir");

        downloader.execute("http://di098.edv.uniovi.es/apertium/comun/script_ejecuta.php");

        System.out.println("asturianu - sent off translation request");
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
