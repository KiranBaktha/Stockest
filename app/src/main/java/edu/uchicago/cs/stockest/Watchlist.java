package edu.uchicago.cs.stockest;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Watchlist extends AppCompatActivity {

    private Database_Adapter db_adapter;
    private  ListAdapter adapter;
    private ListView listView;
    private List<String> symbols;
    private ArrayList<String> companies;
    private List<String> latest_prices;
    private HashMap<String, String> target_list,price_list;
    private boolean periodic_check = false;

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        db_adapter = new Database_Adapter(this);
        db_adapter.open();
        symbols = new ArrayList<String>();
        companies = new ArrayList<String>();
        target_list = new HashMap<>();
        Cursor cursor = db_adapter.fetchAllWatch();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {  // for loop to start cursor from beginning to end
                String sym =cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_SYMBOL));
                symbols.add(sym);
                companies.add(cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_NAME)));
                target_list.put(sym,cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_TARGET)));
        }
        adapter = new ListAdapter(this,symbols,companies,false,true);
        listView = findViewById(R.id.main_list_watch);
        getCurrentPrices(symbols);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Watchlist.this);
                View dialog_view = LayoutInflater.from(Watchlist.this).inflate(R.layout.dialog_watch,null);
                builder.setView(dialog_view);
                final Dialog dialog = builder.create();
                dialog.show();
                Button delete = dialog_view.findViewById(R.id.delB);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteStock(position);
                        dialog.cancel();
                    }
                });
            }
        });
    }

    private void deleteStock(int position){
        db_adapter.deleteWatch(symbols.get(position)); // Delete from database
        price_list.remove(symbols.get(position)); // Delete from data structures present
        target_list.remove(symbols.get(position));
        symbols.remove(position);
        companies.remove(position);
        adapter = new ListAdapter(this,symbols,companies,false,true);
        adapter.setTargets(target_list);
        adapter.setPrices(price_list);
        listView.setAdapter(adapter);
    }

    private void getCurrentPrices(List<String> stock_symbols){
        new FetchPrices().execute(stock_symbols); // Call async task to get the latest prices
    }

    private class FetchPrices extends AsyncTask<List<String>, Void, List<String>> {
        @Override
        protected List<String> doInBackground(List<String>... params) {
            List<String> latest = new ArrayList<String>();
            List<String> symbols = params[0];
            try{
                for(int i=0;i<symbols.size();i++){
                    String URL = "https://api.iextrading.com/1.0/stock/" + params[0].get(i)+ "/batch?types=quote";
                    JSONObject json= new JSONParser2().getJSONFromUrl(URL);
                    JSONObject json_quote = json.getJSONObject("quote");
                    latest.add(json_quote.getString("latestPrice"));
                }
            }
            catch (JSONException e) { // Add custom looper to throw toast messages inside a fragment.
                        Toast.makeText(
                                Watchlist.this,
                                "JSON Exception has occured",
                                Toast.LENGTH_LONG
                        ).show();
            }
            return latest;
        }
        @Override
        protected void onPostExecute(List<String> latest) {
            latest_prices = latest;
            price_list = new HashMap<>();
            for(int i=0;i<symbols.size();i++){
                price_list.put(symbols.get(i),latest_prices.get(i)); // The 2 array lists are in sync
            }
            adapter.setPrices(price_list);
            adapter.setTargets(target_list);
            listView.setAdapter(adapter);
        }}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.watchlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Intent intent = new Intent(this, Watchlist.class);
            startActivity(intent);
            finish();
            return true;
        }

        if (id == R.id.action_periodic) {
            if (!periodic_check){
                periodic_check = true;
                startService(new Intent(this,RefreshService.class));
                Toast.makeText(this,"Periodic Refresh Started",Toast.LENGTH_SHORT).show();
            }
            else{
                periodic_check = false;
                stopService(new Intent(this,RefreshService.class));
                Toast.makeText(this,"Periodic Refresh Stopped",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
