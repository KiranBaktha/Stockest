package edu.uchicago.cs.stockest;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class info extends Fragment {

    private TextView text;
    private WebView webView;
    private Button summarize;
    private Button addwatch;
    private String symbol;
    private AppCompatActivity activity;
    public info() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            activity = (AppCompatActivity) context;
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info, container, false);
        symbol = ((Start)getActivity()).getSymbol();
        final String company = ((Start)getActivity()).getCompany();
        text = view.findViewById(R.id.info_text);
        text.setText(company);
        webView = view.findViewById(R.id.webview);
        addwatch = view.findViewById(R.id.add_button);
        summarize = view.findViewById(R.id.summarize_button);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (savedInstanceState == null)  // Load only if saved instance is null
        {
            webView.loadUrl("https://finance.yahoo.com/chart/" + symbol);
        }
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    return true;
                }
                return false;
            }
        } );
        summarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Loading load_frag= new Loading();
               getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,load_frag).commit();
               new FetchStats().execute(symbol);
            }
        });

        addwatch.setOnClickListener(new View.OnClickListener() {
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


    @Override
    public void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }


    private class FetchStats extends AsyncTask<String, Void, StartFragment.Combolists> {
        @Override
        protected StartFragment.Combolists doInBackground(String... params) {
            StartFragment.Combolists combo = new StartFragment.Combolists();
            ArrayList<String> attributes = new ArrayList<String>();
            ArrayList<String> values = new ArrayList<String>();
            final String URL1 = "https://api.iextrading.com/1.0/stock/" + params[0]+ "/batch?types=quote";
            JSONObject json= new JSONParser2().getJSONFromUrl(URL1);
            try{

                JSONObject json_quote = json.getJSONObject("quote");
                attributes.add("Symbol");
                values.add(json_quote.getString("symbol"));
                attributes.add("Company Name");
                values.add(json_quote.getString("companyName"));
                attributes.add("Primary Exchange");
                values.add(json_quote.getString("primaryExchange"));
                attributes.add("Latest Date/Time");
                values.add(json_quote.getString("latestTime"));
                attributes.add("Open Price");
                values.add(json_quote.getString("open"));
                attributes.add("Close Price");
                values.add(json_quote.getString("close"));
                attributes.add("Lowest Price");
                values.add(json_quote.getString("low"));
                attributes.add("Highest Price");
                values.add(json_quote.getString("high"));
                attributes.add("Change Percent");
                values.add(json_quote.getString("changePercent"));
                attributes.add("Yearly High");
                values.add(json_quote.getString("week52High"));
                attributes.add("Yearly Low");
                values.add(json_quote.getString("week52Low"));
                attributes.add("Computed Weekly Average");
                values.add(computeAverage());
            }
            catch (JSONException e) { // Add custom looper to throw toast messages inside a fragment.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                activity,
                                "JSON Exception has occured",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
            // Add them to the combined class representation
            combo.one = attributes;
            combo.two = values;
            return combo;
        }
        @Override
        protected void onPostExecute(StartFragment.Combolists combo) {
            SummarizeFragment sum_frag= new SummarizeFragment();
            Bundle bundle = new Bundle();
            String[] attr = new String[combo.one.size()];
            attr = combo.one.toArray(attr);
            String[] val = new String[combo.two.size()];
            val = combo.two.toArray(val);
            bundle.putStringArray("attr",attr);
            bundle.putStringArray("val",val);
            sum_frag.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.container,sum_frag).commit();
        }}

        private String computeAverage(){
        // Computes the weekly average from the API data
            float average = 0.0f;
            String URL2 = "https://api.iextrading.com/1.0/stock/" + symbol + "/batch?types=chart&range=1m";
            JSONObject json2= new JSONParser2().getJSONFromUrl(URL2);
            try{
                JSONArray json_chart = json2.getJSONArray("chart");
                for (int i=json_chart.length()-1;i>=json_chart.length()-5;i--){ // Get the latest 5 dates change percent
                    String change = json_chart.getJSONObject(i).getString("changePercent");
                    average += Float.valueOf(change);
                }
                average = average/5;
            }
            catch (JSONException e) { // Add custom looper to throw toast messages inside a fragment.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                activity,
                                "JSON Exception has occured",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
             return String.valueOf(average);
        }


}
