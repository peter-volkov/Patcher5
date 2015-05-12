package ml.peter_volkov.patcher5;

import java.util.ArrayList;
import java.util.List;


public class SwitchNode {
    List<LabelNode> packedLabels = new ArrayList<LabelNode>();
    LabelNode label;
    String packedValue;
    String type;

    public SwitchNode(List<String> lines, LabelNode label) {
        this.label = label;
        this.parse(lines);
    }
    private void parse(List<String> lines) {
        String[] segments = lines.get(0).split("\\s+");
        this.type = segments[0];
        this.label.switchNode = this;
    }

   public List<String> getSmaliText() {
       List<String> smaliText = new ArrayList<String>();
       //TODO: finish this
       if (this.type.equals(".packed-switch")) {

       } else if (this.type.equals(".sparse-switch")) {

       }
       return smaliText;
   }
}
