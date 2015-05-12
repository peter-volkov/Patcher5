package ml.peter_volkov.patcher5;

import junit.framework.TestCase;

public class TryBlockTest extends TestCase{

    public void testTryBlock() {

        String correct = ".catch Ljava/lang/Exception; {:no_root_privacy_try_start .. :no_root_privacy_try_end} :no_root_privacy_handler";

        MethodNode method = new MethodNode();
        method.instructions.add(new InstructionNode("dummy 1"));
        method.instructions.add(new InstructionNode("dummy 2"));
        method.instructions.add(new InstructionNode("dummy 3"));

        LabelNode start = new LabelNode(":no_root_privacy_try_start", 0);
        LabelNode end = new LabelNode(":no_root_privacy_try_end", 1);
        LabelNode ret = new LabelNode(":no_root_privacy_return", method.instructions.size() - 1);
        LabelNode handler = new LabelNode(":no_root_privacy_handler", method.instructions.size());

        TryBlockNode tryBlockNode = new TryBlockNode(correct, start, end, handler);

        method.labels.put(start.name, start);
        method.labels.put(end.name, end);
        method.labels.put(ret.name, ret);
        method.labels.put(handler.name, handler);

        assertEquals(correct, method.getSmaliText().get(5));
    }

    public void testTryBlock2() {

        String correct = ".catch Lorg/apache/http/client/ClientProtocolException; {:try_start_a .. :try_end_33} :catch_34";
        MethodNode method = new MethodNode();
        method.instructions.add(new InstructionNode("dummy 1"));
        method.instructions.add(new InstructionNode("dummy 2"));
        method.instructions.add(new InstructionNode("dummy 3"));

        LabelNode start = new LabelNode(":try_start_a", 0);
        LabelNode end = new LabelNode(":try_end_33", 1);
        LabelNode ret = new LabelNode(":no_root_privacy_return", method.instructions.size() - 1);
        LabelNode handler = new LabelNode(":catch_34", method.instructions.size());

        TryBlockNode tryBlockNode = new TryBlockNode(correct, start, end, handler);

        method.labels.put(start.name, start);
        method.labels.put(end.name, end);
        method.labels.put(ret.name, ret);
        method.labels.put(handler.name, handler);

        assertEquals(correct, method.getSmaliText().get(5));
    }
}
