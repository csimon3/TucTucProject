package com.solucionjava.tuctuc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.app.DialogFragment;
//import android.support.v4.app.DialogFragment;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    EditText newTucEdit;
    EditText newTucOwner;
    ImageButton addTucButton;
    Button deleteAllButton;
    Button updateAllButton;
    private TableLayout tucTableScrollView;
    private DBTools dbTools;
    private final String INDISPONIBLE = "No disponible";
    private final String TUC_INVALIDO = "TUC Invalido";
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SharedPreferences pref = this.getSharedPreferences("example_list",MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //System.out.println("onCreate");
        dbTools = new DBTools(this);

        // Initialize Components
        newTucEdit = (EditText) findViewById(R.id.newTucEdit);
        newTucOwner = (EditText) findViewById(R.id.newTucOwner);
        addTucButton = (ImageButton) findViewById(R.id.addTucButton);
        deleteAllButton = (Button) findViewById(R.id.deleteAllButton);
        updateAllButton = (Button) findViewById(R.id.updateAllButton);
        tucTableScrollView = (TableLayout) findViewById(R.id.tucTableScrollView);

        // Add ClickListeners to the buttons
        addTucButton.setOnClickListener(enterTucButtonListener);
        deleteAllButton.setOnClickListener(deleteTucsButtonListener);
        updateAllButton.setOnClickListener(updateTucsButtonListener);

        // Force the keyboard to close
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newTucEdit.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(newTucOwner.getWindowToken(), 0);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.

    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add saved tucs to the Tuc Scrollview
        updateSavedTucList();
    }

    private void updateSavedTucList() {
        //System.out.println("updateSavedTucList");
        ArrayList<HashMap<String, String>> tucList = dbTools.getAllTucs();
        tucTableScrollView.removeAllViews();
        // Display saved tuc list
        int i = 0;
        for (HashMap<String, String> s : tucList) {
            int lineId = insertTucInScrollView(s, i++);
            View newTucRow = tucTableScrollView.getChildAt(lineId);
            TextView currSaldo = (TextView) newTucRow.findViewById(R.id.tucSaldoTextView);
            if (currSaldo.getText() == null || currSaldo.getText().length() == 1 || currSaldo.getText().toString().equals(getApplicationContext().getString(R.string.pending))) {
                //System.out.println("Updating saldo for line "+lineId);
                new UpdateSaldo(lineId).execute();
            }

        }
    }

    private void saveTucSymbol(String newTuc, String owner) {
        // If this is a new tuc add its components
        String tuc = ("00000000" + newTuc);
        tuc = tuc.substring(tuc.length() - 8);
        if (dbTools.getTucId(tuc) == 0) {
            HashMap<String, String> newTucMap = new HashMap<String, String>();
            newTucMap.put("noTuc", tuc);
            newTucMap.put("owner", owner);
            newTucMap.put("saldo", "");
            newTucMap.put("lastUpdate", "");
            dbTools.insertTuc(newTucMap);
            updateSavedTucList();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.duplicated_tuc_title);
            builder.setPositiveButton(R.string.ok, null);
            builder.setMessage(R.string.duplicated_tuc_text);
            AlertDialog theAlertDialog = builder.create();
            theAlertDialog.show();
        }

    }

    private int insertTucInScrollView(HashMap<String, String> tuc, int arrayIndex) {

        // Get the LayoutInflator service
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use the inflater to inflate a tuc row from tuc__row.xml
        View newTucRow = inflater.inflate(R.layout.tuc_row, null);

        // Create the TextView for the ScrollView Row
        TextView tucId = (TextView) newTucRow.findViewById(R.id.tucId);
        TextView newTucTextView = (TextView) newTucRow.findViewById(R.id.noTucTextView);
        TextView newTucOwnerTextView = (TextView) newTucRow.findViewById(R.id.tucOwnerTextView);
        TextView newTucSaldoTextView = (TextView) newTucRow.findViewById(R.id.tucSaldoTextView);

        // Add the tuc symbol to the TextView
        tucId.setText(tuc.get("tucId"));
        newTucTextView.setText(tuc.get("noTuc"));
        newTucOwnerTextView.setText(tuc.get("owner"));
        if (tuc.get("saldo").equals(INDISPONIBLE) || tuc.get("saldo").equals(TUC_INVALIDO)) {
            newTucSaldoTextView.setText(tuc.get("saldo") + "\n" + tuc.get("lastUpdate"));
        } else {
            if (tuc.get("saldo").length() > 2) {
                newTucSaldoTextView.setText(tuc.get("saldo") + " C$\n" + tuc.get("lastUpdate"));
            } else {
                newTucSaldoTextView.setText(R.string.pending);
            }
        }

        ImageButton tucButton = (ImageButton) newTucRow.findViewById(R.id.tucDelButton);
        tucButton.setOnClickListener(delOneTucButtonListener);

        // Add the new components for the tuc to the TableLayout
        tucTableScrollView.addView(newTucRow);

        //System.out.println("Added : "+tuc.get("noTuc")+" -- "+arrayIndex);
        return arrayIndex;
    }


    public View.OnClickListener enterTucButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View theView) {

            // If there is a tuc symbol entered into the EditText
            // field
            if (newTucEdit.getText().length() > 0) {

                // Save the new tuc and add its components
                saveTucSymbol(newTucEdit.getText().toString(), newTucOwner.getText().toString());

                newTucEdit.setText(""); // Clear EditText box
                newTucOwner.setText(""); // Clear EditText box
                newTucEdit.requestFocus();

                // Force the keyboard to close
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newTucEdit.getWindowToken(), 0);
            } else {
                showAlert(R.string.invalid_tuc, R.string.missing_tuc);
            }

        }

    };

    private void deleteAllTucs() {
        askConfirmDeleteAll(R.string.removeConfirmTitle, R.string.removeConfirmMessage);
    }

    private void deleteOneTuc(String tucId) {
        dbTools.deleteTuc(Integer.parseInt(tucId));
        updateSavedTucList();
    }

    public View.OnClickListener deleteTucsButtonListener = new View.OnClickListener() {


        public void onClick(View v) {
            deleteAllTucs();
        }

    };

    public View.OnClickListener delOneTucButtonListener = new View.OnClickListener() {


        public void onClick(View v) {
            TableRow tableRow = (TableRow) v.getParent();

            TextView tucTextView = (TextView) tableRow.findViewById(R.id.tucId);
            String tucId = tucTextView.getText().toString();
            ////System.out.println("Try Deleting tuc "+tucId+" ++ "+tableRow.getChildCount());
            deleteOneTuc(tucId);
        }

    };

    public View.OnClickListener updateTucsButtonListener = new View.OnClickListener() {

        public void onClick(View v) {
            //System.out.println("isNetworkAvailable()="+isNetworkAvailable());
            if (isNetworkAvailable()) {
                for (int i = 0; i < tucTableScrollView.getChildCount(); i++) {
                    new UpdateSaldo(i).execute();
                }
            } else {
                showAlert(R.string.sinConnTitle, R.string.sinConnMesg);
            }
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            /*case R.id.action_settings:
                Intent newIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(newIntent);
                return true;*/
            case R.id.action_exit:
                Toast myToast = Toast.makeText(this,R.string.chao, Toast.LENGTH_SHORT);
                myToast.show();
                moveTaskToBack(true);
                return true;
            case R.id.action_about:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    DialogFragment dialog = new About();
                    dialog.show(getFragmentManager(), "test");
                } else {
                    showAlert(R.string.about, R.string.createdBy);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class UpdateSaldo extends AsyncTask<String, String, String> {
        int lineId;
        String saldo;

        public UpdateSaldo(int lineId) {
            super();
            this.lineId = lineId;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                int i = lineId;
                View newTucRow = tucTableScrollView.getChildAt(i);
                //add request header
                if (newTucRow != null) {
                    TextView currTuc = (TextView) newTucRow.findViewById(R.id.noTucTextView);
                    //System.out.println("ID = "+currTuc.getText());
                    String tuc = ("00000000" + currTuc.getText());
                    tuc = tuc.substring(tuc.length() - 8);
                    String urlParameters = "_funcion=1&_terminal=" + tuc;
                    saldo = getSaldo(tuc, urlParameters);
                    HashMap<String, String> newTucMap = new HashMap<String, String>();
                    newTucMap.put("noTuc", tuc);
                    newTucMap.put("lastUpdate", getCurrDate());
                    newTucMap.put("saldo", saldo);
                    dbTools.updateSaldoTuc(newTucMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            View newTucRow = tucTableScrollView.getChildAt(lineId);
            TextView currSaldo = (TextView) newTucRow.findViewById(R.id.tucSaldoTextView);
            //System.out.println ("old saldo = "+currSaldo.getText());
            if (currSaldo.getText().length() > 1 && (saldo == null || saldo.equals(INDISPONIBLE))) {
                //System.out.println("Not updated since no disponible (saldo="+saldo);
            } else {
                if (saldo.equals(INDISPONIBLE) || saldo.equals(TUC_INVALIDO)) {
                    currSaldo.setText(saldo + "\n" + getCurrDate());
                } else {
                    currSaldo.setText(saldo + " C$\n" + getCurrDate());
                }
            }
            //updateSavedTucList();
        }

        private String getSaldo(String noTuc, String urlParameters) {
            saldo = INDISPONIBLE;
            try {
                String url = "http://www.mpeso.net/datos/consulta.php";
                URL obj = new URL(url);
                // Send post request
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                System.out.println(line);
                if (line.startsWith("{\"Error\":true,")) {
                    saldo = TUC_INVALIDO;
                } else {
                    if (line.indexOf("HTML")>-1) {
                    saldo = INDISPONIBLE;
                } else
                    line = line.substring(47).replace("\"}", "");
                    //System.out.println(noTuc+" = " + line);
                    saldo = line;
                }
                con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                saldo = INDISPONIBLE;
            }
            return saldo;
        }
    }

    private String getCurrDate() {
        String currDate;
        GregorianCalendar cal = new GregorianCalendar();
        currDate = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + "  " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + " " + ((cal.get(Calendar.AM_PM) == 1) ? "PM" : "AM");
        return currDate;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showAlert(int title, int message) {
        // Create an alert dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Set alert title
        builder.setTitle(title);

        // Set the value for the positive reaction from the user
        // You can also set a listener to call when it is pressed
        builder.setPositiveButton(R.string.ok, null);

        // The message
        builder.setMessage(message);

        // Create the alert dialog and display it
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
    }

    private void askConfirmDeleteAll(int title, int message) {
        // Create an alert dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Set alert title
        builder.setTitle(title);

        // Set the value for the positive reaction from the user
        // You can also set a listener to call when it is pressed
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dbTools.deleteAllTuc();
                // Delete all the tucs stored in the TableLayout
                tucTableScrollView.removeAllViews();
            }
        });


        // The message
        builder.setMessage(message);

        // Create the alert dialog and display it
        AlertDialog theAlertDialog = builder.create();
        theAlertDialog.show();
    }

    public void verVecinos(View view) {
        if (isNetworkAvailable()) {
            TableRow tableRow = (TableRow) view.getParent();
            TextView tucTextView = (TextView) tableRow.findViewById(R.id.tucId);
            String tucId = tucTextView.getText().toString();
            HashMap<String, String> myTuc = dbTools.getTucInfo(Integer.parseInt(tucId));
            Intent myIntent = new Intent(getApplicationContext(), Vecinos.class);
            myIntent.putExtra("noTucVecino", myTuc.get("noTuc"));
            myIntent.putExtra("ownerVecino", myTuc.get("owner"));
            myIntent.putExtra("saldoVecino", myTuc.get("saldo"));
            startActivity(myIntent);
        } else {
            showAlert(R.string.sinConnTitle, R.string.sinConnMesg);
        }
    }
    public void verHistory(View view) {
        TableRow tableRow = (TableRow) view.getParent();
        TextView tucTextView = (TextView) tableRow.findViewById(R.id.tucId);
        String tucId = tucTextView.getText().toString();
        HashMap<String, String> myTuc = dbTools.getTucInfo(Integer.parseInt(tucId));
        Intent myIntent = new Intent(getApplicationContext(), History.class);
        myIntent.putExtra("noTucHist", myTuc.get("noTuc"));
        myIntent.putExtra("ownerHist", myTuc.get("owner"));
        myIntent.putExtra("saldoHist", myTuc.get("saldo"));
        startActivity(myIntent);
    }
}
