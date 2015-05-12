package ml.peter_volkov.patcher5;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.io.FileUtils;

import kellinwood.security.zipsigner.ProgressListener;
import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.zipio.ZioEntry;
import kellinwood.zipio.ZioEntryInputStream;
import kellinwood.zipio.ZioEntryOutputStream;
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
import java.util.Iterator;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

public class MainActivity extends ActionBarActivity {

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
        String baksmaliPath = "/sdcard/smali_games/baksmali.dex.zip";
        String command = "dalvikvm -classpath " + baksmaliPath + " org.jf.baksmali.main";
        command += " -o " + targetDirPath;
        command += " " + sourceDexFilePath;
        return runShellCommand(command);
    }

    private String assembleDexFile(String sourceDirPath, String targetDexFilePath) {
        String smaliPath = "/sdcard/smali_games/smali.dex.zip";
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

    private void doStuff() {
        File sdDir = Environment.getExternalStorageDirectory();

        //String workingDir = sdDir + "/smali_games/";
        //String dexFilePath = workingDir + "classes.dex";

        String originalApkFilePath = "/sdcard/smali_games/signing/original.apk";

        ZipOutput zipOutput;

        String workingDir = "/sdcard/smali_games/signing/";

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //doStuff();
        dropSmali();
    }

    private void dropSmali() {
        try {
            InputStream ins = this.getClass().getClassLoader().getResourceAsStream("assets/bin/baksmali.dex.zip");
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            //FileOutputStream fos = this.openFileOutput(FILENAME, this.MODE_PRIVATE);
            FileOutputStream fos = new FileOutputStream("/sdcard/smali_games/test_smali_drop/baksmali.dex.zip");
            fos.write(buffer);
            fos.close();

            //File file = getFileStreamPath(FILENAME);
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
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
}
