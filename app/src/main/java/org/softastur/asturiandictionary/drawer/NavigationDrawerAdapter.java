package org.softastur.asturiandictionary.drawer;

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

import org.softastur.asturiandictionary.R;

import java.util.ArrayList;

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
        //addDrawerItem("",0,0,true);
    }

    @Override
    public boolean isEnabled(int position) {
        return ((DrawerItem)getItem(position)).active;
    }

    @Deprecated
    public void addDrawerItem(String label, int id, int icon, boolean active, int type) {
        add(new DrawerItem(label,id,icon,active,type));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view;
        DrawerItem item = (DrawerItem)getItem(position);
        if(item.type == DrawerItem.HEADER_ROW) {
            view = inflater.inflate(R.layout.navigation_drawer_row_title, parent, false);
        }else if(item.type == DrawerItem.SEPARATOR_SPACE_ROW) {
            view = inflater.inflate(R.layout.navigation_drawer_row_spacer, parent, false);
        }else if(item.type == DrawerItem.SEPARATOR_LINE_ROW) {
            view = inflater.inflate(R.layout.navigation_drawer_row_divider, parent, false);
        }else {
            view = inflater.inflate(R.layout.navigation_drawer_row, parent, false);
            TextView label = (TextView)view.findViewById(R.id.label);
            ImageView icon = (ImageView)view.findViewById(R.id.icon);

            if (item.active) view.findViewById(R.id.warning).setVisibility(View.INVISIBLE);

            label.setText(item.label);

            icon.setImageResource(item.icon);
            ((ImageView) view.findViewById(R.id.warning)).setColorFilter(
                    0xffff9900,
                    PorterDuff.Mode.SRC_ATOP
            );


            if (position == selectedIndex) {
                label.setTypeface(null, Typeface.BOLD);
                label.setTextColor(mContext.getResources().getColor(R.color.asturian_blue));
                icon.setColorFilter(
                        mContext.getResources().getColor(R.color.foreground_contrast),
                        PorterDuff.Mode.SRC_ATOP
                );
                view.setBackgroundColor(
                        mContext.getResources().getColor(R.color.background_popup_highlight)
                );
            } else {
                icon.setColorFilter(
                        mContext.getResources().getColor(R.color.foreground_secondary),
                        PorterDuff.Mode.SRC_ATOP
                );
            }
        }
        return view;
    }

    public void addHeaderRow() {
        add(new DrawerItem("",0,0,false,DrawerItem.HEADER_ROW));
    }
    public void addSpacerRow() {
        add(new DrawerItem("",0,0,false,DrawerItem.SEPARATOR_SPACE_ROW));
    }
    public void addDividerRow() {
        add(new DrawerItem("",0,0,false,DrawerItem.SEPARATOR_LINE_ROW));
    }

    public void addIconTextRow(String label, int id, int icon, boolean active) {
        add(
                new DrawerItem(label,id,icon,active,DrawerItem.ICON_TEXT_ROW)
        );
    }

    public class DrawerItem {
        final String label;
        final int id;
        final int icon;
        final boolean active;
        final int type;
        final static int HEADER_ROW = 0;
        final static int SEPARATOR_LINE_ROW = 1;
        final static int SEPARATOR_SPACE_ROW = 2;
        final static int ICON_TEXT_ROW = 3;

        public DrawerItem (String label, int id, int icon, boolean active, int type) {
            this.label = label;
            this.id = id;
            this.icon = icon;
            this.active = active;
            this.type = type;
        }
    }
}
