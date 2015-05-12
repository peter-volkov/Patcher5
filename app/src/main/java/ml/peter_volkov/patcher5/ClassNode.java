package ml.peter_volkov.patcher5;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ClassNode {
    String filePath = "";
    byte[] buf;

    String name;
    String superName;
    String source;
    List<String> implementsList = new ArrayList<String>();
    List<String> interfaces = new ArrayList<String>();
    List<MethodNode> methods = new ArrayList<MethodNode>();
    List<String> access = new ArrayList<String>();
    List<FieldNode> fields = new ArrayList<FieldNode>();
    List<String> annotations = new ArrayList<String>();
    List<String> everythingElse = new ArrayList<String>();

    public ClassNode() {
    }

    public ClassNode(String filePath) {
        this.filePath = filePath;

        List<String> lines = new ArrayList<>();
        File file = new File(this.filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                lines.add(line);
            }
        } catch (Exception e) {
            Log.e(e.toString());
        }

        this.parse(lines);
    }

    public ClassNode(List<String> lines) {
        this.parse(lines);
    }

    private void parse(List<String> lines) {
        ListIterator<String> lineIterator = lines.listIterator();
        String line;
        String[] segments;
        String type;
        while (lineIterator.hasNext()) {
            line = lineIterator.next();
            segments = line.split("\\s+");
            type = segments[0];
            if (type.equals(".source")) {
                this.source = segments[1];
            } else if (type.equals(".class")) {
                this.name = segments[segments.length - 1];
                this.access = Arrays.asList(Arrays.copyOfRange(segments, 1, segments.length - 1));
            } else if (type.equals(".super")) {
                this.superName = segments[1];
            } else if (type.equals(".interface")) {
                Log.e(this.getClass().getName(), "can't parse .interface");
            } else if (type.equals(".field")) {
                this.fields.add(new FieldNode(line));
            } else if (type.equals(".method")) {
                List<String> methodLines = new ArrayList<>();
                do {
                    methodLines.add(line);
                    line = lineIterator.next();
                }
                while (!line.startsWith(".end method"));
                //appending ".end method"
                methodLines.add(line);
                this.methods.add(new MethodNode(methodLines));
            } else if (type.equals(".annotation")) {
                do {
                    this.annotations.add(line);
                    line = lineIterator.next();
                }
                while (!line.startsWith(".end annotation"));
                //appending ".end annotation"
                this.annotations.add(line);
            } else {
                this.everythingElse.add(line);
            }
        }
        Log.d("", this.name + "was parsed");
    }

    public List<String> getPrettySmaliText(List<String> smaliText) {
        //just formatting for manual debugging purposes
        String indent = "    ";
        List<String> prettySmaliText = new ArrayList<>();
        boolean insertIndent = false;
        for (String line : smaliText) {
            if (line.startsWith(".annotation") || line.startsWith(".method")) {
                line = "\n" + line;
                insertIndent = true;
            } else if (line.startsWith(".end annotation") || line.startsWith(".end method")) {
                insertIndent = false;
            }
            prettySmaliText.add(insertIndent ? indent + line : line);
        }
        return prettySmaliText;
    }

    public List<String> getSmaliText() {
        List<String> smaliText = new ArrayList<String>();
        smaliText.add(String.format(".class %s %s", TextUtils.join(" ", this.access), this.name));
        smaliText.add(String.format(".super %s", this.superName));

        if (this.source != null) {
            smaliText.add(String.format(".source %s", this.source));
        }

        if (this.implementsList.size() != 0) {
            for(String implementation: this.implementsList) {
                smaliText.add(String.format(".implements %s", implementation));
            }
        }

        for(String line: this.annotations) {
            smaliText.add(line);
        }

        for(FieldNode field: this.fields) {
            smaliText.add(field.getSmaliText());
        }
        try {
            for (MethodNode method : this.methods) {
                for (String line : method.getSmaliText()) {
                    smaliText.add(line);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e(this.name + " method dumping error " + e.toString() + " " + sw.toString());
        }

        for(String line: this.everythingElse) {
            smaliText.add(line);
        }

        return smaliText;
    }

    public void saveToFile(String dirPath) {
        //'Landroid/support/annotation/AnimatorRes;'
        String classPath = this.name.substring(1, this.name.length() - 1);
//        int classNameDelimiterIndex = classPath.lastIndexOf('/');
//        String classDir = classPath.substring(0, classNameDelimiterIndex);
//        String className = classPath.substring(classNameDelimiterIndex + 1);
        File smaliTextFile = new File(dirPath, classPath + ".smali");
        boolean mkdirsResult = smaliTextFile.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(smaliTextFile);
            for (String line : this.getPrettySmaliText(this.getSmaliText())) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (Exception e) {
            Log.e(e.toString());
        }
    }

}
