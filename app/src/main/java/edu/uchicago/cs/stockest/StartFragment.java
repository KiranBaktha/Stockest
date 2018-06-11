package edu.uchicago.cs.stockest;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class StartFragment extends Fragment {

    private ListView listView;
    private EditText edittext;
    private Database_Adapter db_adapter;
    private  ListAdapter adapter;
    private ArrayList<String> relevant_stock;
    private ArrayList<String> relevant_companies;

    public StartFragment() {
    }

    // Class to hold custom 2 Arraylists to be used as a return type for async task
    public static class Combolists{
        ArrayList<String> one;
        ArrayList<String> two;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_start, container, false);
        listView = view.findViewById(R.id.main_list);
        db_adapter = new Database_Adapter(getActivity());
        db_adapter.open();

        edittext = view.findViewById(R.id.textenter);
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(getActivity(),s,Toast.LENGTH_LONG).show();
                new FetchCodesTask().execute(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   fireloadfragment(position);
            }
        });

        return view;
    }

    private void fireloadfragment(int position){
        ((Start)getActivity()).setSymbol(relevant_stock.get(position));
        ((Start)getActivity()).setCompany(relevant_companies.get(position));
        info Info_fragment =  new info();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,Info_fragment).commit();
    }

    private class FetchCodesTask extends AsyncTask<String, Void,Combolists> {
        @Override
        protected Combolists doInBackground(String... params) {
              Combolists combo = new Combolists();
              relevant_stock = new ArrayList<String>();
              relevant_companies = new ArrayList<String>();
              Cursor cursor = db_adapter.fetchAll();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {  // for loop to start cursor from beginning to end
                String company = cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.COL_STOCK_NAME));
                if (company.length() > params[0].length() && company.substring(0,params[0].length()).equalsIgnoreCase(params[0])){ //
                    relevant_stock.add(cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.COL_STOCK_SYMBOL)));
                    relevant_companies.add(company);
                }
            }
            // Add them to the combined class representation
            combo.one = relevant_stock;
            combo.two = relevant_companies;
           return combo;
        }
        @Override
        protected void onPostExecute(Combolists combo) {
            adapter = new ListAdapter(getActivity(),combo.one,combo.two,false, false);
            listView.setAdapter(adapter);
        }}
}
