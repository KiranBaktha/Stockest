package edu.uchicago.cs.stockest;


import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SummarizeFragment extends Fragment {
   private List<String> attributes;
   private List<String> value;
   private ListView listView;
   private Button addbutton;
   private String symbol;
   private String company;

    public SummarizeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summarize, container, false);
        Bundle bundle = getArguments();
        symbol = ((Start)getActivity()).getSymbol();
        company = ((Start)getActivity()).getCompany();
        attributes = Arrays.asList(bundle.getStringArray("attr"));
        value = Arrays.asList(bundle.getStringArray("val"));
        addbutton = view.findViewById(R.id.add_sum_button);
        ListAdapter adapter = new ListAdapter(getActivity(),attributes,value,true, false);
        listView = view.findViewById(R.id.main_list_sum);
        listView.setAdapter(adapter);
       /* addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
       addbutton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
               View dialog_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog,null);
               builder.setView(dialog_view);
               final Dialog dialog = builder.create();
               dialog.show();
               Button completed = dialog_view.findViewById(R.id.doneB);
               final EditText price = dialog_view.findViewById(R.id.custom_edit);
               completed.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       String pri = price.getText().toString();
                       if (pri.matches("")){
                           Toast.makeText(getActivity(),"Please enter a value",Toast.LENGTH_SHORT).show();
                       }
                       else{
                           ArrayList<String> syms = new ArrayList<String>();
                           Database_Adapter db_adapter = new Database_Adapter(getActivity());
                           db_adapter.open();
                           Cursor cursor = db_adapter.fetchAllWatch();
                           for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {  // for loop to start cursor from beginning to end
                               syms.add(cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_SYMBOL)));
                           }
                           if(!syms.contains(symbol)){ // Add only if not already in watchlist
                               ((Start)getActivity()).addtowatchlist(symbol,company,pri);
                           }
                           ((Start) getActivity()).toast();
                           dialog.dismiss();
                       }
                   }
               });
           }
       });
        return view;
    }

}
