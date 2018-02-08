package by.project.dartlen.testappwithserviceandbroadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MyService extends Service {

    /*private OkHttpClient client;
    private ElenaWebSocketListener webSocketListener;
    private WebSocket ws;
    private Handler wsHandler;*/

    private static final int CONNECT_TO_WEB_SOCKET = 1;
    private static final int SEND_MESSAGE = 2;
    private static final int CLOSE_WEB_SOCKET = 3;
    private static final int DISCONNECT_LOOPER = 4;

    private static final String KEY_MESSAGE = "keyMessage";

    private Handler mServiceHandler;
    private Looper mServiceLooper;
    private WebSocket mWebSocket;
    private boolean mConnected=true;
    private ElenaWebSocketListener ewsl;

    //timer
    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;
    public int counter = 0;


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.d("in timer", "in timer ++++  " + (counter++));
                Message message = Message.obtain();
                message.what = SEND_MESSAGE;
                Bundle data = new Bundle();
                data.putString(KEY_MESSAGE, counter+" ");
                message.setData(data);
                mServiceHandler.sendMessage(message);
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("WebSocket service");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mServiceHandler.sendEmptyMessage(CONNECT_TO_WEB_SOCKET);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initService(intent);
        //webSocketHandler();
        startTimer();


        return START_STICKY;
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
        //Log.i(LOG_TAG, "Received Stop Foreground Intent");
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    /**
     * WebSocket
     */

    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED
    }

    public interface ServerListener {
        void onNewMessage(String message);
        void onStatusChange(ConnectionStatus status);
    }

    private final class ElenaWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {


            webSocket.send("Hello, it's SSaurel !");
            webSocket.send("What's up ?");
            webSocket.send(ByteString.decodeHex("deadbeef"));
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {


        }
    }

    private void output(final String txt) {
        Log.d("websocket:",txt);
    }



    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_TO_WEB_SOCKET:
                    connectToWebSocket();
                    break;
                case SEND_MESSAGE:
                    sendMessageThroughWebSocket(msg.getData().getString(KEY_MESSAGE));

                    break;
                case CLOSE_WEB_SOCKET:
                    closeWebSocket();
                    break;
                case DISCONNECT_LOOPER:
                    mServiceLooper.quit();
                    break;
            }
        }
    }

    private void sendMessageThroughWebSocket(String message) {
        if (!mConnected) {
            return;
        }

        mWebSocket.send(message);

        //mWebSocket.sendMessage(WebSocket.PayloadType.TEXT, new Buffer().write(message.getBytes()));

    }

    private void connectToWebSocket() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();

        ewsl = new ElenaWebSocketListener();

        mWebSocket = okHttpClient.newWebSocket(request, ewsl);

        okHttpClient.dispatcher().executorService().shutdown();


    }

    private void closeWebSocket() {
        if (!mConnected) {
            return;
        }
        mWebSocket.close(1000, "Goodbye, World!");

    }


}