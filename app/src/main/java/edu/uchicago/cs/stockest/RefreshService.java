package edu.uchicago.cs.stockest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class RefreshService extends Service
{
    CustomReciever alarm = new CustomReciever();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setPeriodicRefresh(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.setPeriodicRefresh(this);
    }

    @Override
    public void onDestroy(){
        alarm.cancelPeriodicRefresh(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}

