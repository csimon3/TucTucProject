package com.solucionjava.tuctuc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by csimon on 24/11/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class About extends DialogFragment {

    TextView tv1;
    TextView tv2;
    TextView sourceCode;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View content = factory.inflate(R.layout.about, null);
        tv2 = (TextView) content.findViewById(R.id.version);
        String packageName = "com.solucionjava.tuctuc";
        int versionNumber = 0;
        try {
            PackageInfo pi = getActivity().getApplicationContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            versionNumber = pi.versionCode;
            String versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        tv2.setText(getActivity().getString(R.string.version) +" "+ versionNumber);
        sourceCode = (TextView) content.findViewById(R.id.sourceCode);
        sourceCode.setText(getActivity().getString(R.string.sourceCode) +"https://github.com/csimon3/TucTucProject");
        tv1 = (TextView) content.findViewById(R.id.createdBy);
        tv1.setText(getActivity().getString(R.string.createdBy));

        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(getActivity());
        //  now customize your dialog
        dialog.setTitle(getActivity().getString(R.string.action_about)+" "+getActivity().getString(R.string.app_name))
                .setView(content)
                .setCancelable(true) //this is the default I think.
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do something
                    }
                });

        return dialog.create();
    }
}
