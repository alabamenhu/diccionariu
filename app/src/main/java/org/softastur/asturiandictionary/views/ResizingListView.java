package org.softastur.asturiandictionary.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by guifa on 9/15/17.
 */

public class ResizingListView extends ListView {
    ResizingListView(Context context) {
        super(context);
    }
    ResizingListView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }
    ResizingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }
    ResizingListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context,attrs,defStyleAttr);
    }

    public void resize() {
        ListAdapter listAdapter = getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, this);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = totalHeight + (getDividerHeight() * (listAdapter.getCount() - 1));
        setLayoutParams(params);
        requestLayout();
    }
}
