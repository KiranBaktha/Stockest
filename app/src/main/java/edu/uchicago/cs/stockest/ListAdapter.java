package edu.uchicago.cs.stockest;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter{

    private Context con;
    private List<String> symbol_values;
    private List<String>company_values;
    private Boolean in_summarize;
    private Boolean in_watchlist;
    private HashMap<String, String> price_map;
    private HashMap<String, String> target_map;

    // The ViewHolder Class
    static class ViewHolder{
        TextView text;
        TextView text2;
    }
    // insum is a boolean variable that checks if the adapter is in summarize fragement or watchlist activity. Avoids duplicating list adaper code.
    public ListAdapter(Context context, List<String> symbols, List<String> companies, boolean insum, boolean inwatch){
        symbol_values = symbols;
        company_values = companies;
        con = context;
        in_summarize = insum;
        in_watchlist = inwatch;
    }

    @Override
    public int getCount() {
        return symbol_values.size();
    }

    @Override

    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setPrices(HashMap<String, String> price_list){
        price_map = price_list;
    }

    public void setTargets(HashMap<String, String> target_prices){
        target_map = target_prices;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        // create a ViewHolder reference
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = ((Activity) con).getLayoutInflater();
            if(in_summarize){
                view = inflater.inflate(R.layout.list_row2, viewGroup, false);
                holder.text = (TextView) view.findViewById(R.id.row_symboltext2);
                holder.text2 = (TextView) view.findViewById(R.id.row_text2);
            }
            else{
                view = inflater.inflate(R.layout.list_row, viewGroup, false);
                holder.text = (TextView) view.findViewById(R.id.row_symboltext);
                holder.text2 = (TextView) view.findViewById(R.id.row_text);
            }


            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        String symbolItem = symbol_values.get(position);
        String companyItem = company_values.get(position);
        if(in_watchlist){ // Change color if the target stock value has been reached.
            if (Float.valueOf(price_map.get(symbolItem))>= Float.valueOf(target_map.get(symbolItem))){
                holder.text.setBackgroundColor(con.getResources().getColor(R.color.red));
                holder.text2.setBackgroundColor(con.getResources().getColor(R.color.red));
            }
        }
        if (symbolItem != null && companyItem != null) {
            //set the item name on the TextView
            holder.text.setText(symbolItem);
            holder.text2.setText(companyItem);
        } else {
            holder.text.setText("Unknown");
            holder.text2.setText("Unknown");
        }
        return view;
    }


}

