package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by guifa on 5/7/15.
 */
public class FavoritesAdapter extends ArrayAdapter {

    Context mContext;
    String[] mPartOfSpeech;

    public FavoritesAdapter(Context context,int resource, List<FragmentFavorites.FavoriteEntry> entries) {
        super(context,resource,entries);
        mContext = context;
        mPartOfSpeech = context.getResources().getStringArray(R.array.part_of_speech_abbreviations);
    }

    @Override
    public long getItemId(int position) {
        return (long)((FragmentFavorites.FavoriteEntry)getItem(position)).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.favorites_row, null);

        FragmentFavorites.FavoriteEntry searchResult = (FragmentFavorites.FavoriteEntry) getItem(position);

        ((TextView)view.findViewById(R.id.entry_id)).setText(
                " "
        );
        ((TextView)view.findViewById(R.id.lede)).setText(
                searchResult.lede
        );
        ((TextView)view.findViewById(R.id.part_of_speech)).setText(
                mPartOfSpeech[searchResult.pos]
        );

        return view;
    }

}
