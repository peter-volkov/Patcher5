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
import java.util.List;


public class UninstallOriginalApkActivity extends ActionBarActivity {
    String originalApkFilePath;
    String packageName;
    String appName;
    String newApkFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstall_orignal_apk);

        Intent intent = getIntent();
        this.appName = intent.getStringExtra("appName");
        this.packageName =  intent.getStringExtra("packageName");
        this.originalApkFilePath =  intent.getStringExtra("originalApkFilePath");
        this.newApkFilePath =  intent.getStringExtra("newApkFilePath");

        Button buttonUninstall = (Button) findViewById(R.id.buttonUninstall);
        buttonUninstall.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPackageInstalled(appName)) {
                    uninstallPackage(packageName);
                } else {
                    Intent navigationIntent = new Intent(UninstallOriginalApkActivity.this, InstallModifiedApkActivity.class);
                    navigationIntent.putExtra("appName", appName);
                    navigationIntent.putExtra("packageName", packageName);
                    navigationIntent.putExtra("originalApkFilePath", originalApkFilePath);
                    navigationIntent.putExtra("newApkFilePath", newApkFilePath);
                    navigationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(navigationIntent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uninstall_orignal_apk, menu);
        return true;
    }

    private void uninstallPackage(String packageName) {
        Uri localUri = Uri.fromParts("package", packageName, null);
        Intent localIntent = new Intent("android.intent.action.DELETE");
        localIntent.setData(localUri);
        startActivity(localIntent);
    }

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
