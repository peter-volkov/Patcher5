package ml.peter_volkov.patcher5;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceClient {
    private static final String TAG = "testAskService";
    Messenger serviceMessenger;
    boolean isBound = false;

    static final int MSG_REQUEST_TYPE = 31337;
    static final int MSG_RESPONSE_TYPE = 31338;

    final Object token = new Object();
    volatile String permissionResult;

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

    public void showToast(String text) {
        Toast.makeText(this.context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }


    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "Activity handleMessage");
            switch (message.what) {
                case MSG_RESPONSE_TYPE:
                    String response =  message.getData().get("response").toString();
                    showToast("Received answer from service: " + response);
                    permissionResult = response;
                    Log.d("setting: " + permissionResult);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    public void sendRequest(String requestKey, String requestValue) {
        if (!isBound) return;
        Log.d("2" + permissionResult);


        Bundle messageData = new Bundle();
        messageData.putString(requestKey, requestValue);
        Message requestMessage = Message.obtain(null, MSG_REQUEST_TYPE, 0, 0);
        try {
            //Handler handler = new ResponseHandler();
            Log.d("3" + permissionResult);
            requestMessage.replyTo = new Messenger(new ResponseHandler());
            Log.d("4" + permissionResult);
            requestMessage.setData(messageData);
            serviceMessenger.send(requestMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    Context context;
    public ServiceClient(Context context) {
        this.context = context;

        Intent serviceIntent;
        serviceIntent = new Intent("ml.peter_volkov.testaskservice.MessengerService.BIND");
        serviceIntent.setPackage("ml.peter_volkov.testaskservice");

        this.context.startService(serviceIntent);
        this.context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    String message;
    public void log(String message) {
        this.message = message;
        this.sendRequest("class", message);
        this.showToast("asking about " + message);
    }

    public class MyThread implements Runnable {

        String message;
        public MyThread(String message) {
            this.message = message;
        }

        public void run() {
            Looper.prepare();

            sendRequest("class", "asdsd");
            Looper.loop();

        }
    }

    public boolean checkPermission(String message) {
        //permissionResult = null;
        //log(message);

//        lock.lock();
        try {
            Runnable logThread = new MyThread(message);
            new Thread(logThread).start();

//            Log.d("after wait");            Log.d("before wait");
            //synchronized (logThread) {
                logThread.wait();
            //}
//            lock.lock();

//            permissionResult = null;
//            Runnable logThread = new MyThread(message);
//            new Thread(logThread).start();


//            while (permissionResult == null) {
//                permissionResultArray.get(0);
//                Log.d("waiting");
//                Thread.sleep(1000);
//            }
//            lock.unlock();

        } catch (Exception e) {
            Log.e(e.getMessage());
        }
//
//        try {
//            while (permissionResult == null) {
//                Thread.sleep(100);
//            }
//            //lock.tryLock(100L, TimeUnit.MILLISECONDS);
//
//        } catch (Exception e) {
//            Log.e(e.getMessage());
//        }

        //log(message);
//        try {
//
//            synchronized (permissionResult) {
//                handler.wait(1000L);
//            }
//    } catch (Exception e) {
//            Log.e(e.getMessage());
//        }

        int a = 12;
        Log.d("returning: " + permissionResult);
        if (permissionResult != null && permissionResult.toLowerCase().equals("pass")) {
            return true;
        } else {
            return false;
        }
    }
}
