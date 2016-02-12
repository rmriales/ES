package com.example.ryan.lighttvbt;


import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String timeoutState = "timeoutState";

    private int time;
    private int radioState;

    private BluetoothAdapter BTAdapter = null;
    private BluetoothSocket BTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThread cThread;
    private static String address;

    Button updateButton;
    RadioButton enabled;
    RadioButton disabled;
    EditText countdown;
    ProgressBar cdprogress;
    RadioGroup radioGroup;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            time = 5;
        } else {
            time = savedInstanceState.getInt(timeoutState);
        }

        updateButton = (Button) findViewById(R.id.button);
        enabled = (RadioButton) findViewById(R.id.enableRadioButton);
        disabled = (RadioButton) findViewById(R.id.disableRadioButton);
        countdown = (EditText) findViewById(R.id.timeOutEditText);
        cdprogress = (ProgressBar) findViewById(R.id.countdownProgressBar);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        addListenerToRadios();
        addOnClickListener();

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        if(address == null) {
            final LayoutInflater factory = getLayoutInflater();

            final View sdlView = factory.inflate(R.layout.select_device, null);

            listView = (ListView) sdlView.findViewById(R.id.listView);
            address = getDeviceToUse(listView);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private String getDeviceToUse(View view) {
        String deviceReturn;
        final LayoutInflater factory = getLayoutInflater();


        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        List<String> devices = new ArrayList<String>();
        for (BluetoothDevice BT : pairedDevices) {
            devices.add(BT.getName());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,com.example.ryan.lighttvbt.R.layout.select_device, devices);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceReturn = devices.getPosition();
                }
            });
        return deviceReturn;
        }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void addOnClickListener() {
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cThread.write(time);
            }
        });
    }

    public void onResume(){
        super.onResume();

        Intent intent = getIntent();

        //address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);




        BluetoothDevice device = BTAdapter.getRemoteDevice(address);

        try{
            BTSocket = createBluetoothSocket(device);
        }catch(IOException e){
            Toast.makeText(getBaseContext(), "Socket creation failed.", Toast.LENGTH_LONG).show();
        }
        try{
            BTSocket.connect();
        }catch(IOException e2){
            Toast.makeText(getBaseContext(), "Connection could not be made.", Toast.LENGTH_LONG).show();
        }

        cThread = new ConnectedThread(BTSocket);
        cThread.start();

        cThread.write("1"); //check if device is connected
    }

    public void onPause(){
        super.onPause();
        try{
            BTSocket.close();
        }catch(IOException e2){
            Toast.makeText(getBaseContext(), "Connection could not close.", Toast.LENGTH_LONG).show();
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
                } else if (disabled.isChecked()) {
                    radioState = -1;
                }
            }
        });
    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
