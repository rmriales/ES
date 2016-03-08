package com.example.rmriales.lightandtvcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

//AppCompatActivity
public class MainMenu extends AppCompatActivity {

    private static final String timeoutState = "timeoutState";

    private String time;
    private int radioState;

    private BluetoothAdapter BTAdapter = null;
    private BluetoothSocket BTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThread cThread;
    private static String address;


    RadioButton enabled;
    RadioButton disabled;
    EditText countdown;
    RadioGroup radioGroup;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        if (savedInstanceState == null) {
            time = "5";
        } else {
            time = savedInstanceState.getString(timeoutState);
        }


        enabled = (RadioButton) findViewById(R.id.enableRadioButton);
        disabled = (RadioButton) findViewById(R.id.disableRadioButton);
        countdown = (EditText) findViewById(R.id.timeOutEditText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        addListenerToRadios();


        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        if(address != null) {
            try {
                BluetoothDevice device = BTAdapter.getRemoteDevice(address);
                try {
                    BTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Socket creation failed.", Toast.LENGTH_LONG).show();
                }
                try {
                    BTSocket.connect();
                } catch (IOException e2) {
                    Toast.makeText(getBaseContext(), "Connection could not be made.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Failed to get valid address", Toast.LENGTH_LONG).show();
            }

            cThread = new ConnectedThread(BTSocket);
            cThread.start();


            try {
                cThread.write("1"); //check if device is connected
            } catch (NullPointerException e) {
                Toast.makeText(getBaseContext(), "Null Bluetooth Address", Toast.LENGTH_LONG).show();
            }
        }
    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    public void onClick (View v){
        switch(v.getId()){
            case R.id.button:
                time = countdown.getText().toString();
                time = "t"+time;
                cThread.write(time);
                break;
            case R.id.lightOffButton:
                cThread.write("l0");
                break;
            case R.id.lightOnButton:
                cThread.write("l1");
            case R.id.remote:
                break;
            case R.id.BTConnect:
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
    }


    public void onResume() {
        super.onResume();

        Intent intent = getIntent();


        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        if(address != null){
        BluetoothDevice device = BTAdapter.getRemoteDevice(address);


            try {
                BTSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Socket creation failed.", Toast.LENGTH_LONG).show();
            }
            try {
                BTSocket.connect();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Connection could not be made.", Toast.LENGTH_LONG).show();
            }

            cThread = new ConnectedThread(BTSocket);
            cThread.start();

            cThread.write("1"); //check if device is connected
        }
    }

    public void onPause(){
        super.onPause();
        if(address !=  null) {
            try {
                BTSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Connection could not close.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkBTState(){
        if(BTAdapter==null){
            Toast.makeText(getBaseContext(), "Device not supported.", Toast.LENGTH_LONG).show();
        }else{
            if(BTAdapter.isEnabled()){

            }else{
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, 1);
            }
        }
    }

    private void addListenerToRadios(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (enabled.isChecked()) {
                    radioState = 1;
                    cThread.write("e1");
                } else if (disabled.isChecked()) {
                    radioState = -1;
                    cThread.write("e0");
                }
            }
        });
    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectedThread extends Thread{
        private final OutputStream streamOut;

        public ConnectedThread(BluetoothSocket socket){
            OutputStream out = null;

            try{
                out = socket.getOutputStream();
            }catch(IOException e){
            }
            streamOut = out;
        }
        //public void run(){}

        public void write(String output){
            byte[] sendBuffer = output.getBytes();
            try{
                streamOut.write(sendBuffer);
            }catch(IOException e){
                Toast.makeText(getBaseContext(), "Cannot send data.", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
}
