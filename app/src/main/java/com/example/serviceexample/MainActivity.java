package com.example.serviceexample;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private Button download, calc;
    private TextView aReturn1, aVolatility1, aReturn2, aVolatility2, aReturn3, aVolatility3, aReturn4, aVolatility4, aReturn5, aVolatility5;
    private EditText ticker1, ticker2, ticker3, ticker4, ticker5;
    private BroadcastReceiver myBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up layout
        setContentView(R.layout.activitymain);

        download = (Button) findViewById(R.id.btn_download);
        calc = (Button) findViewById(R.id.btn_calc);
        aReturn1 = (TextView) findViewById(R.id.txtAnnualizedReturn1);
        aVolatility1 = (TextView) findViewById(R.id.txtAnnualizedVolatility1);
        aReturn2 = (TextView) findViewById(R.id.txtAnnualizedReturn2);
        aVolatility2 = (TextView) findViewById(R.id.txtAnnualizedVolatility2);
        aReturn3 = (TextView) findViewById(R.id.txtAnnualizedReturn3);
        aVolatility3 = (TextView) findViewById(R.id.txtAnnualizedVolatility3);
        aReturn4 = (TextView) findViewById(R.id.txtAnnualizedReturn4);
        aVolatility4 = (TextView) findViewById(R.id.txtAnnualizedVolatility4);
        aReturn5 = (TextView) findViewById(R.id.txtAnnualizedReturn5);
        aVolatility5 = (TextView) findViewById(R.id.txtAnnualizedVolatility5);
        ticker1 = (EditText) findViewById(R.id.editTicker1);
        ticker2 = (EditText) findViewById(R.id.editTicker2);
        ticker3 = (EditText) findViewById(R.id.editTicker3);
        ticker4 = (EditText) findViewById(R.id.editTicker4);
        ticker5 = (EditText) findViewById(R.id.editTicker5);

        // list of edit texts (ticker input)
        List<EditText> tickerEditTextList = new ArrayList<EditText>(Arrays.asList(
                ticker1, ticker2, ticker3, ticker4, ticker5
        ));

        // list of text views (annualized volatility)
        List<TextView> aVolatilityTxtList = new ArrayList<TextView>(Arrays.asList(
                aVolatility1, aVolatility2, aVolatility3, aVolatility4, aVolatility5
        ));

        // list of text views (annualized returns)
        List<TextView> aReturnTxtList = new ArrayList<TextView>(Arrays.asList(
                aReturn1, aReturn2, aReturn3, aReturn4, aReturn5
        ));

        // start service, pass ticker info via an intent
        Intent intent = new Intent(getApplicationContext(), MyService.class);

        // download & calculate button onclick event listener
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numTickers = 0;

                for (int i = 0; i < tickerEditTextList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        numTickers++; // to keep track of how many tickers the user entered

                        tickerEditTextList.get(i).setError(null); // remove any existing error msges shown to user

                        // if user enters ticker1 and 3 but not 2, the intent will have the extras ticker1 and ticker3
                        // MyService: intent.hasExtra("ticker1") can use this code to check if an extra was set
                        // MyBroadcastReceiver: can use the 1/2/3/4/5 to know which annualizedReturn/annualizedVolatility textview to change also
                        intent.putExtra("ticker" + String.valueOf(i+1), String.valueOf(tickerEditTextList.get(i).getText())); // for e.g. ticker1, "MSFT" or ticker2, "GOOGL"
                    } else {
                        intent.removeExtra("ticker" + String.valueOf(i+1)); // remove extra if there is an existing value set
                    }
                }

                if (numTickers != 0) { // start service if number of tickers entered by user is not 0
                    intent.putExtra("numTickers", numTickers);
                    startService(intent);
                    Log.v("myservice ", "started");
                }

                // if the editText is empty, don't show waiting for data
                for (int i = 0; i < aVolatilityTxtList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        aVolatilityTxtList.get(i).setText("Waiting for data.. ");
                    } else {
                        aVolatilityTxtList.get(i).setText("N/A");
                    }
                }

                for (int i = 0; i < aReturnTxtList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        aReturnTxtList.get(i).setText("Waiting for data.. ");
                    } else {
                        aReturnTxtList.get(i).setText("N/A");
                    }
                }
            }
        });

        // create broadcast receiver and register receivers
        myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
        registerReceiver(myBroadcastReceiver, new IntentFilter("CHECK_TICKER"));
        registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_COMPLETE")); // register broadcast receiver to get informed that data is downloaded so that we can calc
        registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_FAILED"));

        // calculate downloaded button onclick event listener
        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("CHECK_TICKER");

                for (int i = 0; i < tickerEditTextList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        intent.putExtra("ticker" + String.valueOf(i+1), String.valueOf(tickerEditTextList.get(i).getText()));
                    } else {
                        intent.removeExtra("ticker" + String.valueOf(i+1)); // remove extra if there is an existing value set
                    }

                }

                // if the editText is empty, don't show waiting for data
                for (int i = 0; i < aVolatilityTxtList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        aVolatilityTxtList.get(i).setText("Waiting for data.. ");
                    } else {
                        aVolatilityTxtList.get(i).setText("N/A");
                    }
                }

                for (int i = 0; i < aReturnTxtList.size(); i++) {
                    if (!tickerEditTextList.get(i).getText().toString().matches("")) {
                        aReturnTxtList.get(i).setText("Waiting for data.. ");
                    } else {
                        aReturnTxtList.get(i).setText("N/A");
                    }
                }

                sendBroadcast(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }
}