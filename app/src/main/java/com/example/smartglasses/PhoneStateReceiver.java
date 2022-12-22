package com.example.smartglasses;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by theappguruz on 07/05/16.
 */
public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            System.out.println("Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//            String incomingName = ;//intent.getStringExtra(ContactsContract.Contacts.DISPLAY_NAME);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                Toast.makeText(context,"Incoming Call State",Toast.LENGTH_SHORT).show();
                Toast.makeText(context,"Ringing - " + incomingNumber,Toast.LENGTH_LONG).show();

//                MainActivity.getInstance().btdata = "!" + incomingNumber + "!";
//                MainActivity.getInstance().sendBTdata(context.getApplicationContext());
//                sendBTdata("!" + incomingNumber + "!");
            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
//                Toast.makeText(context,"Call Received State",Toast.LENGTH_SHORT).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
//                Toast.makeText(context,"Call Idle State",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void sendBTdata(String d)
    {
        try {
            if(MainActivity.isBtConnected) {
                //msg(d);
                MainActivity.btSocket.getOutputStream().write(d.getBytes());
            }
        } catch (Exception e) {
            //msg(d + "\nBluetooth Not Connected");
        }
    }

}


