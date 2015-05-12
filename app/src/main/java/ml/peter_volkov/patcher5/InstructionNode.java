package ml.peter_volkov.patcher5;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InstructionNode {

    static Pattern invokeInstructionRe;
    static String opcodeNameReTemplate = "[a-zA-Z/-]+";
    static String registerListReTemplate = "[A-Za-z0-9,\\s]+";
    static String registerRangeReTemplate = "[A-Za-z0-9]+ .. [A-Za-z0-9]+";
    static String methodNameReTemplate = "[A-Za-z$0-9_/<>]+";
    static String javaTypeReTemplate = "[a-zA-Z0-9;_/$\\[]+";
    static
    {

        //welcome to the regexp hell!
        //example: invoke-static {v0}, Ljava/lang/Boolean;->parseBoolean(Ljava/lang/String;)Z
        invokeInstructionRe = Pattern.compile(String.format("^(%s) \\{(%s|%s)?\\}, (%s)?->(%s)\\((%s)?\\)(%s)",
                opcodeNameReTemplate,
                registerListReTemplate,
                registerRangeReTemplate,
                javaTypeReTemplate,
                methodNameReTemplate,
                javaTypeReTemplate,
                javaTypeReTemplate));
    }

    String line;
    boolean isInvokeInstruction = false;

    public String methodName;
    public String opcodeName;
    public String className;
    public List<TypeNode> argumentTypes;
    public TypeNode returnType;

    boolean isRangeFormat = false;
    public String startRegister;
    public String endRegister;
    public List<String> registerList;

    public InstructionNode() {

    }

    public InstructionNode(InstructionNode anotherInstruction) {
        this.methodName = anotherInstruction.methodName;
        this.opcodeName = anotherInstruction.opcodeName;
        this.className = anotherInstruction.className;
        this.returnType = anotherInstruction.returnType;
        this.isRangeFormat = anotherInstruction.isRangeFormat;
        this.startRegister = anotherInstruction.startRegister;
        this.endRegister = anotherInstruction.endRegister;
        this.isInvokeInstruction = anotherInstruction.isInvokeInstruction;
        this.line = anotherInstruction.line;

        if (anotherInstruction.argumentTypes != null) {
            this.argumentTypes = new ArrayList<>();
            for (TypeNode type : anotherInstruction.argumentTypes) {
                this.argumentTypes.add(type);
            }
        } else {
            this.argumentTypes = null;
        }

        if (anotherInstruction.registerList != null) {
            this.registerList = new ArrayList<>();
            for (String register : anotherInstruction.registerList) {
                this.registerList.add(register);
            }
        } else {
            this.registerList = null;
        }
    }

    public InstructionNode(String line) {
        this.parse(line);
    }

    public String getDescriptor() {
        return String.format("%s->%s(%s)%s",
                this.className,
                this.methodName,
                TypeNode.getArgumentString(this.argumentTypes),
                this.returnType.toString()
        );
    }

    private void parseRegisterListString(String registerString) {
        //examples: "v0", "v0, v1, v2, p1, p2"
        this.registerList = Arrays.asList(registerString.split(",\\s*"));
    }

    private void parseRegisterRangeString(String registerString) {
        //examples: "v0 .. v12", "p3 .. v3"
        String[] segments = registerString.split("\\s+");
        this.startRegister = segments[0];
        this.endRegister = segments[segments.length - 1];
    }

    private void parse(String line) {
        if (line.startsWith("invoke")) {
            this.isInvokeInstruction = true;
            Matcher match = invokeInstructionRe.matcher(line);
            if (match.matches()) {
                this.opcodeName = (match.group(1) != null) ? match.group(1) : "";
                String registersString = (match.group(2) != null) ? match.group(2) : "";
                this.className = (match.group(3) != null) ? match.group(3) : "";
                this.methodName = (match.group(4) != null) ? match.group(4) : "";
                String argumentsString = (match.group(5) != null) ? match.group(5) : "";
                this.argumentTypes = TypeNode.parseArgumentList(argumentsString);
                String returnTypeString = (match.group(6) != null) ? match.group(6) : "";
                this.returnType = new TypeNode(returnTypeString);

                if (this.opcodeName.endsWith("/range")) {
                    this.isRangeFormat = true;
                    this.parseRegisterRangeString(registersString);
                } else {
                    this.parseRegisterListString(registersString);
                }

            } else {
                Log.e("pattern can't match invoke instruction " + line);
            }
        } else {
            //keeping raw text for non-invoke instructions to return as is
            this.line = line;
        }
    }

    public String getSmaliText() {
        if (this.isInvokeInstruction) {
            //example: invoke-static {v0}, Ljava/lang/Boolean;->parseBoolean(Ljava/lang/String;)Z
            String registersString;
            if (this.isRangeFormat) {
                registersString = String.format("%s .. %s", this.startRegister, this.endRegister);
            } else {
                registersString = TextUtils.join(", ", this.registerList);
            }

            String argumentsString = TypeNode.getArgumentString(this.argumentTypes);

            return String.format("%s {%s}, %s->%s(%s)%s",
                    this.opcodeName,
                    registersString,
                    this.className,
                    this.methodName,
                    argumentsString,
                    this.returnType.toString()
            );
        }
        return this.line;
    }
}
