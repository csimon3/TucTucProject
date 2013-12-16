package com.solucionjava.tuctuc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csimon on 19/11/13.
 */
public class History extends Activity {

    private String noTuc;
    private String owner;
    private DBTools dbTools;
    private Intent myIntent;
    private TableLayout tucTableScrollView;
    private TextView histTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbTools = new DBTools(this);
        setContentView(R.layout.history);
        //System.out.println("onCreate history");
        myIntent = getIntent();
        noTuc = myIntent.getStringExtra("noTucHist");
        owner= myIntent.getStringExtra("ownerHist");
        histTitleView = (TextView) findViewById(R.id.historyTitleView);
        Context cont = getApplicationContext();
        histTitleView.setText(cont.getString(R.string.historyCard)+" "+noTuc+" "+cont.getString(R.string.de)+" "+owner);
        tucTableScrollView = (TableLayout) findViewById(R.id.tucTableScrollView);
        getTucHistory();

    }

    private void getTucHistory() {
        //System.out.println("updateSavedTucList");
        ArrayList<HashMap<String, String>> tucList = dbTools.getTucHist(noTuc);
        tucTableScrollView.removeAllViews();
        // Display saved tuc list
        int i = 0;
        for (HashMap<String, String> s : tucList) {
            insertTucInScrollView(s, i++);
        }
        if (i==0){
            Context cont = getApplicationContext();
            HashMap<String, String> tucMap = new HashMap<String, String>();
            tucMap.put("tucId","0" );
            tucMap.put("noTuc", noTuc);
            tucMap.put("owner", "");
            tucMap.put("saldo", "--");
            tucMap.put("fecha", cont.getString(R.string.noHistorty));
            insertTucInScrollView(tucMap, 0);
        }
    }
    private int insertTucInScrollView(HashMap<String, String> tuc, int arrayIndex) {

        // Get the LayoutInflator service
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use the inflater to inflate a tuc row from tuc__row.xml
        View newTucRow = inflater.inflate(R.layout.tuchist_row, null);

        // Create the TextView for the ScrollView Row
        TextView tucId = (TextView) newTucRow.findViewById(R.id.tucId);
        TextView fechaTextView = (TextView) newTucRow.findViewById(R.id.historyFechaTextView);
        TextView saldoTextView = (TextView) newTucRow.findViewById(R.id.historySaldoTextView);

        // Add the tuc symbol to the TextView
        tucId.setText(tuc.get("tucId"));
        fechaTextView.setText(tuc.get("fecha")+"    ");
        saldoTextView.setText("    "+tuc.get("saldo")+" C$");

        // Add the new components for the tuc to the TableLayout
        tucTableScrollView.addView(newTucRow);

        //System.out.println("Added : "+tuc.get("noTuc")+" -- "+arrayIndex);
        return arrayIndex;
    }



}
