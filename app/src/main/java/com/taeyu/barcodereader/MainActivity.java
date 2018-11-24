package com.taeyu.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.taeyu.barcodereader.utils.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MainActivity.verifyStoragePermissions(MainActivity.this);

        // getting barcode instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);

    }

    @Override
    public void onScanned(final Barcode barcode) {
        Log.e(TAG, "onScanned: " + barcode.displayValue);
        barcodeReader.playBeep();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "Barcode: " + barcode.displayValue, Toast.LENGTH_SHORT).show();
                showConfirmMessage(barcode.displayValue);
            }
        });
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        Log.e(TAG, "onScannedMultiple: " + barcodes.size());

        String codes = "";
        for (Barcode barcode : barcodes) {
            codes += barcode.displayValue + ", ";
        }

        final String finalCodes = codes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "Barcodes: " + finalCodes, Toast.LENGTH_SHORT).show();
                showConfirmMessage(finalCodes);
            }
        });
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(), "Camera permission denied!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void showConfirmMessage(final String strCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("확인");
        String strMessage = "Detected 바코드: " + strCode + "\n저장하시겠습니까?";
        builder.setMessage(strMessage);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                if (writeToFile(strCode)) {
                    Toast.makeText(getApplicationContext(), "바코드 (" + strCode + ") 저장 완료되었습니다!" +
                                   "\n(" + Environment.getExternalStorageDirectory() + "/Barcodes/)",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "바코드 (" + strCode + ") 저장 할수없습니다!",
                            Toast.LENGTH_LONG).show();
                }
                /*Intent intent = getIntent();
                finish();
                startActivity(intent);*/
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean writeToFile(String data) {
        String strDate = Util.getDate();
        String strToday = Util.convertToDate(strDate);
        String strTime = Util.convertToTimeSimple(strDate);
        data = strTime + "," + data;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Barcodes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, strToday + ".csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(data);
            writer.newLine();
            writer.flush();
            writer.close();
            return true;
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return false;
    }

    private boolean writeToFile1(String data) {
        String strDate = Util.getDate();
        String strToday = Util.convertToDate(strDate);
        String strTime = Util.convertToTimeSimple(strDate);
        data = strTime + "," + data;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Barcodes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, strToday + ".csv");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(data);
            writer.flush();
            writer.close();
            return true;
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return false;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}
