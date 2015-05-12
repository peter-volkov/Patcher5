package ml.peter_volkov.patcher5;

import java.util.ArrayList;
import java.util.List;

public class LabelNode implements Comparable {
    String name;
    int index = -1;
    public List<TryBlockNode> tryBlocks = new ArrayList<TryBlockNode>();
    public SwitchNode switchNode;

    public LabelNode(String line, int index) {
        this.name = line.substring(1);
        this.index = index;
    }

    public String getSmaliText() {
        return String.format(":%s", this.name);
    }

    @Override
    public String toString() {
        return String.format("Label: %s\n", this.name);
    }

    @Override
    public int compareTo(Object another) {
        LabelNode anotherLabelNode = (LabelNode) another;
        return this.index - anotherLabelNode.index;
    }
}
