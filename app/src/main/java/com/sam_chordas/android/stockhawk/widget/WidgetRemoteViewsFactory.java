package com.sam_chordas.android.stockhawk.widget;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context Context;
    private ArrayList<Stocks> stocks;

    public WidgetRemoteViewsFactory(Context mContext) {
        this.Context = mContext;
        this.stocks = new ArrayList<>();
    }

    private void getStockData() {
        stocks.clear();

        String[] requiredColumns = new String[]
                {QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE, QuoteColumns.ISUP};
        Cursor stockCursor = Context.getContentResolver()
                .query(QuoteProvider.Quotes.CONTENT_URI, requiredColumns,
                        QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);//obtaining the stock details

        if (stockCursor != null) {
            stockCursor.moveToFirst();
            do {
                Stocks stock = new Stocks(stockCursor.getString(0), stockCursor.getString(1), stockCursor.getString(2), stockCursor.getInt(3));
                stocks.add(stock);
            } while (stockCursor.moveToNext());

            stockCursor.close();
        }
    }

    @Override
    public void onCreate() {
        getStockData();
    }

    @Override
    public void onDataSetChanged() {
        getStockData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Stocks stockdetail = stocks.get(position);

        RemoteViews remoteViews = new RemoteViews(Context.getPackageName(), R.layout.widget_individual);
        remoteViews.setTextViewText(R.id.stock_symbol, stockdetail.getStockSymbol());
        remoteViews.setTextViewText(R.id.bid_price, stockdetail.getBidPrice());
        remoteViews.setTextViewText(R.id.change, stockdetail.getPercentChange());



        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}