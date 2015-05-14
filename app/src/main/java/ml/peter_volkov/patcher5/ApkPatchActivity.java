package ml.peter_volkov.patcher5;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.zipio.ZioEntry;
import kellinwood.zipio.ZipInput;
import kellinwood.zipio.ZipOutput;


public class ApkPatchActivity extends ActionBarActivity {

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        String modifiedApkFilePath;

        public BackgroundTask(ApkPatchActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Repacking APK, please wait.");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Intent navigationIntent = new Intent(ApkPatchActivity.this, UninstallOriginalApkActivity.class);
            navigationIntent.putExtra("newApkFilePath", this.modifiedApkFilePath);
            navigationIntent.putExtra("originalApkFilePath", originalApkFilePath);
            navigationIntent.putExtra("packageName", packageName);
            navigationIntent.putExtra("appName", appName);
            navigationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(navigationIntent);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                extractSmaliDexFiles();
                this.modifiedApkFilePath = patchApk(originalApkFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private String getAppDataDir() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("Error Package name not found " + e.getMessage());
            return null;
        }
    }

    private void signZip(String inputFile, String outputFile) {
        try {
            ZipSigner zipSigner = new ZipSigner();
            zipSigner.setKeymode("auto-testkey");
            zipSigner.signZip(inputFile, outputFile);
        } catch (IllegalAccessException e) {
            Log.e(e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(e.getMessage());
        } catch (InstantiationException e) {
            Log.e(e.getMessage());
        } catch (IOException e) {
            Log.e(e.getMessage());
        } catch (GeneralSecurityException e) {
            Log.e(e.getMessage());
        }
    }

    private String runShellCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;
    }

    private String disassembleDexFile(String sourceDexFilePath, String targetDirPath) {
        String baksmaliPath = getAppDataDir() + "/files/baksmali.dex.zip";
        String command = "dalvikvm -classpath " + baksmaliPath + " org.jf.baksmali.main";
        command += " -o " + targetDirPath;
        command += " " + sourceDexFilePath;
        return runShellCommand(command);
    }

    private String assembleDexFile(String sourceDirPath, String targetDexFilePath) {
        String smaliPath = getAppDataDir() + "/files/smali.dex.zip";
        String command = "dalvikvm -classpath " + smaliPath + " org.jf.smali.main";
        command += " -o " + targetDexFilePath;
        command += " " + sourceDirPath;
        return runShellCommand(command);
    }

    private byte[] getBytes(String filepath) {
        File file = new File(filepath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(e.getMessage());
        }
        return bytes;
    }

    private void writeLocalFileFromInputStream(InputStream inputStream, String filepath) {
        try {
            FileOutputStream fileOutputStream = this.openFileOutput(filepath, Context.MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) >= 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
    }

    private void writeFileFromInputStream(InputStream inputStream, String filepath) {
        try {
            FileOutputStream out = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
    }

    private void writeFileToZip(String filepath, String archiveFilepath) {
        ZioEntry entry = new ZioEntry("classes.dex");
        OutputStream entryOut = entry.getOutputStream();
        try {
            ZipOutput zipOutput = new ZipOutput(archiveFilepath);
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    private String patchApk(String originalApkFilePath) {
        ZipOutput zipOutput;

        String appDataDir = getAppDataDir();
        new File(appDataDir).setReadable(true, false);
        new File(appDataDir + "/files/").setReadable(true, false);

        String workingDir = "/sdcard/smali_games/signing/";
        //String workingDir = appDataDir + "/files/workingDir/";
        new File(workingDir).mkdirs();
        new File(workingDir).setReadable(true, false);

        String originalDexFilePath = workingDir + "classes.dex.original";
        String modifiedDexFilePath = workingDir + "classes.dex.modified";
        String modifiedApkFilePath = workingDir + "out.apk";
        String signedModifiedApkFilePath = workingDir + "out.signed.apk";
        String originalSmaliSourcesDir = workingDir + "original_smali/";
        String modifiedSmaliSourcesDir = workingDir + "modified_smali/";

        try {
            //ZioEntry entry;
            ZipInput zipInput = ZipInput.read(originalApkFilePath);
            File outputFile = new File(modifiedApkFilePath);

            zipOutput = new ZipOutput(outputFile);

            Iterator<ZioEntry> zipFiles = zipInput.getEntries().values().iterator();
            while (zipFiles.hasNext()) {
                ZioEntry zippedFile = zipFiles.next();

                if (zippedFile.getName().equals("classes.dex")) {
                    writeFileFromInputStream(zippedFile.getInputStream(), originalDexFilePath);
                } else if (!zippedFile.getName().startsWith("META-INF/")) {
                    //http://zip-signer.googlecode.com/svn/zipio-lib/tags/zipio-lib-1.2/src/test/java/kellinwood/zipio/CreateZipFileTest.java
                    zipOutput.write(zippedFile);
                }
            }

            disassembleDexFile(originalDexFilePath, originalSmaliSourcesDir);
            SmaliTree smaliTree = new SmaliTree(originalSmaliSourcesDir);

            SmaliTreeModifier smaliTreeModifier = new SmaliTreeModifier(smaliTree);
            String smaliModifierConfigFilePath = "assets/config/method_hooks.json";
            smaliTreeModifier.modify(smaliModifierConfigFilePath);

            smaliTree.saveTree(modifiedSmaliSourcesDir);
            assembleDexFile(modifiedSmaliSourcesDir, modifiedDexFilePath);

            //putting modified Dalvik bytecode into new APK
            ZioEntry entry = new ZioEntry("classes.dex");
            OutputStream entryOut = entry.getOutputStream();
            entryOut.write(getBytes(modifiedDexFilePath));
            zipOutput.write(entry);

            zipOutput.close();

        } catch (IOException e) {
            Log.e(e.getMessage());
        }

        //signing new APK
        signZip(modifiedApkFilePath, signedModifiedApkFilePath);

        //cleaning temp files
        try {
            //FileUtils.deleteDirectory(new File(originalSmaliSourcesDir));
            //FileUtils.deleteDirectory(new File(modifiedSmaliSourcesDir));
            new File(originalDexFilePath).delete();
            new File(modifiedDexFilePath).delete();
            new File(modifiedApkFilePath).delete();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
        return signedModifiedApkFilePath;
    }


    private void extractSmaliDexFiles() {
        String appDataDir = getAppDataDir();
        try {
            if (!new File(appDataDir, "/files/baksmali.dex.zip").exists()) {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/bin/baksmali.dex.zip");
                this.writeLocalFileFromInputStream(inputStream, "baksmali.dex.zip");
                if (!new File(appDataDir + "/files/baksmali.dex.zip").setReadable(true, false)) {
                    Log.e("Cannot set " + appDataDir + "/files/baksmali.dex.zip readable");
                }
            }

            if (!new File(appDataDir, "/files/smali.dex.zip").exists()) {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/bin/smali.dex.zip");
                this.writeLocalFileFromInputStream(inputStream, "smali.dex.zip");
                if (!new File(appDataDir + "/files/smali.dex.zip").setReadable(true, false)) {
                    Log.e("Cannot set " + appDataDir + "/files/smali.dex.zip readable");
                }
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }



    String originalApkFilePath;
    String packageName;
    String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_patch);

        Intent intent = getIntent();
        this.appName = intent.getStringExtra("appName");
        this.packageName =  intent.getStringExtra("packageName");
        this.originalApkFilePath =  intent.getStringExtra("originalApkFilePath");

        BackgroundTask task = new BackgroundTask(ApkPatchActivity.this);
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_apk_patch, menu);
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
