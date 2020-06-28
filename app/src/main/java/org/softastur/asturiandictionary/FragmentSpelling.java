package org.softastur.asturiandictionary;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.softastur.asturianspellchecker.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSpelling.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSpelling#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSpelling extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Checker checker;

    private EditText original;
    private TextView corrected;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAbout.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSpelling newInstance(String param1, String param2) {
        FragmentSpelling fragment = new FragmentSpelling();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentSpelling() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        checker = new Checker(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spelling, container, false);
        Button checkButton = (Button) view.findViewById(R.id.correct_button);
        checkButton.setOnClickListener(this);

        original = (EditText) view.findViewById(R.id.text_to_correct);
        corrected = (TextView) view.findViewById(R.id.corrected_text);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
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

    public static final String valid = "a\u00e1a\u0301bcde\u00e9e\u0301fgh\u1e25h\u0323i\u00ed"
            + "i\u0301jkll\u1e37l\u0323mn\u00f1n\u0303o\u00f3o\u0301pqrstu\u00fau\u0301\u00fc"
            + "u\u0308vwxyzA\u00c1A\u0301BCDE\u00c9E\u0301FGH\u1e24H\u0323I\u00cdI\u0301JKLḶ"
            + "L\u0323MN\u00d1N\u0303O\u00d3O\u0301PQRSTU\u00daU\u0301\u00dcU\u0308VWXYZ-'\u2019";

    public void onClick(View view) {
        String originalText = original.getText().toString();
        FormattedStringBuilder result = new FormattedStringBuilder(true);
        StringBuilder builder = new StringBuilder("");
        boolean processingText = false;


        for(int i = 0, max = originalText.length(); i < max; i++) {
            String letter = originalText.substring(i,i+1);
            if(valid.indexOf(letter)>=0) {
                // it's a letter
                if(processingText) {
                    builder.append(letter);
                }else{
                    // first letter after punctuation
                    result.beginFormat(new Object[]{new ForegroundColorSpan(0xff000000)})
                            .append(builder.toString())
                            .endFormat();
                    builder = new StringBuilder(letter);
                    processingText = true;
                }
            }else{
                // it's not a letter
                if(processingText) {
                    // this is the first punctuation, so check spelling
                    String word = builder.toString();
                    builder = new StringBuilder(letter);
                    if(word.length() > 0) {
                        if(checker.getRoot(Checker.textToIntArray(word.toLowerCase()))) {
                            result.beginFormat(new Object[] {new ForegroundColorSpan(0xff000000)})
                                    .append(word)
                                    .endFormat();
                        }else{
                            result.beginFormat(new Object[] {new ForegroundColorSpan(0xffaa0000)})
                                    .append(word)
                                    .endFormat();
                        }
                    }
                    processingText = false;
                }else{
                    builder.append(letter);
                }
            }
        }

        corrected.setText(result.build());
    }

}
