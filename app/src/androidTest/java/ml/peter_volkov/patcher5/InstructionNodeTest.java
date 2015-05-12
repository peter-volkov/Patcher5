package ml.peter_volkov.patcher5;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peevo on 04.05.2015.
 */
public class InstructionNodeTest extends TestCase {

    List<List<String>> testCases = new ArrayList<>();
    private void loadTestCases() {

        List<String> testCaseFiles = new ArrayList<>();
        testCaseFiles.add("assets/testCases/instructionNode/dalvik_invoke_instructions.txt");

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

    public void testRegexpParts() {
        Pattern opcodeNameRe = Pattern.compile(InstructionNode.opcodeNameReTemplate);
        Pattern registerListRe = Pattern.compile(InstructionNode.registerListReTemplate);
        Pattern registerRangeRe = Pattern.compile(InstructionNode.registerRangeReTemplate);
        Pattern javaTypeRe = Pattern.compile(InstructionNode.javaTypeReTemplate);
        Pattern methodNameRe = Pattern.compile(InstructionNode.methodNameReTemplate);

        Matcher match = opcodeNameRe.matcher("invoke-static");
        assertTrue(match.matches());

        match = registerListRe.matcher("v0");
        assertTrue(match.matches());

        match = registerRangeRe.matcher("v0 .. v0");
        assertTrue(match.matches());

        match = javaTypeRe.matcher("Ljava/lang/Boolean;");
        assertTrue(match.matches());

        match = methodNameRe.matcher("parseBoolean");
        assertTrue(match.matches());
    }

    public void testParseInvokeInstruction() {
        //this.loadTestCases();
        assertTrue(this.testCases != null);
        for (List<String> testCaseLines : this.testCases) {
            for (String testCaseLine : testCaseLines){
                //String testCase = "invoke-static {v0}, Ljava/lang/Boolean;->parseBoolean(Ljava/lang/String;)Z";
                InstructionNode instructionNode = new InstructionNode(testCaseLine);
                String actualResult = instructionNode.getSmaliText();
                assertEquals(testCaseLine, actualResult);
            }
        }
    }
}
