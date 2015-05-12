package ml.peter_volkov.patcher5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class SmaliTree {

    String dirPath;
    public List<File> smaliFiles = new ArrayList<File>();
    public List<ClassNode> classes = new ArrayList<>();
    public SmaliTree(String disassembledBytecodeDirPath) {
        this.dirPath = disassembledBytecodeDirPath;
        this.traverseDir(new File(this.dirPath));
        for (File smaliFile: this.smaliFiles) {
            this.parseClass(smaliFile);
        }
    }

    public void copy(File source, File target) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void traverseDir(File node) {
        if (node.isFile()
            && node.getAbsolutePath().endsWith(".smali")
            && !node.getAbsolutePath().contains("annotation")
            && !node.getAbsolutePath().contains("/android/support/")
            ) {
            this.smaliFiles.add(node.getAbsoluteFile());
        } else if (node.isFile() && node.getAbsolutePath().endsWith(".smali")) {
            //copy to result dir without any changes
            try {
                File newFile = new File(node.getAbsolutePath().replace("original_smali", "modified_smali"));
                boolean mkdirsResult = newFile.getParentFile().mkdirs();
                copy(node, newFile);
            } catch (IOException e) {
                Log.e(e.toString());
            }
        }
        if(node.isDirectory()){
            String[] childNodes = node.list();
            for(String filename : childNodes){
                traverseDir(new File(node, filename));
            }
        }
    }

    public void saveTree(String outputDir) {
        for (ClassNode classNode : this.classes) {
            Log.d("SmaliTree", "Saving " + classNode.name + "to " + outputDir);
            classNode.saveToFile(outputDir);
        }
    }

    private void parseClass(File file) {
        Log.d("SmaliTree", "Parsing " + file.getAbsolutePath());
        ClassNode classNode = new ClassNode(file.getAbsolutePath());
        this.classes.add(classNode);
    }
}
