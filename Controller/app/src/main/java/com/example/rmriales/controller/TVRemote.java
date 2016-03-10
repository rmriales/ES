package com.example.rmriales.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class TVRemote extends AppCompatActivity {

    private BluetoothAdapter BTAdapter = null;
    private  BluetoothSocket BTSocket = null;
    private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final String address = "20:15:08:10:80:91";
    private ConnectedThread cThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvremote);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = BTAdapter.getRemoteDevice(address);


        try {
            BTSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed.", Toast.LENGTH_LONG).show();
        }
        try {
            BTSocket.connect();
        } catch (IOException e2) {
            try {
                BTSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                BTSocket.connect();
            }
            catch (Exception e3) {
                Log.e("", "Couldn't establish Bluetooth connection!");
            }

        }

        cThread = new ConnectedThread(BTSocket);
        cThread.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    public void onClick (View v){
        switch(v.getId()){
            case R.id.dirLeft:
                cThread.write("f");
                break;
            case R.id.dirUp:
                cThread.write("u");
                break;
            case R.id.dirDown:
                cThread.write("d");
                break;
            case R.id.dirRight:
                cThread.write("r");
                break;
            case R.id.OKAY:
                cThread.write("o");
                break;
            case R.id.Apps:
                cThread.write("v");
                break;
            case R.id.Netflix:
                cThread.write("n");
                break;
            case R.id.AmazonPrime:
                cThread.write("a");
                break;
            case R.id.backButton:
                cThread.write("b");
                break;
            case R.id.exitButton:
                cThread.write("x");
                break;
            case R.id.PowerButton:
                cThread.write("p");
                break;
            case R.id.inputButton:
                cThread.write("i");
                break;
            case R.id.back:
                cThread = null;
                try {
                    BTSocket.close();
                }catch(IOException e){
                    Toast.makeText(getBaseContext(), "Socket could not be closed.", Toast.LENGTH_LONG).show();
                }
                finish();
                break;
        }
    }

    private class ConnectedThread extends Thread {
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { Toast.makeText(getBaseContext(), "Can't get Output Stream.", Toast.LENGTH_LONG).show();}

            mmOutStream = tmpOut;
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}


