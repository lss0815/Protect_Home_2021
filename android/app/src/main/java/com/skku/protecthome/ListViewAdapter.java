package com.skku.protecthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    private TextView dateTextView;

    private ArrayList<ListViewItem> listViewItemArrayList = new ArrayList<ListViewItem>();

    @Override
    public int getCount() {
        return listViewItemArrayList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Context context = parent.getContext();
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        dateTextView = (TextView) convertView.findViewById(R.id.date);

        ListViewItem listViewItem = listViewItemArrayList.get(position);

        dateTextView.setText(listViewItem.getDate());

        return convertView;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public ListViewItem getItem(int position){
        return listViewItemArrayList.get(position);
    }

    public void addItem(ListViewItem _item){
        ListViewItem item = new ListViewItem();

        item.setDate(_item.getDate());

        listViewItemArrayList.add(item);
    }

    public void clear(){
        listViewItemArrayList.clear();
    }
}
