package ml.peter_volkov.patcher5;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClassNodeTest  extends TestCase {

    List<List<String>> testCases = new ArrayList<>();
    private void loadTestCases() {

        List<String> testCaseFiles = new ArrayList<>();
        testCaseFiles.add("assets/testCases/classNode/R.smali");

        for (String testCaseFilePath : testCaseFiles) {
            try {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream(testCaseFilePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                ArrayList<String> lines = new ArrayList<String>();
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        lines.add(line);
                    }
                }
                testCases.add(lines);
            } catch (Exception e) {
                Log.e(testCaseFilePath + " not found");
            }
        }
    }

    public void setUp() {
        this.loadTestCases();
    }

    public void testClassRebuild() {
        for (List<String> testCaseLines : this.testCases) {
            ClassNode classNode = new ClassNode(testCaseLines);
            List<String> actualResult = classNode.getSmaliText();
            assertEquals(testCaseLines, actualResult);
        }
    }

}
