package ml.peter_volkov.patcher5;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeNodeTest extends TestCase {

    private static boolean compareLists(List<?> list1, List<?> list2) {
        ArrayList<?> listCopy = new ArrayList<>(list1);
        for (Object object : list2) {
            if (!listCopy.remove(object)) {
                return false;
            }
        }
        return listCopy.isEmpty();
    }

    public void testUtilMethods() {
        assertEquals("int", TypeNode.getHumanReadableClassName("I"));
        assertEquals("I", TypeNode.getBytecodeNotationClassName("int"));
        assertEquals("java.util.Random", TypeNode.getHumanReadableClassName("Ljava/util/Random;"));
        assertEquals("Ljava/util/Random;", TypeNode.getBytecodeNotationClassName("java.util.Random"));
    }

    public void testGetArgumentString() {
        List<TypeNode> testCase = new ArrayList<>();
        testCase.add(new TypeNode("Ljava/lang/String;"));
        testCase.add(new TypeNode("Ljava/lang/Boolean;"));
        testCase.add(new TypeNode("I"));
        assertEquals("Ljava/lang/String;Ljava/lang/Boolean;I", TypeNode.getArgumentString(testCase));
    }

    public void testArgumentListParser() {
        HashMap<String, List<TypeNode>> testCases = new HashMap<>();

        //testing showAlert(Ljava/lang/String;Ljava/lang/String;)V
        List<TypeNode> correct = new ArrayList<>();
        correct.add(new TypeNode("Ljava/lang/String;"));
        correct.add(new TypeNode("Ljava/lang/String;"));
        testCases.put("Ljava/lang/String;Ljava/lang/String;", correct);

        correct = new ArrayList<>();
        correct.add(new TypeNode("Ljava/lang/String;"));
        testCases.put("Ljava/lang/String;", correct);

        correct = new ArrayList<>();
        correct.add(new TypeNode("Landroid/app/Activity;"));
        correct.add(new TypeNode("[Landroid/view/View;"));
        correct.add(new TypeNode("[Ljava/lang/String;"));
        testCases.put("Landroid/app/Activity;[Landroid/view/View;[Ljava/lang/String;", correct);

        correct = new ArrayList<>();
        correct.add(new TypeNode("Landroid/telephony/SmsManager;"));
        correct.add(new TypeNode("Ljava/lang/String;"));
        correct.add(new TypeNode("Ljava/lang/String;"));
        correct.add(new TypeNode("Ljava/lang/String;"));
        correct.add(new TypeNode("Landroid/app/PendingIntent;"));
        correct.add(new TypeNode("Landroid/app/PendingIntent;"));
        testCases.put("Landroid/telephony/SmsManager;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;", correct);

        for (Map.Entry<String, List<TypeNode>> testCase : testCases.entrySet()) {
            List<TypeNode> actual = TypeNode.parseArgumentList(testCase.getKey());
            assertEquals(actual.toString(), testCase.getValue().toString());
        }
    }
}
