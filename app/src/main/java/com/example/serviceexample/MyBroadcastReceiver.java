package com.example.serviceexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final Handler handler;

    public MyBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    public double getAnnualizedReturns(ArrayList<Double> openList, ArrayList<Double> closeList) {
        int rows = openList.size();
        double sum = 0.0;
        for (int i=0;i<rows;i++) {
            sum += ((closeList.get(i) - openList.get(i)) / openList.get(i));
        }
        double average = sum/rows;
        int NUM_TRADING_DAYS = 250;
        double result = (average * NUM_TRADING_DAYS)*100;
        return result;
    }

    public double getAnnualizedVolatility(ArrayList<Double> openList, ArrayList<Double> closeList) {
        int rows = openList.size();
        double sum = 0.0;
        for (int i=0;i<rows;i++) {
            sum += ((closeList.get(i) - openList.get(i)) / openList.get(i));
        }
        double average = sum/rows;
        double numerator = 0.0;
        for (int i=0;i<rows;i++) {
            double ret = ((closeList.get(i) - openList.get(i)) / openList.get(i));
            numerator += ((ret - average) * (ret - average));
        }
        double denominator = rows - 1;
        double insideSquareRoot = numerator/denominator;
        double standardDeviation = Math.sqrt(insideSquareRoot);
        int NUM_TRADING_DAYS = 250;
        double result = (Math.sqrt(NUM_TRADING_DAYS) * standardDeviation)*100;
        return result;
    }

    public TextView getAReturnTextView(Context context, int i) {
        if (i == 0) return null;
        HashMap<Integer, TextView> aReturnMap = new HashMap<>();
        aReturnMap.put(1, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn1));
        aReturnMap.put(2, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn2));
        aReturnMap.put(3, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn3));
        aReturnMap.put(4, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn4));
        aReturnMap.put(5, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedReturn5));
        return aReturnMap.get(i);
    }

    public TextView getAVolatilityTextView(Context context, int i) {
        if (i == 0) return null;
        HashMap<Integer, TextView> aVolatilityMap = new HashMap<>();
        aVolatilityMap.put(1, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility1));
        aVolatilityMap.put(2, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility2));
        aVolatilityMap.put(3, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility3));
        aVolatilityMap.put(4, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility4));
        aVolatilityMap.put(5, (TextView) ((Activity)context).findViewById(R.id.txtAnnualizedVolatility5));
        return aVolatilityMap.get(i);
    }

    public EditText getTickerEditText(Context context, int i) {
        if (i == 0) return null;
        HashMap<Integer, EditText> editTextHashMap = new HashMap<>();
        editTextHashMap.put(1, (EditText) ((Activity)context).findViewById(R.id.editTicker1));
        editTextHashMap.put(2, (EditText) ((Activity)context).findViewById(R.id.editTicker2));
        editTextHashMap.put(3, (EditText) ((Activity)context).findViewById(R.id.editTicker3));
        editTextHashMap.put(4, (EditText) ((Activity)context).findViewById(R.id.editTicker4));
        editTextHashMap.put(5, (EditText) ((Activity)context).findViewById(R.id.editTicker5));
        return editTextHashMap.get(i);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("CHECK_TICKER")) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    for (int i = 1; i <= 5; i++) {
                        if (intent.hasExtra("ticker" + i)) {
                            String tickerName = intent.getStringExtra("ticker" + i);
                            Log.v("data", "broadcast receiver successfully received check_ticker: " + tickerName);
                            Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
                            Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, "ticker_name LIKE \'" + tickerName + "\'", null, null);
                            if (cursor.moveToFirst()) {
                                Log.v("data", cursor.getString(cursor.getColumnIndexOrThrow("ticker_name")) + " success!!!");
                                Intent intent = new Intent("DOWNLOAD_COMPLETE");
                                intent.putExtra("ticker", tickerName + i);
                                context.sendBroadcast(intent);

                            } else {
                                EditText editTextTicker = getTickerEditText(context, i);
                                editTextTicker.setError("Ticker not found, try downloading?");
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

                TextView txtAnnualizedReturn = getAReturnTextView(context, index);
                TextView txtAnnualizedVolatility = getAVolatilityTextView(context, index);
                EditText editTextTicker = getTickerEditText(context, index);
                txtAnnualizedReturn.setText("N/A");
                txtAnnualizedVolatility.setText("N/A");
                editTextTicker.setError("Invalid Ticker");
            }
        });
        }

        if (intent.getAction().equals("DOWNLOAD_COMPLETE")) {
            String intentExtra = intent.hasExtra("ticker") ? intent.getStringExtra("ticker") : "";
            String tickerName = intentExtra.substring(0, intentExtra.length()-1);
            int index = Integer.parseInt(intentExtra.substring(intentExtra.length() - 1, intentExtra.length()));

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
                    TextView aReturnTextView = getAReturnTextView(context, index);
                    TextView aVolatilityTextView = getAVolatilityTextView(context, index);
                    aReturnTextView.setText("Calculating...");
                    aVolatilityTextView.setText("Calculating...");
                    ArrayList<Double> closeList = new ArrayList<>();
                    ArrayList<Double> openList = new ArrayList<>();
                    Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, "ticker_name LIKE \'" + tickerName + "\'", null, null);
                    if (cursor.moveToFirst()) {
                        double close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                        double open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                        closeList.add(close);
                        openList.add(open);
                        cursor.moveToNext();
                        while (!cursor.isAfterLast()) {
                            close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                            open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                            closeList.add(close);
                            openList.add(open);
                            cursor.moveToNext();
                            Log.v("open, close", open + " " + close);
                        }
                        double annualizedReturns = getAnnualizedReturns(openList, closeList);
                        double annualizedVolatility = getAnnualizedVolatility(openList, closeList);
                        Log.v("annualized returns",  annualizedReturns + "");
                        Log.v("annualized volatility",  annualizedVolatility + "");
                        aReturnTextView.setText(String.format("%.2f", annualizedReturns)+"%");
                        aVolatilityTextView.setText(String.format("%.2f", annualizedVolatility)+"%");
                    } else {
                        aReturnTextView.setText("No records found");
                        aVolatilityTextView.setText("No records found");
                    }
                }
            });
        }
    }
}
