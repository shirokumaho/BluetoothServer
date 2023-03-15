package com.example.bluetoothback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    protected String TAG = "MainActivity";

    // 選択機器名称表示エリア
    TextView mDeviceNameView = null;

    // コンソール
    static TextView mConsoleView = null;

    //
    BluetoothManager mBluetoothManager = null;

    private static final int PERMISSION_WRITE_EX_STR = 1;

    // bluetooth有効設定リクエストのコールバッククラス
    private ActivityResultLauncher<Intent> mActivityResultLauncher = null;

    private static Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = new BluetoothManager(this);

        mDeviceNameView = ((TextView) findViewById(R.id.device_name_view));

        Button btn = ((Button) findViewById(R.id.select_button));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 権限チェック
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        print(TAG, "BLUETOOTH_CONNECT　権限なし.");
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN,
//                                        Manifest.permission.BLUETOOTH_CONNECT,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                },
                                PERMISSION_WRITE_EX_STR);
                    } else {
                        bluetoothManager.showSelectDeviceDialog(MainActivity.this);
                    }
                } catch (Throwable t) {
                    printErr(TAG, "デバイス選択でエラー",t);
                }
            }
        });

        Button startBtn = ((Button) findViewById(R.id.start_button));
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    print(TAG, "開始ボタンタップ");
                    BluetoothServerThread server = new BluetoothServerThread(MainActivity.this, mDeviceNameView.getText().toString() );
                    server.start();
                } catch (Throwable t) {
                    printErr(TAG, "接続開始エラー",t);
                }
            }
        });

        // スクロール可能Text
        mConsoleView = (TextView) findViewById(R.id.console_view);
        mConsoleView.setMovementMethod(new ScrollingMovementMethod());
        mConsoleView.setText("");
        mHandler = new Handler();

        // bluetooth有効設定リクエストのコールバック処理（権限ではなく、bluetoothをONにしていいですか、の確認
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            //結果を受け取った後の処理
                        } else {
                            //拒否された時の挙動
                        }
                    }
                });
    }

    public ActivityResultLauncher getActivityResultLauncher(){
        return mActivityResultLauncher;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permission, int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if (grantResults.length <= 0) {
            return;
        }
        switch (requestCode) {
            case PERMISSION_WRITE_EX_STR: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /// 許可が取れた場合・・・
                    mBluetoothManager.showSelectDeviceDialog(MainActivity.this);
                } else {
                    /// 許可が取れなかった場合・・・
                    Toast.makeText(this,
                            "アプリを起動できません....", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            return;
        }
    }

    /**
     * 選択したデバイス名称を設定
     * @param name
     */
    public void setDeviceName(String name){
        mDeviceNameView.setText(name);
    }

    /**
     * コンソールとlogcatにログを出力する。
     * @param TAG
     * @param msg
     */
    public static void print(String TAG, String msg) {
        Log.i(TAG,"★"+ msg);
        if (mConsoleView == null) {
            return;
        }
        //メインスレッドのメッセージキューにメッセージを登録します。
        mHandler.post(new Runnable() {
            //run()の中の処理はメインスレッドで動作されます。
            public void run() {
                //画面のtextViewへ"処理が完了しました。"を表示させる。
                mConsoleView.append("★"+ TAG + " : " + msg + "\n");
            }
        });
    }

    /**
     * コンソールとlogcatにログを出力する。エラーのスタックトレースも出力する。
     * @param TAG
     * @param msg
     */
    public static void printErr(String TAG, String msg, Throwable t) {
        StackTraceElement[] list = t.getStackTrace();
        StringBuilder b = new StringBuilder();
        b.append(t.getClass()).append(":").append(t.getMessage()).append("\n");
        for (StackTraceElement s : list) {
            b.append(s.toString()).append("\n");
        }
        Log.e(TAG,b.toString());
        if (mConsoleView == null) {
            return;
        }
        print(TAG ,msg);
        print(TAG ,b.toString());
    }
}