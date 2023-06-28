package com.home.balconia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //Attributes
    private TextView connTxt;

    //Bluetooth Attributes
    private final UUID UUID_PORT = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice balconia;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;
    private OutputStream outputStream;

    private String chosenSwarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connTxt = findViewById(R.id.connTxt);

        if(!bluetoothAdapter.isEnabled()){
            turnBTOn();
        }
        if(initConnection())
            connTxt.setText("Connected");

    }

    public void onClickWhite(View v){
        sendData("w");
    }

    public void onClickOff(View v){
        sendData("o");
    }

    public void onClickFire(View v){
        sendData("f");
    }

    public void onClickPride(View v){
        sendData("p");
    }

    public void onClickBodega(View v){
        sendData("b");
    }

    public void onClickRandom(View v){
        sendData("x");
    }

    public void onClickGamer(View v){
        sendData("g");
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
            }//end if device name == chosenSwarm
        }//end for loop pairedDevice
        return found;
    }// end initSwarmConnection()

    private void sendData(String data){
        if(data == null || !socket.isConnected())
            return;
        try {
            outputStream.write(data.getBytes());

        }catch(IOException e){
            e.printStackTrace();
        }


    }//end sendData


}