package ml.peter_volkov.patcher5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SmaliTreeModifier {

    public static String agentPackagePrefix = "no_root_privacy";

    HashMap<String, ClassNode> stubClasses = new HashMap<>();

    //{className: {methodName: methodNone}}
    HashSet<String> stubMethods = new HashSet<>();

    SmaliTree smaliTree;
    SmaliTreeModifierConfig config;
    //HashMap<String, ClassNode> stubClasses = new HashMap<>();

    public SmaliTreeModifier(SmaliTree smaliTree) {
        this.smaliTree = smaliTree;
    }

    public void modify(String configFilepath) {
        //basically list of methods to hook
        config = new SmaliTreeModifierConfig(configFilepath);

        //changing invokation instruction instructions, which calls methods listed in config
        //to make them call injected stub methods instead
        this.modifyMethods();

        //inserting main instrumentation module that contains utils
        // such as logging, communicating to decision service etc
        this.injectClass("assets/smaliSources/Agent.smali");

        //insert dynamically created stub methods to hook methods listed in config
        this.injectStubClasses();
    }

    private List<String> readAssetTextFile(String assetFilePath) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(assetFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (Exception e) {
            Log.e(assetFilePath + " not found");
        }
        return null;
    }

    private void injectClass(String classSourceFilepath) {
        this.smaliTree.classes.add(new ClassNode(this.readAssetTextFile(classSourceFilepath)));
    }

    private void injectMethod(ClassNode classNode, String methodSourceFilepath) {
        MethodNode methodNode = new MethodNode(this.readAssetTextFile(methodSourceFilepath));
        classNode.methods.add(methodNode);
    }

    private void injectStubClasses() {
        for (ClassNode stubClass : this.stubClasses.values()) {
            this.smaliTree.classes.add(stubClass);
        }
    }

    private void modifyMethods() {
        for (ClassNode classNode : this.smaliTree.classes) {
            for (MethodNode methodNode : classNode.methods) {
                this.modifyMethod(methodNode);
            }
        }
    }

    private ClassNode getStubClass(String className) {
        ClassNode stubClass = new ClassNode();
        //Landroid/telephony/SmsManager; -> Lno_root_privacy/android/telephony/SmsManager;
        stubClass.name = className;//String.format("L%s/%s", agentPackagePrefix, className.substring(1));
        //stubClass.name = String.format("L%s/%s", agentPackagePrefix, className.substring(1));
        stubClass.superName = "Ljava/lang/Object;";
        stubClass.access.add("public");

        MethodNode defaultConstructor = new MethodNode();
        defaultConstructor.name = "<init>";
        defaultConstructor.access.add("public");
        defaultConstructor.access.add("constructor");
        defaultConstructor.returnType = new TypeNode(TypeNode.getBytecodeNotationClassName("void"));
        defaultConstructor.numberOfRegisters = 1;
        defaultConstructor.instructions.add(new InstructionNode("invoke-direct {p0}, Ljava/lang/Object;-><init>()V"));
        defaultConstructor.instructions.add(new InstructionNode("return-void"));

        stubClass.methods.add(defaultConstructor);

        return stubClass;
    }

    private MethodNode getStubMethod(InstructionNode originalInstruction, InstructionNode modifiedInstruction) {

        MethodNode stubMethod = new MethodNode();
        stubMethod.name = modifiedInstruction.methodName;
        stubMethod.access.add("public");
        stubMethod.access.add("static");
        stubMethod.returnType = modifiedInstruction.returnType;
        stubMethod.argumentTypes = modifiedInstruction.argumentTypes;

        stubMethod.numberOfRegisters = TypeNode.getRegisterCount(stubMethod.argumentTypes);

        int registerIndex = 1;

        if (stubMethod.numberOfRegisters <= 5) {
            originalInstruction.isRangeFormat = false;
            originalInstruction.registerList = new ArrayList<>();
            for (int i = 0; i < stubMethod.numberOfRegisters; i+= 1) {
                originalInstruction.registerList.add(String.format("p%d", i));
            }
        } else {
            originalInstruction.isRangeFormat = true;
            originalInstruction.startRegister = "p0";
            originalInstruction.endRegister = String.format("p%d", stubMethod.numberOfRegisters - 1);
        }

        //returning original method's result
        stubMethod.instructions.add(originalInstruction);

        if (!stubMethod.returnType.isVoid) {
            registerIndex += 1;
            String instructionString = "move-result-object v1";
            if (stubMethod.returnType.isBasicType && stubMethod.returnType.arrayDimension == 0 && !stubMethod.returnType.isWide) {
                instructionString = "move-result v1";
            } else if (stubMethod.returnType.isWide) {
                registerIndex += 1;
                instructionString = "move-result-wide v1";
            }
            stubMethod.instructions.add(new InstructionNode(instructionString));
        }

        stubMethod.instructions.add(new InstructionNode(String.format("new-instance v%d, Ljava/lang/StringBuilder;", registerIndex)));
        stubMethod.instructions.add(new InstructionNode(String.format("invoke-direct {v%d}, Ljava/lang/StringBuilder;-><init>()V", registerIndex)));
        String instructionString = String.format("const-string v%d, \"%s->%s(\"", registerIndex + 1, originalInstruction.className, originalInstruction.methodName);
        stubMethod.instructions.add(new InstructionNode(instructionString));
        instructionString = String.format("invoke-virtual {v%d, v%d}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;", registerIndex, registerIndex + 1);
        InstructionNode appendInstruction = new InstructionNode(instructionString);
        stubMethod.instructions.add(appendInstruction);

        //print arguments
        int argumentIndex = 1;
        //skipping zeroth element, cause it is object instance artificially added during modification
        for (int i = 1; i < modifiedInstruction.argumentTypes.size(); i += 1) {
            TypeNode argumentType = modifiedInstruction.argumentTypes.get(i);
            instructionString = String.format("const-string v%d, \"%s=\"", registerIndex + 1, argumentType.toString());
            stubMethod.instructions.add(new InstructionNode(instructionString));
            stubMethod.instructions.add(appendInstruction);
            if (argumentType.isBasicType && argumentType.arrayDimension == 0) {
                if (!argumentType.isWide) {
                    instructionString = String.format("invoke-static {p%d}, Ljava/lang/String;->valueOf(%s)Ljava/lang/String;", argumentIndex, argumentType.toString());
                    stubMethod.instructions.add(new InstructionNode(instructionString));
                } else {
                    instructionString = String.format("invoke-static {p%d, p%d}, Ljava/lang/String;->valueOf(%s)Ljava/lang/String;", argumentIndex, argumentIndex + 1, argumentType.toString());
                    stubMethod.instructions.add(new InstructionNode(instructionString));
                    argumentIndex += 1;
                }
            } else {
                instructionString = String.format("invoke-static {p%d}, Lno_root_privacy/apimonitor/Helper;->toString(Ljava/lang/Object;)Ljava/lang/String;", argumentIndex);
                stubMethod.instructions.add(new InstructionNode(instructionString));
            }
            argumentIndex += 1;
            stubMethod.instructions.add(new InstructionNode(String.format("move-result-object v%d", registerIndex + 1)));
            stubMethod.instructions.add(appendInstruction);

            //for all except last
            if (argumentType != modifiedInstruction.argumentTypes.get(modifiedInstruction.argumentTypes.size() - 1)) {
                stubMethod.instructions.add(new InstructionNode(String.format("const-string v%d, \" | \"", registerIndex + 1)));
                stubMethod.instructions.add(appendInstruction);
            }
        }

        stubMethod.instructions.add(new InstructionNode(String.format("const-string v%d, \")\"", registerIndex + 1)));
        stubMethod.instructions.add(appendInstruction);

        //print return value
        if (!modifiedInstruction.returnType.isVoid) {
            stubMethod.instructions.add(new InstructionNode(String.format("const-string v%d, \"%s=\"", registerIndex + 1, modifiedInstruction.returnType.toString())));
            stubMethod.instructions.add(appendInstruction);
            if (modifiedInstruction.returnType.isBasicType && modifiedInstruction.returnType.arrayDimension == 0) {
                if (modifiedInstruction.returnType.isWide) {
                    instructionString = String.format("invoke-static {v1, v2}, Ljava/lang/String;->valueOf(%s)Ljava/lang/String;", modifiedInstruction.returnType.toString());
                } else {
                    instructionString = String.format("invoke-static {v1}, Ljava/lang/String;->valueOf(%s)Ljava/lang/String;", modifiedInstruction.returnType.toString());
                }
            } else {
                instructionString = "invoke-static {v1}, Lno_root_privacy/apimonitor/Helper;->toString(Ljava/lang/Object;)Ljava/lang/String;";
            }
            stubMethod.instructions.add(new InstructionNode(instructionString));
            stubMethod.instructions.add(new InstructionNode(String.format("move-result-object v%d", registerIndex + 1)));
            stubMethod.instructions.add(appendInstruction);
        } else {
            stubMethod.instructions.add(new InstructionNode(String.format("const-string v%d, \"%s\"", registerIndex + 1, modifiedInstruction.returnType.toString())));
            stubMethod.instructions.add(appendInstruction);
        }

        stubMethod.instructions.add(new InstructionNode(String.format("invoke-virtual {v%d}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;", registerIndex)));
        stubMethod.instructions.add(new InstructionNode(String.format("move-result-object v%d", registerIndex + 1)));
        stubMethod.instructions.add(new InstructionNode(String.format("invoke-static {v%d}, Lno_root_privacy/apimonitor/Helper;->log(Ljava/lang/String;)V", registerIndex + 1)));

        if (!modifiedInstruction.returnType.isVoid) {
            if (modifiedInstruction.returnType.isBasicType && modifiedInstruction.returnType.arrayDimension == 0) {
                if (modifiedInstruction.returnType.isWide) {
                    stubMethod.instructions.add(new InstructionNode("return-wide v1"));
                } else {
                    stubMethod.instructions.add(new InstructionNode("return v1"));
                }
            } else {
                stubMethod.instructions.add(new InstructionNode("return-object v1"));
            }
        } else {
            stubMethod.instructions.add(new InstructionNode("return-void"));
        }

        LabelNode start = new LabelNode(":no_root_privacy_try_start", 0);
        LabelNode end = new LabelNode(":no_root_privacy_try_end", 1);
        LabelNode ret = new LabelNode(":no_root_privacy_return", stubMethod.instructions.size() - 1);
        LabelNode handler = new LabelNode(":no_root_privacy_handler", stubMethod.instructions.size());
        String instruction = ".catch Ljava/lang/Exception; {:no_root_privacy_try_start .. :no_root_privacy_try_end} :no_root_privacy_handler";

        //TODO: WTF
        TryBlockNode tryBlockNode = new TryBlockNode(instruction, start, end, handler);
        //stubMethod.tryBlocks.add(tryBlockNode);

        stubMethod.labels.put(start.name, start);
        stubMethod.labels.put(end.name, end);
        stubMethod.labels.put(ret.name, ret);
        stubMethod.labels.put(handler.name, handler);

        stubMethod.instructions.add(new InstructionNode("move-exception v0"));
        stubMethod.instructions.add(new InstructionNode("invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V"));

        if (!modifiedInstruction.returnType.isVoid) {
            if (modifiedInstruction.returnType.isWide) {
                stubMethod.instructions.add(new InstructionNode("const-wide/16 v1, 0x0"));
            } else {
                stubMethod.instructions.add(new InstructionNode("const/4 v1, 0x0"));
            }
        }

        stubMethod.instructions.add(new InstructionNode("goto :no_root_privacy_return"));

        stubMethod.numberOfRegisters += registerIndex + 2;

        return stubMethod;
    }

    private void addStubMethod(InstructionNode originalInstruction, InstructionNode modifiedInstruction) {
        if (!this.stubClasses.containsKey(modifiedInstruction.className)) {
            this.stubClasses.put(modifiedInstruction.className, this.getStubClass(modifiedInstruction.className));
        }
        String methodDescriptor = modifiedInstruction.getDescriptor();
        if (!this.stubMethods.contains(methodDescriptor)) {
            String stubFileName = config.getStubFileName(originalInstruction);
            if (stubFileName != null) {
                //special stub code
                MethodNode stubMethod = new MethodNode(this.readAssetTextFile("assets/smaliSources/" + stubFileName));
                this.stubClasses.get(modifiedInstruction.className).methods.add(stubMethod);
            } else {
                MethodNode stubMethod = this.getStubMethod(originalInstruction, modifiedInstruction);
                this.stubClasses.get(modifiedInstruction.className).methods.add(stubMethod);
                this.stubMethods.add(methodDescriptor);
            }
        }
    }

    public InstructionNode getModifiedInstruction(InstructionNode instructionNode) {
        // if method calling by the instruction is a constructor
//                if (instructionNode.methodName.equals("<init>")) {
//                    InstructionNode
//                    methodNode.instructions.add(position, new InstructionNode());
//                } else {
//                    instructionNode.opcodeName = newOpcodeName;
//                    instructionNode.className = this.agentPackagePrefix + instructionNode.className;
//                }

        if (!instructionNode.opcodeName.startsWith("invoke-static")) {
            instructionNode.argumentTypes.add(0, new TypeNode(instructionNode.className));
        }

        String newOpcodeName = "invoke-static";
        if (instructionNode.isRangeFormat) {
            newOpcodeName += "/range";
        }
        instructionNode.opcodeName = newOpcodeName;

        instructionNode.className = String.format("L%s/%s",
                SmaliTreeModifier.agentPackagePrefix,
                instructionNode.className.substring(1)
        );
        return instructionNode;
    }

    private void modifyMethod(MethodNode methodNode) {
        for (int position = 0; position < methodNode.instructions.size(); position += 1) {
            InstructionNode instructionNode = methodNode.instructions.get(position);
            if (instructionNode.isInvokeInstruction && config.needsToBeHooked(instructionNode)) {
                if (instructionNode.opcodeName.startsWith("invoke-direct") || instructionNode.opcodeName.startsWith("invoke-static")) {
                    //skipping static methods and constructors
                    //TODO: support static methods and constructors
                    continue;
                }
                Log.i("Hooking " + instructionNode.getSmaliText());
                InstructionNode originalInstruction = new InstructionNode(instructionNode);
                InstructionNode modifiedInstruction = this.getModifiedInstruction(instructionNode);
                this.addStubMethod(originalInstruction, modifiedInstruction);
                //TODO: iterate super to cover parent classes - may be skipped by now
            }
        }
    }
}
