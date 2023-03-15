package com.example.bluetoothback;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;


import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothManager {

    public static final String TAG = "BluetoothManager";

    BluetoothAdapter mBluetoothAdapter;

    public static String mSelectedDevieName = null;

    public BluetoothManager(MainActivity activity) {
        activity = activity;
    }

    public void showSelectDeviceDialog(MainActivity activity) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //bluetoothがが、無効な場合は有効化設定をリクエスト　
        if (mBluetoothAdapter.isEnabled() == false) {
            //   ※2020年からstartActivityForResult を使った手法は非推奨
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.getActivityResultLauncher().launch(enableBtIntent);
            // MainActivityのActivityResultLauncherにコールバックされる
            return;
        }

        //権限チェック。コンパイルエラー対策
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList<String> nameList = new ArrayList<String>();
        for (BluetoothDevice device : pairedDevices) {
            MainActivity.print(TAG, "ペアリングデバイス:" + device.getName());
            nameList.add(device.getName());
        }

        final String[] items = nameList.toArray(new String[nameList.size()]);

        // 機器選択ダイアログを表示
        new AlertDialog.Builder(activity)
                .setTitle("Selector")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // item_which pressed
                        mSelectedDevieName = items[which];
                        activity.setDeviceName(mSelectedDevieName);
                    }
                })
                .show();
        return;
    }
}
