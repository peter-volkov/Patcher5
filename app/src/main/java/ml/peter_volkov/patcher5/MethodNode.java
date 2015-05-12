package ml.peter_volkov.patcher5;

import android.text.TextUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MethodNode {

    String name;
    String descriptor;
    TypeNode returnType;
    boolean isConstructor = false;
    int numberOfRegisters = 0;
    List<String> access = new ArrayList<>();
    List<TypeNode> argumentTypes = new ArrayList<>();
    List<InstructionNode> instructions = new ArrayList<>();
    HashMap<String, LabelNode> labels = new HashMap<>();
    List<TryBlockNode> tryBlocks = new ArrayList<>();
    boolean isDynamicallyCreated = false;

    public MethodNode() {
        this.isDynamicallyCreated = true;
    }

    public MethodNode(List<String> methodLines) {
        this.parse(methodLines);
    }

    private void parseDescriptor() {
        //skip avoiding
        if (!this.isDynamicallyCreated) {
            int openIndex = this.descriptor.indexOf('(');
            int closeIndex = this.descriptor.indexOf(')');
            this.returnType = new TypeNode(this.descriptor.substring(closeIndex + 1, this.descriptor.length()));
            String argumentsString = this.descriptor.substring(openIndex + 1, closeIndex);

            this.argumentTypes = TypeNode.parseArgumentList(argumentsString);
        }
    }

    private void parse(List<String> methodLines) {
        List<String> tryBlockTmpBuffer = new ArrayList<String>();
        String[] segments;

        for (int index = 0; index < methodLines.size(); index += 1) {
            String line = methodLines.get(index);

            segments = line.split("\\s+");
            //processing first line ".method <access-spec> <method-spec>
            if (line.startsWith(".method")) {
                this.access = Arrays.asList(Arrays.copyOfRange(segments, 1, segments.length - 1));
                this.descriptor = segments[segments.length - 1];
                this.name = this.descriptor.split("\\(")[0];

                //processing second line ".registers <register-num>"
            } else if (line.startsWith(".registers")) {
                this.numberOfRegisters = Integer.parseInt(segments[1]);

            } else if (line.startsWith(":")) {
                //TODO: fix bug with index here
                LabelNode label = new LabelNode(line, this.instructions.size());
                this.labels.put(label.name, label);
            } else if (line.startsWith(".catch")) {
                // .catch <classname> {<label1> .. <label2>} <label3>
                // .catchall {<label1> .. <label2>} <label3>
                tryBlockTmpBuffer.add(line);
//            } else if (line.startsWith(".packed-switch") || line.startsWith(".sparse-switch")) {
//                //danger here!
//                LabelNode label = this.labels.get(methodLines.get(index - 1));
//                List<String> switchTmpBuffer = new ArrayList<String>();
//                do {
//                    switchTmpBuffer.add(line);
//                    index += 1;
//                    line = line = methodLines.get(index);
//                } while (!line.startsWith(".end"));
//                switchTmpBuffer.add(line);
//                index += 1;
//
//                //WTF?? TODO: do something
//                // example ['.packed-switch 0x1', ':pswitch_17', ':pswitch_1d', ':pswitch_50', ':pswitch_56', ':pswitch_5c', ':pswitch_62', ':pswitch_68', '.end packed-switch']
//                SwitchNode switchNode = new SwitchNode(switchTmpBuffer, label);
//            } else if (line.startsWith(".array-data")) {
//
//            } else if (line.startsWith(".annotation")) {

            } else {
                //pass instructions that will not be modified as is
                this.instructions.add(new InstructionNode(line));
            }
        }

        for (String line: tryBlockTmpBuffer) {
            this.tryBlocks.add(new TryBlockNode(line, labels));
        }

        if (this.name.equals("<init>")) {
            this.isConstructor = true;
        }
    }

    public List<String> getSmaliText() {
        this.parseDescriptor();
        List<String> smaliText = new ArrayList<>();

        for (InstructionNode instructionNode : this.instructions) {
            smaliText.add(instructionNode.getSmaliText());
        }

        //sort labels by index
        List<LabelNode> labelNodes = new ArrayList<>(labels.values());
        Collections.sort(labelNodes);
        int insertedInstructionsCount = 0;

        try {
            for (LabelNode labelNode : labelNodes) {
                smaliText.add(labelNode.index + insertedInstructionsCount, labelNode.getSmaliText());
                insertedInstructionsCount += 1;
                for (TryBlockNode tryBlockNode : labelNode.tryBlocks) {
                    smaliText.add(labelNode.index + insertedInstructionsCount, tryBlockNode.getSmaliText());
                    insertedInstructionsCount += 1;
                }
            }

            if (this.numberOfRegisters > 0) {
                smaliText.add(0, String.format(".registers %d", this.numberOfRegisters));
            } else if (!this.access.contains("abstract") && !this.access.contains("final") && !this.access.contains("native")) {
                smaliText.add(0, ".registers 0");
            }
            smaliText.add(0, String.format(".method %s %s(%s)%s", TextUtils.join(" ", this.access), this.name, TypeNode.getArgumentString(this.argumentTypes), this.returnType));
        } catch (Exception e) {
            Log.e(e.getMessage());
        }

        if (!smaliText.get(smaliText.size() - 1).equals(".end method")) {
            smaliText.add(".end method");
        }

        return smaliText;
    }
}