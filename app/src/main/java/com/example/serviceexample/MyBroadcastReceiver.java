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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;


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
        EditText ticker1 = (EditText) ((Activity)context).findViewById(R.id.editTicker1);
        EditText ticker2 = (EditText) ((Activity)context).findViewById(R.id.editTicker2);
        EditText ticker3 = (EditText) ((Activity)context).findViewById(R.id.editTicker3);
        EditText ticker4 = (EditText) ((Activity)context).findViewById(R.id.editTicker4);
        EditText ticker5 = (EditText) ((Activity)context).findViewById(R.id.editTicker5);

        List<EditText> tickerEditTextList = new ArrayList<EditText>(Arrays.asList(
                ticker1, ticker2, ticker3, ticker4, ticker5
        ));

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

//                TextView txtAnnualizedReturn = aReturnTxtList.get(index-1);
//                TextView txtAnnualizedVolatility = aVolatilityTxtList.get(index-1);
                EditText editTextTicker = tickerEditTextList.get(index-1);
//                txtAnnualizedReturn.setText("Invalid Ticker");
//                txtAnnualizedVolatility.setText("Invalid Ticker");
                editTextTicker.setError("Invalid Ticker");
            }
        });
        }

        if (intent.getAction().equals("DOWNLOAD_COMPLETE")) {
            String intentExtra = intent.hasExtra("ticker") ? intent.getStringExtra("ticker") : "";
            String tickerName = intentExtra.substring(0, intentExtra.length()-1);
            int index = Integer.parseInt(intentExtra.substring(intentExtra.length() - 1, intentExtra.length()));
            Log.v("Index: ", String.valueOf(index));
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
                        Log.v("annualized volatility",  annualizedReturns + "");
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
