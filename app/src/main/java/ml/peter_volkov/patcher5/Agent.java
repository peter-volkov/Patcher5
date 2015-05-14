package ml.peter_volkov.patcher5;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import android.os.Handler;

public class Agent {
    Context context;
    public Agent(Context context) {
        this.context = context;
    }

    public void showToast(String text) {
        Toast.makeText(this.context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();

        Intent serviceIntent;
        serviceIntent = new Intent("ml.peter_volkov.testaskservice.MessengerService.BIND");
        serviceIntent.setPackage("ml.peter_volkov.testaskservice");

        this.context.startService(serviceIntent);
        this.context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private static final String TAG = "testAskService";

    Messenger serviceMessenger;
    boolean isBound = false;

    static final int MSG_REQUEST_TYPE = 31337;
    static final int MSG_RESPONSE_TYPE = 31338;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
            isBound = false;
        }
    };

    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Activity handleMessage");
            switch (msg.what) {
                case MSG_RESPONSE_TYPE:
                    String response =  msg.getData().get("response").toString();
                    showToast("Received answer from service: " + response);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void sendRequest(String requestKey, String requestValue) {
        if (!isBound) return;

        Bundle messageData = new Bundle();
        messageData.putString(requestKey, requestValue);
        Message requestMessage = Message.obtain(null, MSG_REQUEST_TYPE, 0, 0);
        requestMessage.replyTo = new Messenger(new ResponseHandler());
        requestMessage.setData(messageData);

        try {
            serviceMessenger.send(requestMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        } else if (object.getClass().isArray()) {
            StringBuilder stringBuilder = new StringBuilder("{");
            int arrayLength = Array.getLength(object);
            for (int index = 0; index < arrayLength; index++) {
                stringBuilder.append(toString(Array.get(object, index))).append(", ");
            }
            return stringBuilder.append("}").toString();
        } else {
            return object.getClass().toString();
        }
    }


    public  void logToService(String message) {
        String className = "ml.peter_volkov.serviceclienttest.MainActivity$2.onClick";
        this.sendRequest("class", message);
        this.showToast("asking about " + message);
    }

    public static void log(String paramString) {
        Log.v(TAG, paramString.replaceAll("\\r?\\n", "\\\\n"));
    }
}
