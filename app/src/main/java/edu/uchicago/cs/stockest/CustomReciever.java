package edu.uchicago.cs.stockest;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomReciever extends BroadcastReceiver
{
    private boolean need_to_notify = false;
    private Context con;
    // Use this onReceive method and setPeriodicRefresh method for testing if you prefer rather than the ones below. It sends notification every minute(minimum limit as per the documentation)
    // with running any checks.
    /*
    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire(); // Acquire the lock
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.stocknotification);
        mBuilder.setContentTitle("Stockest Watchlist Update");
        mBuilder.setContentText("Hey, 1 or more of your watchlisted stocks just reached their target value. Open the app to check it out.");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
        wl.release(); // Release
    }

       public void setPeriodicRefresh(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, CustomReciever.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 *60, pi); // Millisec * Second*Minute (1 Minute - Minimum time as per the android docs)
    }

    */

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire(); // Acquire the lock
        con = context;
        // Create a new thread to execute the Network operations.
        Runnable r = new Runnable() {
            public void run() {
            RunTask();
            }
            };
        Thread newthread = new Thread(r);
        newthread.start();

        if (need_to_notify){
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.drawable.stocknotification);
            mBuilder.setContentTitle("Stockest Watchlist Update");
            mBuilder.setContentText("Hey, 1 or more of your watchlisted stocks just reached their target value. Open the app to check it out.");
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
            wl.release(); // Release
        }
    }

    public void RunTask(){
        // Check if any of the stock has reached the target value.
        Database_Adapter db_adapter = new Database_Adapter(con);  // Create new Database adapter
        db_adapter.open();
        List<String> symbols = new ArrayList<String>();
        List<String> company = new ArrayList<String>();
        HashMap<String, String> target_list =  new HashMap<String, String>();
        HashMap<String, String> price_list =  new HashMap<String, String>();
        // We fetch values from the database because the database contains the latest stocks in watchlist regardless of when the user adds or deletes a watchlisted stock.
        Cursor cursor = db_adapter.fetchAllWatch();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {  // for loop to start cursor from beginning to end
            String sym =cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_SYMBOL));
            symbols.add(sym);
            company.add(cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_NAME)));
            target_list.put(sym,cursor.getString(cursor.getColumnIndexOrThrow(Database_Adapter.WATCH_STOCK_TARGET)));
        }
        List<String> latest = new ArrayList<String>();
        // Hit the server to get the latest prices
        try{
            for(int i=0;i<symbols.size();i++){
                String URL = "https://api.iextrading.com/1.0/stock/" + symbols.get(i)+ "/batch?types=quote";
                JSONObject json= new JSONParser2().getJSONFromUrl(URL);
                JSONObject json_quote = json.getJSONObject("quote");
                latest.add(json_quote.getString("latestPrice"));
            }
        }
        catch (JSONException e) { // Add custom looper to throw toast messages inside a fragment.
            Toast.makeText(
                    con,
                    "JSON Exception has occured",
                    Toast.LENGTH_LONG
            ).show();
        }
        for(int i=0;i<symbols.size();i++){
            price_list.put(symbols.get(i),latest.get(i)); // Add to price list hash map
        }
        // Check if any stock has reached it's set target price
        need_to_notify = false;
        for(int i=0;i<symbols.size();i++) {
            if(Float.valueOf(price_list.get(symbols.get(i))) >= Float.valueOf(target_list.get(symbols.get(i)))){
                need_to_notify =true; // We need to notify
            }
        }
    }

    public void setPeriodicRefresh(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, CustomReciever.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 *60, pi); // Millisec * Second*Minute ()
    }

    public void cancelPeriodicRefresh(Context context)
    {
        Intent intent = new Intent(context, CustomReciever.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}

