package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentFavorites.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentFavorites#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentFavorites extends Fragment implements AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnLoadEntry mListener;

    private List<FavoriteEntry> favorites = new ArrayList<>();
    private FavoritesAdapter favoritesAdapter;

    public FragmentFavorites() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentFavorites.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentFavorites newInstance(String param1, String param2) {
        FragmentFavorites fragment = new FragmentFavorites();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    private void loadEntries() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Set<String> entriesSet = preferences.getStringSet("favorites", new HashSet<String>());
        String[] entries = entriesSet.toArray(new String[entriesSet.size()]);
        favorites.clear();
        for(String entry : entries) {
            favorites.add(new FavoriteEntry(entry));
        }
        Collections.sort(favorites,new FavoriteComparator());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        loadEntries();
        favoritesAdapter = new FavoritesAdapter(getActivity(),R.layout.favorites_row, favorites);
        AdapterView adapterView = (AdapterView) view.findViewById(R.id.favoritesList);
        adapterView.setAdapter(favoritesAdapter);
        adapterView.setOnItemClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onLoadEntry(1);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoadEntry) {
            mListener = (OnLoadEntry) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnLoadEntry");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnLoadEntry {
        // TODO: Update argument type and name
        void onLoadEntry(int id);
    }

    public class FavoriteEntry {
        public final int id;
        public final String lede;
        public final int pos;
        FavoriteEntry(String entry) {
            String elements[] = entry.split(",",3);
            id = Integer.parseInt(elements[0]);
            lede = elements[2];
            pos = Integer.parseInt(elements[1]);
        }

        FavoriteEntry(int _id, String _lede, int _pos) {
            id = _id;
            lede = _lede;
            pos = _pos;
        }

    }
    static class FavoriteComparator implements Comparator<FavoriteEntry>
    {
        // TODO ensure the order is correct according to Asturian sorting
        public int compare(FavoriteEntry c1, FavoriteEntry c2)
        {
            return c1.lede.compareTo(c2.lede);
        }
    }

    public void onItemClick(AdapterView view, View v, int item, long i) {
        FavoriteEntry entry = (FavoriteEntry) favoritesAdapter.getItem(item);
        mListener.onLoadEntry(entry.id);
    }
}
