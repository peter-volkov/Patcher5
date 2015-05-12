package ml.peter_volkov.patcher5;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by peevo on 02.05.2015.
 */
public class FieldNode {

    String name;
    String type;
    String value;
    List<String> access = new ArrayList<String>();

    public FieldNode(String line) {
        this.parse(line);
    }

    private void parse(String line) {
        int delimiterIndex = line.indexOf('=');
        String[] segments;
        if (delimiterIndex > 0) {
            segments = line.substring(0, delimiterIndex).split("\\s+");
            this.value = line.substring(delimiterIndex + 1).trim();
        } else {
            segments = line.split("\\s+");
        }
        this.access = Arrays.asList(Arrays.copyOfRange(segments, 1, segments.length - 1));
        this.name = segments[segments.length - 1].split(":")[0];
        this.type = segments[segments.length - 1].split(":")[1];
    }

    public String getSmaliText() {
        String line = String.format(".field %s %s:%s", TextUtils.join(" ", this.access), this.name, this.type);
        if (this.value != null) {
            line += String.format(" = %s", this.value);
        }
        return line;
    }
}