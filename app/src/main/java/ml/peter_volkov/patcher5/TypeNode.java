package ml.peter_volkov.patcher5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TypeNode {
    // See reference for bytecode descriptors notation
    // http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3

    static HashMap<String, String> templates = new HashMap<String, String>();
    static HashMap<Character, String> basicTypes = new HashMap<Character, String>();
    static HashMap<String, Character> basicTypesByHumanReadable = new HashMap<String, Character>();
    //static String invokeInstructionTemplate;
    static
    {
        // bytecode basic types to human-readable version
        basicTypes.put('V', "void");
        basicTypes.put('Z', "boolean");
        basicTypes.put('B', "byte");
        basicTypes.put('S', "short");
        basicTypes.put('C', "char");
        basicTypes.put('I', "int");
        basicTypes.put('J', "long");
        basicTypes.put('F', "float");
        basicTypes.put('D', "double");

        // generating inverted basicTypes dictionary for reverse searches \
        // (bytecode_notation, human_readable) -> (human_readable, bytecode_notation)
        for(HashMap.Entry<Character, String> entry : basicTypes.entrySet()){
            basicTypesByHumanReadable.put(entry.getValue(), entry.getKey());
        }
    }

    String type;
    public int arrayDimension = 0;
    public boolean isBasicType = false;
    public boolean isVoid = false;
    public boolean isWide = false;

    public TypeNode(String descriptor) {
        this.parse(descriptor);
    }

    private void parse(String descriptor) {
        this.arrayDimension = descriptor.lastIndexOf('[') + 1;
        descriptor = descriptor.substring(this.arrayDimension);

        //checking if type is Java basic type
        char typeShortcut = descriptor.charAt(0);
        if (basicTypes.get(typeShortcut) != null) {
            this.type = Character.toString(typeShortcut);
            this.isBasicType = true;
            if (typeShortcut == 'V') {
                this.isVoid = true;
            } else if (this.arrayDimension == 0 && (typeShortcut == 'J' || typeShortcut == 'D')) {
                this.isWide = true;
            }
        //if type is a class (not basic)
        } else if (typeShortcut == 'L') {
            this.type = descriptor;
        }
    }

    public static String getHumanReadableClassName(String bytecodeNotationClassName) {
        // "I"
        if (bytecodeNotationClassName.length() == 1) {
            return TypeNode.basicTypes.get(bytecodeNotationClassName.charAt(0));
        } else {
            // "Ljava/util/Random;" -> "java.util.Random"
            return bytecodeNotationClassName.substring(1, bytecodeNotationClassName.length() - 1).replaceAll("\\/", ".");
        }
    }

    public static int getRegisterCount(List<TypeNode> types) {
        int registerCount = 0;
        for (TypeNode argumentType : types) {
            registerCount += 1;
            if (argumentType.isWide) {
                registerCount += 1;
            }
        }
        return registerCount;
    }

    public static String getBytecodeNotationClassName(String humanReadableClassName) {
        // "int"
        if (TypeNode.basicTypesByHumanReadable.get(humanReadableClassName) != null) {
            return TypeNode.basicTypesByHumanReadable.get(humanReadableClassName).toString();
        } else {
            // "java.util.Random" -> "Ljava/util/Random;"
            return String.format("L%s;", humanReadableClassName.replaceAll("\\.", "/"));
        }
    }

    public static List<TypeNode> parseArgumentList(String argumentsString) {
        // WTF block TODO: refactor it - something strange happens with arrays here
        List<TypeNode> arguments = new ArrayList<>();
        int arrayDimension = 0;
        String typeString;
        for (int i = 0; i < argumentsString.length(); i += 1) {
            char c = argumentsString.charAt(i);
            if (c == '[') {
                arrayDimension += 1;
            } else if (TypeNode.basicTypes.containsKey(c)) {
                arguments.add(new TypeNode(argumentsString.substring(i - arrayDimension, i + 1)));
                arrayDimension = 0;
            } else if (c == 'L') {
                int typeDeclarationEndIndex = argumentsString.indexOf(';', i);
                //should be like "Landroid/accessibilityservice/AccessibilityServiceInfo;"
                typeString = argumentsString.substring(i - arrayDimension, typeDeclarationEndIndex  + 1);
                arguments.add(new TypeNode(typeString));
                i += typeString.length() - arrayDimension - 1;
                arrayDimension = 0;
            }
        }
        return arguments;
    }

    public static String getArgumentString(List<TypeNode> types) {
        StringBuilder argumentStringSB = new StringBuilder();
        for (TypeNode typeName : types) {
            argumentStringSB.append(typeName.toString());
        }
        return argumentStringSB.toString();
    }

    @Override
    public String toString() {
        StringBuilder typeSB = new StringBuilder();
        for (int i = 0; i < this.arrayDimension; i++) {
            typeSB.append('[');
        }
        typeSB.append(this.type);
        return typeSB.toString();
    }
}
