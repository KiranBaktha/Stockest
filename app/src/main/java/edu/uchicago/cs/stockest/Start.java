package edu.uchicago.cs.stockest;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Start extends AppCompatActivity {

    public static ArrayList<String> symbols = new ArrayList<String>();
    public static ArrayList<String> companies = new ArrayList<String>();
    private Database_Adapter db_adapter;
    private WebView webview;
    private String symbol;
    private String company;
    StartFragment fragone = new StartFragment();
    info Info_Fragemnt = new info();
    initialfragment fragload = new initialfragment();

    // Getters and setters for symbol and comapny to be accessible and set by any fragment
    public void setSymbol(String sym){
        symbol = sym;
    }

    public void setCompany(String com){
        company = com;
    }

    public String getSymbol(){
        return symbol;
    }

    public String getCompany(){
        return company;
    }

    @Override
    public boolean onSupportNavigateUp(){
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag instanceof info){
            getSupportFragmentManager().beginTransaction().replace(R.id.container,fragone).commit();
        }
        else if(frag instanceof SummarizeFragment){
            getSupportFragmentManager().beginTransaction().replace(R.id.container,Info_Fragemnt).commit();
        }
        else{
            finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        db_adapter = new Database_Adapter(Start.this); // Learn: Start.this because only this implies a view.
        db_adapter.open();
        webview = findViewById(R.id.webview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        if(db_adapter.checkEmpty()){
            getSupportFragmentManager().beginTransaction().add(R.id.container,fragload).commit();
            String URL = "https://api.iextrading.com/1.0/ref-data/symbols";
            new FetchCodesTask().execute(URL);
        }
        else{
            getSupportFragmentManager().beginTransaction().add(R.id.container,fragone).commit();
        }

    }

    private class FetchCodesTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                if (jsonArray == null) {
                    throw new JSONException("no data available.");
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    String sym = jsonArray.getJSONObject(i).getString("symbol");
                    String com =  jsonArray.getJSONObject(i).getString("name");
                    if (sym.length()>0 && com.length()>0){
                        symbols.add(sym);
                        companies.add(com);
                        db_adapter.createEntry(sym,com);
                    }
                }
                Toast.makeText(Start.this,"Welcome",Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,fragone).commit();
            } catch (JSONException e) {
                Toast.makeText(
                        Start.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                e.printStackTrace();
                finish();
            }
        }}

        public void addtowatchlist(String symbol, String company,String target){
              db_adapter.createEntryWatch(symbol,company,target);
        }

    public void toast(){
        Toast to = new Toast(this);
        ImageView view = new ImageView(this);
        view.setImageResource(R.drawable.check);
        to.setView(view);
        to.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_watch) {
            Intent intent = new Intent(this, Watchlist.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_exit) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
