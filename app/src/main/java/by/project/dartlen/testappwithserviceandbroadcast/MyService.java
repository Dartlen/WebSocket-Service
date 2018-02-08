package by.project.dartlen.testappwithserviceandbroadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements ServerConnection.ServerListener{

    private final String SERVER_URL = "ws://echo.websocket.org";
    private ServerConnection mServerConnection;
    //timer
    private Timer timer;
    private TimerTask timerTask;
    public int counter = 0;


    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.d("in timer", "in timer ++++  " + (counter++));
                mServerConnection.sendMessage(String.valueOf(counter));
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initService(intent);
        startTimer();

        mServerConnection = new ServerConnection(SERVER_URL);
        mServerConnection.connect(this);

        return START_STICKY;
    }

    @Override
    public void onNewMessage(String message) {
        Log.d("WS DATA:", message);
    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {
        Log.d("WS STATUS:", status.toString());
    }

    private void initService(Intent intent) {

        Intent playIntent = new Intent(this, MyService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MyService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_foreground);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Truiton Music Player")
                .setTicker("Truiton Music Player")
                .setContentText("My Music")
                .setSmallIcon(R.drawable.ic_launcher_background)

                /*.setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous,
                        "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent)*/.build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

/*
        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        } */
        if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {

            stopForeground(true);
            stopSelf();
        }

    }

    private void stopService(){
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}