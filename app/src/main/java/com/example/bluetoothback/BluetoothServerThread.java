package com.example.bluetoothback;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServerThread extends Thread {

    protected String TAG = "BluetoothServerThread";

    // "00001101-0000-1000-8000-00805f9b34fb" = SPP (シリアルポートプロファイル) の UUID.
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    MainActivity mActivity = null;
    BluetoothAdapter mBluetoothAdapter;

    // 待ち受け対象のデバイス名
    String mBluetoothDeviceName;

    BluetoothServerSocket bluetoothServerSocket;
    BluetoothSocket bluetoothSocket;
    InputStream inputStream;
    OutputStream outputStream;

    public BluetoothServerThread(MainActivity activity, String bluetoothDeviceName) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivity = activity;
        mBluetoothDeviceName = bluetoothDeviceName;

        MainActivity.print(TAG, "接続デバイス " + mBluetoothDeviceName);

    }

    public void run() {

        byte[] incomingBuff = new byte[30000];
        MainActivity.print(TAG, "start server thread.");

        try {
            while (true) {

                if (Thread.interrupted()) {
                    break;
                }

                try {

                    // 権限チェック。コンパイルエラー対策
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    bluetoothServerSocket
                            = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            mBluetoothDeviceName, SPP_UUID);

                    // 待ち受け処理
                    bluetoothSocket = bluetoothServerSocket.accept();
                    bluetoothServerSocket.close();
                    bluetoothServerSocket = null;

                    inputStream = bluetoothSocket.getInputStream();
                    outputStream = bluetoothSocket.getOutputStream();

                    while (true) {

                        if (Thread.interrupted()) {
                            break;
                        }

                        int incomingBytes = inputStream.read(incomingBuff);
                        byte[] buff = new byte[incomingBytes];
                        MainActivity.print(TAG, "recived length=" + buff.length);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bluetoothSocket != null) {
                    try {
                        bluetoothSocket.close();
                        bluetoothSocket = null;
                    } catch (IOException e) {}
                }

                // Bluetooth connection broke. Start Over in a few seconds.
                Thread.sleep(3 * 1000);
            }
        }
        catch(InterruptedException e){
            MainActivity.printErr(TAG, "Cancelled ServerThread.", e);
        }
        MainActivity.print(TAG, "stop server thread.");
    }
}
