package com.home.balconia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    //Attributes
    private TextView connTxt;
    private ColorPicker colorPicker;
    private boolean stopThread;
    private Button whiteBtn,offBtn,fireBtn,prideBtn,randomBtn,bodegaBtn,gamerBtn,customBtn,setBtn,backBtn;

    //Bluetooth Attributes
    private final UUID UUID_PORT = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice balconia;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!bluetoothAdapter.isEnabled()){
            turnBTOn();
        }
        setUI();

        if(!initConnection()) {
            connTxt.setText("Not Connected");
            return;
        }
        beginListenForData();

    }//end onCreate()

    private void setUI(){
        connTxt = findViewById(R.id.connTxt);
        colorPicker = findViewById(R.id.picker);
        colorPicker.setShowOldCenterColor(false);
        colorPicker.setVisibility(View.INVISIBLE);
        colorPicker.setOnColorChangedListener(color -> {
            String pickedColor = "custom|"+Integer.toHexString(colorPicker.getColor());
            sendData(pickedColor);
        });


        whiteBtn = (Button)findViewById(R.id.whiteBtn);
        offBtn = (Button)findViewById(R.id.offBtn);
        fireBtn = (Button)findViewById(R.id.fireBtn);
        randomBtn = (Button)findViewById(R.id.randomBtn);
        prideBtn = (Button)findViewById(R.id.prideBtn);
        bodegaBtn = (Button)findViewById(R.id.bodegaBtn);
        gamerBtn = (Button)findViewById(R.id.gamerBtn);
        customBtn = (Button)findViewById(R.id.customBtn);
        setBtn = (Button)findViewById(R.id.setBtn);
        backBtn = (Button)findViewById(R.id.backBtn);

        whiteBtn.setVisibility(View.VISIBLE);
        offBtn.setVisibility(View.VISIBLE);
        fireBtn.setVisibility(View.VISIBLE);
        prideBtn.setVisibility(View.VISIBLE);
        randomBtn.setVisibility(View.VISIBLE);
        bodegaBtn.setVisibility(View.VISIBLE);
        customBtn.setVisibility(View.VISIBLE);
        gamerBtn.setVisibility(View.VISIBLE);
        setBtn.setVisibility(View.INVISIBLE);
        backBtn.setVisibility(View.INVISIBLE);
    }

    public void onClickWhite(View v){
        sendData("white");
    }

    public void onClickOff(View v){
        sendData("off");
    }

    public void onClickFire(View v){
        sendData("fire");
    }

    public void onClickPride(View v){
        sendData("pride");
    }

    public void onClickBodega(View v){
        sendData("bodega");
    }

    public void onClickRandom(View v){
        sendData("random");
    }

    public void onClickGamer(View v){
        sendData("gamer");
    }

    public void onClickCustom(View v){
        whiteBtn.setVisibility(View.INVISIBLE);
        offBtn.setVisibility(View.INVISIBLE);
        fireBtn.setVisibility(View.INVISIBLE);
        prideBtn.setVisibility(View.INVISIBLE);
        randomBtn.setVisibility(View.INVISIBLE);
        bodegaBtn.setVisibility(View.INVISIBLE);
        customBtn.setVisibility(View.INVISIBLE);
        gamerBtn.setVisibility(View.INVISIBLE);

        colorPicker.setVisibility(View.VISIBLE);
        setBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
    }

    public void onClickSet(View v){
        String str = "custom|"+Integer.toHexString(colorPicker.getColor());
//        System.out.println(str);
        sendData(str);
    }

    public void onClickBack(View v){
        whiteBtn.setVisibility(View.VISIBLE);
        offBtn.setVisibility(View.VISIBLE);
        fireBtn.setVisibility(View.VISIBLE);
        prideBtn.setVisibility(View.VISIBLE);
        randomBtn.setVisibility(View.VISIBLE);
        bodegaBtn.setVisibility(View.VISIBLE);
        customBtn.setVisibility(View.VISIBLE);
        gamerBtn.setVisibility(View.VISIBLE);

        colorPicker.setVisibility(View.INVISIBLE);
        setBtn.setVisibility(View.INVISIBLE);
        backBtn.setVisibility(View.INVISIBLE);
    }

    public void onClickConnect(View v){
        if(!socket.isConnected()){
            initConnection();
        }else{
            Toast.makeText(getApplicationContext(),"Already connected",Toast.LENGTH_LONG).show();
        }

    }



    /*============= Bluetooth Methods ==================*/
    @SuppressLint("MissingPermission")
    public void turnBTOn() {
        if (!bluetoothAdapter.isEnabled()) {

            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 1);
            Toast.makeText(getApplicationContext(), "Bluetooth Turned On", Toast.LENGTH_LONG).show();

        }
    }

    @SuppressLint("MissingPermission")
    public void off(View v) {
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    private boolean initConnection(){
        boolean found = false;
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

        for(BluetoothDevice pDevice: pairedDevice){
            if(pDevice.getName().equals("BALCONIA")){
                balconia = pDevice;
                try{
                    socket = balconia.createRfcommSocketToServiceRecord(UUID_PORT);
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    found = true;
                }catch(IOException ex){
                    ex.printStackTrace();
                }
                break;
            }//end if device name == balconia
        }//end for loop pairedDevice
        return found;
    }// end initSwarmConnection()

    private void sendData(String data){
        if(data == null)
            return;

        try {
            outputStream.write(data.getBytes());
        }catch(IOException e) {
            e.printStackTrace();
        }
    }//end sendData

    void beginListenForData(){
        final Handler handler = new Handler();
        stopThread = false;
//        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if(socket == null)
                                return;

                            if(socket.isConnected())
                                connTxt.setText("Connected");
                            else
                                connTxt.setText("Not Connected");

                            stopThread = true;


                        }
                    },3000);
                }
            }
        });

        thread.start();
    }//end beginListeningForData()








}//end MainActivity Class

