package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guifa on 5/8/15.
 */
public class NavigationDrawerAdapter extends ArrayAdapter {

    int selectedIndex = 0;
    Context mContext;

    public void setSelectedIndex(int item) {
        selectedIndex = item;
        notifyDataSetChanged();
    }

    public NavigationDrawerAdapter(Context context, int resource, ArrayList<Object> objects) {
        super(context,resource,objects);
        mContext = context;
        addDrawerItem("",0,0,true);
    }

    public void addDrawerItem(String label, int id, int icon, boolean active) {
        add(new DrawerItem(label,id,icon,active));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view;
        if(position == 0) {
            view = inflater.inflate(R.layout.navigation_drawer_row_title, null);

        }else {
            view = inflater.inflate(R.layout.navigation_drawer_row, null);
            DrawerItem item = (DrawerItem) getItem(position);

            if (item.active) view.findViewById(R.id.warning).setVisibility(View.INVISIBLE);
            ((TextView) view.findViewById(R.id.label)).setText(item.label);
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(item.icon);

            ((ImageView) view.findViewById(R.id.warning)).setColorFilter(
                    0xffff9900,
                    PorterDuff.Mode.SRC_ATOP
            );


            if (position == selectedIndex) {
                ((TextView) view.findViewById(R.id.label)).setTypeface(null, Typeface.BOLD);
                ((TextView) view.findViewById(R.id.label)).setTextColor(mContext.getResources().getColor(R.color.asturian_blue));
                ((ImageView) view.findViewById(R.id.icon)).setColorFilter(
                        mContext.getResources().getColor(R.color.asturian_blue),
                        PorterDuff.Mode.SRC_ATOP
                );
                view.setBackgroundColor(0x15000000);
            } else {
                ((ImageView) view.findViewById(R.id.icon)).setColorFilter(
                        0xff555555,
                        PorterDuff.Mode.SRC_ATOP
                );
            }
        }
        return view;
    }

    public class DrawerItem {
        String label;
        int id;
        int icon;
        boolean active;

        public DrawerItem (String _label, int _id, int _icon, boolean _active) {
            label = _label;
            id = _id;
            icon = _icon;
            active = _active;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return position > 0;
    }
}
