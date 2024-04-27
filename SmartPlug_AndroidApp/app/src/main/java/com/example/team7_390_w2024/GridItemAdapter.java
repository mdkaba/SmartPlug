package com.example.team7_390_w2024;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridItemAdapter extends BaseAdapter {
    private Context context;
    private List<String> data;

    public GridItemAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView gridViewItem;
        if (convertView == null) {
            gridViewItem = new TextView(context);
            gridViewItem.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.MATCH_PARENT
            ));
            gridViewItem.setGravity(Gravity.CENTER);
            //gridViewItem.setBackgroundColor(Color.LTGRAY);
        } else {
            gridViewItem = (TextView) convertView;
        }
        if(position%2==0)
        {
            gridViewItem.setPadding(0,20,0,20);
        }
        else {
            gridViewItem.setPadding(0,20,50,20);

        }
        gridViewItem.setText(data.get(position));
        return gridViewItem;
    }

}
