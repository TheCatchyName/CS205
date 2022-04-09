package com.example.serviceexample;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MyService extends Service{
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private HashMap<String, String> tickerNames;

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    // API token
    private final String TOKEN ="c8vd5faad3iaocnjvp60";

    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName(); // e.g. GOOGL3
                    Log.v("thread name", threadName);
                    String tickerName = threadName.substring(0, threadName.length()-1); // e.g. GOOGL

                    String stringUrl = "https://finnhub.io/api/v1/stock/candle?symbol="+ tickerName
                            +"&resolution=D&from=1625097601&to=1640995199&token=" + TOKEN;
                    String result;
                    String inputLine;

                    try {

                        // make GET requests
                        URL myUrl = new URL(stringUrl);
                        HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();

                        connection.setRequestMethod(REQUEST_METHOD);
                        connection.setReadTimeout(READ_TIMEOUT);
                        connection.setConnectTimeout(CONNECTION_TIMEOUT);

                        connection.connect();

                        // store json string from GET response
                        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(streamReader);
                        StringBuilder stringBuilder = new StringBuilder();

                        while((inputLine = reader.readLine()) != null){
                            stringBuilder.append(inputLine);
                        }

                        reader.close();
                        streamReader.close();

                        result = stringBuilder.toString();

                    } catch(IOException e) {
                        e.printStackTrace();
                        result = null;
                        Thread.currentThread().interrupt();
                    }

                    // parse the json string into 'close' and 'open' array
                    JSONObject jsonObject = null;
                    JSONArray jsonArrayClose = null;
                    JSONArray jsonArrayOpen = null;
                    String status = null;

                    try {
                        jsonObject = new JSONObject(result);
                        status = jsonObject.getString("s");
                        jsonArrayClose = jsonObject.getJSONArray("c");
                        jsonArrayOpen = jsonObject.getJSONArray("o");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (status.equals("ok")) {
                        Log.v("data", "status: " + status);
                        Log.v("close", String.valueOf(jsonArrayClose.length()));
                        Log.v("open", String.valueOf(jsonArrayOpen.length()));

                        try {
                            for (int i = 0; i < jsonArrayClose.length(); i++) {
                                double close = jsonArrayClose.getDouble(i);
                                double open = jsonArrayOpen.getDouble(i);

                                Log.v("data", i + "ticker name: " + tickerName + ":, c: " + close + "o: " + open);

                                ContentValues values = new ContentValues();
                                values.put(HistoricalDataProvider.CLOSE, close);
                                values.put(HistoricalDataProvider.OPEN, open);
                                values.put(HistoricalDataProvider.TICKER_NAME, tickerName);

                                // remove old data
                                getContentResolver().delete(HistoricalDataProvider.CONTENT_URI,"ticker_name=?",new String[]{tickerName});

                                // insert new data
                                getContentResolver().insert(HistoricalDataProvider.CONTENT_URI, values);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // broadcast message that download is complete
                        Intent intent = new Intent("DOWNLOAD_COMPLETE");
                        intent.putExtra("ticker", threadName); // e.g. name of extra: ticker, value of extra: GOOGL3
                        sendBroadcast(intent);
                        stopSelf(msg.arg1);
                    } else {
                        Log.v("data", "status: " + status);
                        //if there is no such ticker/no data found, notify user with an error message (in broadcast receiver DOWNLOAD_FAILED) and gracefully cancel
                        //i.e. if download failed for any reason
                        Intent intent = new Intent("DOWNLOAD_FAILED");
                        intent.putExtra("ticker", Thread.currentThread().getName());
                        sendBroadcast(intent);

                        stopSelf(msg.arg1);
                    }
                }
            };

            int index = 0;
            Thread[] threadArr = new Thread[tickerNames.size()];

            for (Map.Entry<String, String> set : tickerNames.entrySet()) {
                String threadName = set.getValue() + set.getKey().substring(set.getKey().length()-1, set.getKey().length()); // e.g. GOOGL3 if GOOGL was entered in the third edit text
                Thread thread = new Thread(runnable, threadName);
                threadArr[index++] = thread;
            }

            // start threads
            for (Thread thread : threadArr) {
                thread.start();
            }

            for (Thread thread : threadArr) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onCreate(){
        HandlerThread thread = new HandlerThread("Service", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        tickerNames = new HashMap<>();

        // to check which tickers have values entered by user
        for (int i = 1; i <= 5; i++) {
            if (intent.hasExtra("ticker" + i)) {
                tickerNames.put("ticker" + i, intent.getStringExtra("ticker" + i)); // e.g. key: ticker3, value: GOOGL
            }

            Log.v("ticker" + i + " exists: ", String.valueOf(intent.hasExtra("ticker" + i)));
            Log.v("ticker" + i + " value: ", String.valueOf(intent.getStringExtra("ticker" + i)));
        }

        Message msg = serviceHandler.obtainMessage();
        msg.obj = tickerNames;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "download done", Toast.LENGTH_SHORT).show();
    }
}
