package ml.peter_volkov.patcher5;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SmaliTreeModifierConfig {
    private class HookInfo {

        String className;
        String methodName;
        String returnType;
        String argumentsString;
        String stubFileName;

        private HookInfo(String className) {
            this.className = className;
        }
    }

    HashMap<String, List<HookInfo>> methodsToHook = new HashMap<>();

    public SmaliTreeModifierConfig(String configPath) {
        this.readConfig(configPath);
    }

    private void readConfig(String configFilepath) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configFilepath);
        try {
            String configContent = new Scanner(is).useDelimiter("\\A").next();
            JSONArray methodsToHookArray = new JSONArray(configContent);
            for (int i = 0; i < methodsToHookArray.length(); i += 1) {
                JSONObject methodToHook = methodsToHookArray.getJSONObject(i);

                String classNameHumanReadable = methodToHook.getString("class");
                String className = TypeNode.getBytecodeNotationClassName(classNameHumanReadable);
                if (!methodsToHook.containsKey(className)) {
                    methodsToHook.put(className, new ArrayList<HookInfo>());
                }
                if (classNameHumanReadable != null) {
                    HookInfo hookInfo = new HookInfo(className);
                    if (methodToHook.has("method")) {
                        hookInfo.methodName = methodToHook.getString("method");
                    }
                    if (methodToHook.has("returnType")) {
                        hookInfo.returnType = TypeNode.getBytecodeNotationClassName(methodToHook.getString("returnType"));
                    }
                    if (methodToHook.has("argumentTypes")) {
                        List<TypeNode> argumentTypes = new ArrayList<>();
                        JSONArray returnTypesJsonArray = methodToHook.getJSONArray("argumentTypes");
                        for (int j = 0; j < returnTypesJsonArray.length(); j += 1) {
                            String humanReadableTypeName = returnTypesJsonArray.getString(j);
                            TypeNode typeNode = new TypeNode(TypeNode.getBytecodeNotationClassName(humanReadableTypeName));
                            argumentTypes.add(typeNode);
                        }
                        hookInfo.argumentsString = TypeNode.getArgumentString(argumentTypes);
                    }
                    if (methodToHook.has("stubFilename")) {
                        hookInfo.stubFileName = methodToHook.getString("stubFilename");
                    }

                    methodsToHook.get(className).add(hookInfo);
                }
            }
        } catch (Exception e) {
            Log.e("config " + configFilepath + " not found");
        }
    }

    public String getStubFileName(InstructionNode instructionNode) {
        if (instructionNode.isInvokeInstruction && this.methodsToHook.containsKey(instructionNode.className)) {
            for (HookInfo hookInfo : this.methodsToHook.get(instructionNode.className)) {
                // filtering invocation instructions that need to be modified
                //     by class, method name, method return type and method argument types
                if (hookInfo.methodName != null) {
                    if (!hookInfo.methodName.equals(instructionNode.methodName)) {
                        continue;
                    }
                }
                if (hookInfo.returnType != null) {
                    if (!hookInfo.returnType.equals(instructionNode.returnType.toString())) {
                        continue;
                    }
                }
                if (hookInfo.argumentsString != null) {
                    if (!hookInfo.argumentsString.equals(TypeNode.getArgumentString(instructionNode.argumentTypes))) {
                        continue;
                    }
                }
                return hookInfo.stubFileName;
            }
        }
        return null;
    }

    public boolean needsToBeHooked(InstructionNode instructionNode) {
        if (instructionNode.isInvokeInstruction && this.methodsToHook.containsKey(instructionNode.className)) {
            for (HookInfo hookInfo : this.methodsToHook.get(instructionNode.className)) {
                // filtering invocation instructions that need to be modified
                //     by class, method name, method return type and method argument types
                if (hookInfo.methodName != null) {
                    if (!hookInfo.methodName.equals(instructionNode.methodName)) {
                        continue;
                    }
                }
                if (hookInfo.returnType != null) {
                    if (!hookInfo.returnType.equals(instructionNode.returnType.toString())) {
                        continue;
                    }
                }
                if (hookInfo.argumentsString != null) {
                    if (!hookInfo.argumentsString.equals(TypeNode.getArgumentString(instructionNode.argumentTypes))) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
