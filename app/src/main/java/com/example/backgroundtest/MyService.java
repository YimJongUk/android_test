package com.example.backgroundtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    BackgroundTask task;
    int value = 0;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task = new BackgroundTask();
        task.execute();

        initializeNotification();

        return START_NOT_STICKY;
    }

    public void initializeNotification()
    {
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                                                        .bigText("설정을 보려면 누르세요.")
                                                        .setBigContentTitle(null)
                                                        .setSummaryText("서비스 동작중");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                                                    .setSmallIcon(R.drawable.push_72)
                                                    .setContentText(null)
                                                    .setContentTitle(null)
                                                    .setOngoing(true)
                                                    .setStyle(style)
                                                    .setWhen(0)
                                                    .setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(new NotificationChannel("1", "포그라운드 서비스", NotificationManager.IMPORTANCE_NONE));
        }

        Notification notification = builder.build();
        startForeground(1, notification);

    }

    class BackgroundTask extends AsyncTask<Integer, String, Integer> {
        String result = "";

        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        protected Integer doInBackground(Integer... values){
            while(isCancelled() == false)
            {
                try{
                    println(value + "번째 실행중");
                    Thread.sleep(2000);

                    value++;
                }catch ( InterruptedException ex) { }
            }
            return value;
        }

        @Override
        protected void onProgressUpdate(String... String){
            println("onProgressUpdate() 업데이트");
        }

        @Override
        protected void onPostExecute(Integer integer){
            println("onPostExcute()");
            value = 0;
        }

        @Override
        protected void onCancelled(){
            value = 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService", "onDestory");

        task.cancel(true);
    }

    public void println(String message){
        Log.d("MyService", message);
    }
}