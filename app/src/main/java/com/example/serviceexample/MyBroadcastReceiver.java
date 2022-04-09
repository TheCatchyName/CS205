package com.example.serviceexample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final Handler handler;

    public MyBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        TextView aReturn1 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn1);
        TextView aVolatility1 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility1);
        TextView aReturn2 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn2);
        TextView aVolatility2 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility2);
        TextView aReturn3 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn3);
        TextView aVolatility3 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility3);
        TextView aReturn4 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn4);
        TextView aVolatility4 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility4);
        TextView aReturn5 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn5);
        TextView aVolatility5 = (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility5);

        List<TextView> aVolatilityTxtList = new ArrayList<TextView>(Arrays.asList(
                aVolatility1, aVolatility2, aVolatility3, aVolatility4, aVolatility5
        ));

        List<TextView> aReturnTxtList = new ArrayList<TextView>(Arrays.asList(
                aReturn1, aReturn2, aReturn3, aReturn4, aReturn5
        ));

        if (intent.getAction().equals("CHECK_TICKER")) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < 5; i++) {
                        if (intent.hasExtra("ticker" + i)) {
                            String tickerName = intent.getStringExtra("ticker" + i);
                            Log.v("data", "broadcast receiver successfully received check_ticker: " + tickerName);
                            Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
                            Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, "ticker_name LIKE \'" + tickerName + "\'", null, null);
                            if (cursor.moveToFirst()) {
                                Log.v("data", cursor.getString(cursor.getColumnIndexOrThrow("ticker_name")) + " success!!!");
                                while (!cursor.isAfterLast()) {
                                    Log.v("data", "name: " + cursor.getString(cursor.getColumnIndexOrThrow("ticker_name")));
                                    cursor.moveToNext();
                                }
                            }
                        }
                    }
                }
            });
        }

        if (intent.getAction().equals("DOWNLOAD_FAILED")) {
            handler.post(new Runnable() {
            @Override
            public void run() {
                String intentExtra = intent.getStringExtra("ticker");
                int index = Integer.parseInt(intentExtra.substring(intentExtra.length() - 1, intentExtra.length()));

                TextView txtAnnualizedReturn = aReturnTxtList.get(index-1);
                TextView txtAnnualizedVolatility = aVolatilityTxtList.get(index-1);
                txtAnnualizedReturn.setText("Invalid Ticker");
                txtAnnualizedVolatility.setText("Invalid Ticker");
            }
        });
        }

        if (intent.getAction().equals("DOWNLOAD_COMPLETE")) {
            String intentExtra = intent.hasExtra("ticker") ? intent.getStringExtra("ticker") : "";
            String tickerName = intentExtra.substring(0, intentExtra.length()-1);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
//                    TextView result = (TextView) ((Activity)context).findViewById(R.id.textview_result);
//                    result.setText("Calculating...");
                    double sum_price = 0.0;
//                    double sum_volume = 0.0;
                    int counter = 0;
                    Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, "ticker_name LIKE \'" + tickerName + "\'", null, null);
                    if (cursor.moveToFirst()) {
                        double close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                        double open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                        sum_price += close - open;
                        counter += 1;
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getColumnIndex("id");
                            close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                            open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                            sum_price += close - open;
                            counter += 1;
                            cursor.moveToNext();
//                            Log.v("data",  "close: " + close + ", open: " + open);
                        }
                    } else {
//                        result.setText("No Records Found");
                    }

                    double annual = sum_price / counter;
                    Log.v("data", "ticker: " + tickerName + ", avg calculation: " + annual);

//                    result.setText(String.format("%.2f", vwap));

                }
            });
        }
    }
}
