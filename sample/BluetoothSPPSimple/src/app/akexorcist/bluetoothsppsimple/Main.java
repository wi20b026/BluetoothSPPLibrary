/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.akexorcist.bluetoothsppsimple;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import app.akexorcist.bluetoothspp.BluetoothSPP;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;

public class Main extends Activity {
	BluetoothSPP bt;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bt = new BluetoothSPP(this);

		if(!bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext()
					, "Bluetooth is not available"
					, Toast.LENGTH_SHORT).show();
            finish();
		}
		
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				Toast.makeText(Main.this, message, Toast.LENGTH_SHORT).show();
			}
		});
		
		bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			public void onDeviceConnected(String name, String address) {
				Toast.makeText(getApplicationContext()
						, "Connected to " + name + "\n" + address
						, Toast.LENGTH_SHORT).show();
			}

			public void onDeviceDisconnected() {
				Toast.makeText(getApplicationContext()
						, "Connection lost", Toast.LENGTH_SHORT).show();
			}

			public void onDeviceConnectionFailed() {
				Toast.makeText(getApplicationContext()
						, "Unable to connect", Toast.LENGTH_SHORT).show();
			}
		});
		
		Button btnConnect = (Button)findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
        			bt.disconnect();
        		} else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE); 
        		}
        	}
        }); 
	}
	
	public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }
	
	public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
        	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) { 
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }
	
	public void setup() {
		Button btnSend = (Button)findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		bt.send("Text", true);
        	}
        });
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
		} else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                		, "Bluetooth was not enabled."
                		, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
