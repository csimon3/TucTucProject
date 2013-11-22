package com.solucionjava.tuctuc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by csimon on 19/11/13.
 */
public class Vecinos extends Activity {

    private final String INDISPONIBLE="No disponible";
    private final String TUC_INVALIDO = "TUC Invalido";
    private int noTuc;
    private Intent myIntent;
    private TextView vecinosTitleView;
    private TextView vecinosResultTextView;
    private TextView vecinosConcluTextView;
    private String owner="";
    private String saldo="";
    private ProgressBar pd;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.vecinos);
        //System.out.println("onCreate Vecinos");
        myIntent = getIntent();
        noTuc = Integer.parseInt(myIntent.getStringExtra("noTucVecino"));
        owner= myIntent.getStringExtra("ownerVecino");
        saldo= myIntent.getStringExtra("saldoVecino");

        vecinosTitleView = (TextView) findViewById(R.id.vecinosTitleView);
        vecinosResultTextView = (TextView) findViewById(R.id.vecinosResultTextView);
        vecinosConcluTextView = (TextView) findViewById(R.id.vecinosConcluTextView);
        Context cont = getApplicationContext();
        vecinosTitleView.setText(cont.getString(R.string.misVecinos)+" "+owner+" ("+saldo+" C$)");
        vecinosResultTextView.setText(cont.getString(R.string.misVecinosConsulta));
        pd = (ProgressBar) findViewById(R.id.progressBar);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        ImageView image = (ImageView) layout.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_launcher);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(R.string.misVecinosConsulta);

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL, 0, 30);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        new getVecinos().execute();
    }


    private class getVecinos extends AsyncTask<String, String, String> {
        double total=0;
        int invalido=0;
        int enZero=0;
        int conSaldo=0;
        int sinRed=0;
        public getVecinos (){
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toast.show();
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                int pgBar=1;
                pd.setProgress(pgBar*10);
                for (int i=noTuc-5; i<noTuc+6;i++){
                    if (i==noTuc) continue;
                    pgBar++;
                    pd.setProgress(pgBar*10);
                    String tuc = ("00000000" + i);
                    tuc = tuc.substring(tuc.length() - 8);
                    String urlParameters = "_funcion=1&_terminal=" + tuc;
                    String curSaldo = getSaldo(tuc, urlParameters);
                    if (curSaldo.equals(INDISPONIBLE)){
                        sinRed++;
                    } else
                        if(curSaldo.equals(TUC_INVALIDO)){
                            invalido++;
                        } else {
                            if (Double.parseDouble(curSaldo)==0){
                                enZero++;
                            } else {
                                total+=Double.parseDouble(curSaldo);
                                conSaldo++;
                            }
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result){
            setProgressBarIndeterminateVisibility(false);
            toast.cancel();
            pd.setVisibility(View.GONE);
            //System.out.println("total = "+total);
            //System.out.println("invalido = "+invalido);
            //System.out.println("enZero = "+enZero);
            //System.out.println("conSaldo = "+conSaldo);
            //System.out.println("sinRed = "+sinRed);
            Context cont = getApplicationContext();
            vecinosResultTextView.setText("\n"+cont.getString(R.string.misVecinosExplain)+"\n"+"\n"+cont.getString(R.string.misVecinosSinRed)+" "+sinRed+"\n"+
                    cont.getString(R.string.misVecinosInvalid)+" "+invalido+"\n"+cont.getString(R.string.misVecinosEnZero)+" "+enZero+"\n"+
                    cont.getString(R.string.misVecinosConSaldo)+" "+conSaldo+"\n"+
                    cont.getString(R.string.misVecinosTotal)+" "+String.format("%.02f",total, Locale.US)+"\n"+
                    cont.getString(R.string.misVecinosPromedio)+" "+String.format("%.02f",(total/conSaldo), Locale.US)+"\n"+
                    cont.getString(R.string.misVecinosPromedioZero)+" "+String.format("%.02f",(total/(conSaldo+enZero)), Locale.US)+"\n"+"\n" );

            //System.out.println("MI saldo = "+saldo);
            try{
                double miSaldo = Double.parseDouble(saldo.replace(" C$",""));
                if (total/(conSaldo+enZero)>miSaldo){
                    vecinosConcluTextView.setText(cont.getString(R.string.misVecinosMasRico));
                } else {
                    vecinosConcluTextView.setText(cont.getString(R.string.misVecinosMasPobre) );
                }
            } catch (NumberFormatException e) {
                vecinosConcluTextView.setText(cont.getString(R.string.misVecinosSaldoDesc) );
            }
        }

        private String getSaldo(String noTuc, String urlParameters){
            String saldo=INDISPONIBLE;
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
                //System.out.println("Resultado para TUC "+noTuc+" :"+line);
                if (line.startsWith("{\"Error\":true,")){
                    saldo=TUC_INVALIDO;
                } else {
                    line = line.substring(47).replace("\"}", "");
                    //System.out.println(noTuc+" = " + line);
                    saldo=line;
                }
                con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                saldo=INDISPONIBLE;
            }
            return saldo;
        }
    }

}
