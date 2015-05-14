package ml.peter_volkov.patcher5;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.zipio.ZioEntry;
import kellinwood.zipio.ZipInput;
import kellinwood.zipio.ZipOutput;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity {


    public class MyThread implements Runnable {

        public MyThread(String message) {
            serviceClient.checkPermission(message);
        }

        public void run() {
        }
    }

    public String getIMEI() {

        String className = "ml.peter_volkov.serviceclienttest.MainActivity$2.onClick";
//        Runnable logThread = new MyThread(className);
//        new Thread(logThread).start();
//        try {
//            synchronized (logThread) {
//                logThread.wait();
//            }
//        } catch (Exception e) {
//            Log.e("sadasd");
//        }


        if (serviceClient.checkPermission(className)) {
            return "realIMEI";
        } else {
            return "fakeIMEI";
        }
    }

    ServiceClient serviceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.listView);
        CreateListView();

        String originalApkFilePath = "/sdcard/smali_games/signing/original.apk";
        //this.patchApk(originalApkFilePath);
        //serviceClient = new ServiceClient(this);
        //agent.askService("someCoolClass");

        Button buttonAskService = (Button) findViewById(R.id.buttonAskService);
        buttonAskService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showToast("IMEI = " + getIMEI());
            }
        });
    }

    public void showToast(String text) {
        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

    private String getApkFilePath(ResolveInfo resolveinfo) {
        return resolveinfo.activityInfo.applicationInfo.sourceDir;
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        //FileOutputStream fos = this.openFileOutput(dst.getPath(), Context.MODE_PRIVATE);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    ListView list;
    List<String> fileList;

    private void CreateListView() {
        fileList = new ArrayList<>();

        final PackageManager packageManager = getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        String applicationName;
        String apkPath;

        final Map<String, String> appNamePathMap = new HashMap<>();

        SortedMap<Long, String> mySortedMap = new TreeMap<>();
        ApplicationInfo appInfo;

        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        final List pmList = getPackageManager().queryIntentActivities(intent, 0);


        for (PackageInfo packageInfo : packages) {
            try {
                appInfo = packageManager.getApplicationInfo(packageInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException ex) {
                appInfo = null;
            }
            applicationName = (String) (appInfo != null ? packageManager.getApplicationLabel(appInfo) : "(unknown)");
            apkPath = (String) (appInfo != null ? appInfo.sourceDir : "(unknown)");
            Log.i(applicationName + " " + packageInfo.packageName);
            appNamePathMap.put(applicationName, apkPath);
            mySortedMap.put(packageInfo.firstInstallTime, applicationName);
        }

        for (Map.Entry<Long, String> entry : mySortedMap.entrySet()) {
            Long key = entry.getKey();
            String value = entry.getValue();
            fileList.add(value);
            Log.i(key.toString() + " " + value);
        }
        Collections.reverse(fileList);

        //Create an adapter for the listView and add the ArrayList to the adapter.
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                //view.setBackgroundColor(Color.parseColor("#58FAAC"));
                String applicationName = fileList.get(position);
                String apkPath = appNamePathMap.get(applicationName);
                Long fileSize = new File(apkPath).length();
                for (Object app : pmList) {
                    ResolveInfo resInfo = (ResolveInfo) app;
                    //ResolveInfo localResolveInfo = new ResolveInfo();
                    String path = resInfo.activityInfo.applicationInfo.sourceDir;
                    String packageName = resInfo.activityInfo.applicationInfo.packageName;
                    if (packageManager.getApplicationLabel(resInfo.activityInfo.applicationInfo).equals(applicationName)) {

                        Intent navigationIntent = new Intent(MainActivity.this, ApkPatchActivity.class);
                        navigationIntent.putExtra("appName", applicationName);
                        navigationIntent.putExtra("packageName", packageName);
                        navigationIntent.putExtra("originalApkFilePath", getApkFilePath(resInfo));
                        navigationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(navigationIntent);

//                        String originalApkFilePath = getApkFilePath(resInfo);
//                        String modifiedApkFilePath = patchApk(originalApkFilePath);
//                        uninstallPackage(packageName);
//                        try {
//                            Thread.sleep(2000);
//                        } catch (Exception e) {
//
//                        }
//                        installApk(modifiedApkFilePath);
                    }
                }
                Log.i(fileList.get(position) + " " + apkPath + " " + fileSize.toString());
            }
        });
    }
}
