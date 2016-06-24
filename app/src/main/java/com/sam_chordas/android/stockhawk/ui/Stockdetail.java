package com.sam_chordas.android.stockhawk.ui;


import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class Stockdetail  extends AppCompatActivity {
    static String stock_symbol;

    ValueLineChart chart;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);           //for checking the network connection
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockdetail);
        Intent intent = getIntent();
        chart = (ValueLineChart) findViewById(R.id.linechart);
        stock_symbol = intent.getStringExtra("stock");
        setTitle(stock_symbol);
        getData(stock_symbol);
        if (isOnline()) {
            getData(stock_symbol);
        } else
            Toast.makeText(this, "Network not available", Toast.LENGTH_LONG);

    }


    public void getData(String stock_symbol) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://chartapi.finance.yahoo.com/instrument/1.0/" + stock_symbol + "/chartdata;type=quote;range=7y/json").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toast.makeText(getApplicationContext(), "Not able to fetch data", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    String info = response.body().string();
                    if (info.startsWith("finance_charts_json_callback(")) {             //checking for the correct data
                        info = info.substring(29, info.length() - 2);
                    }
                    try {
                        JSONObject object = new JSONObject(info);
                        String name = object.getJSONObject("meta").getString("Company-Name");
                        final ValueLineSeries valueLineSeries = new ValueLineSeries();
                        final JSONArray jsonArray = object.getJSONArray("series");          //getting the data of the share
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject temp = jsonArray.getJSONObject(i);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                            String date = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext()).format(simpleDateFormat.parse(temp.getString("Date")));
                            valueLineSeries.addPoint(new ValueLinePoint(date, Float.parseFloat(temp.getString("close"))));
                            valueLineSeries.setColor(R.color.red);

                        }
                        Stockdetail.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chart = (ValueLineChart) findViewById(R.id.linechart);
                                chart.addSeries(valueLineSeries);//adding the points to the chart, and populating it....
                                chart.startAnimation();//starting the animation

                            }


                        });
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
    }
}