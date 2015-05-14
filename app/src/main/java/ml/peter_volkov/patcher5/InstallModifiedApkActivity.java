package ml.peter_volkov.patcher5;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class InstallModifiedApkActivity extends ActionBarActivity {

    String originalApkFilePath;
    String packageName;
    String appName;
    String newApkFilePath;

    private boolean isPackageInstalled(String appName) {

        final PackageManager packageManager = getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        String applicationName;
        ApplicationInfo appInfo;

        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");

        for (PackageInfo packageInfo : packages) {
            try {
                appInfo = packageManager.getApplicationInfo(packageInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException ex) {
                appInfo = null;
            }
            applicationName = (String) (appInfo != null ? packageManager.getApplicationLabel(appInfo) : "(unknown)");
            if (applicationName.equals(appName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_modified_apk);

        Intent intent = getIntent();
        this.appName = intent.getStringExtra("appName");
        this.packageName =  intent.getStringExtra("packageName");
        this.originalApkFilePath =  intent.getStringExtra("originalApkFilePath");
        this.newApkFilePath =  intent.getStringExtra("newApkFilePath");

        Button buttonInstall = (Button) findViewById(R.id.buttonInstall);
        buttonInstall.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isPackageInstalled(appName)) {
                    installApk(newApkFilePath);
                } else {
                    Intent navigationIntent = new Intent(InstallModifiedApkActivity.this, MainActivity.class);
                    navigationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(navigationIntent);
                }
            }
        });
    }


    private void installApk(String apkFilePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkFilePath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_install_modified_apk, menu);
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
}
