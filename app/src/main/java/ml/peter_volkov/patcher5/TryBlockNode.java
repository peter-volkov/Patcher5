package ml.peter_volkov.patcher5;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryBlockNode {

    static String catchRegexpTemplate = "^\\S+\\s+(\\S+)\\s+\\{:(\\S+)\\s+\\.\\.\\s+:(\\S+)\\}\\s+:(\\S+)$";
    static Pattern catchRegexp = Pattern.compile(catchRegexpTemplate);

    String exceptionTypeString;
    LabelNode startLabel;
    LabelNode endLabel;
    LabelNode exceptionHandlerLabel;

    public TryBlockNode(String line, HashMap<String, LabelNode> labels) {
        //line example ".catch Ljava/lang/NoSuchMethodException; {:try_start_8 .. :try_end_2c} :catch_2d"
        Matcher match = catchRegexp.matcher(line);
        if (match.matches()) {
            //Example: Ljava/lang/NoSuchMethodException;
            String exceptionTypeString = match.group(1);

            //Example: try_start_8
            String startLabelName = match.group(2);
            LabelNode startLabel = labels.get(startLabelName);

            //Example: try_end_2c
            String endLabelName = match.group(3);
            LabelNode endLabel = labels.get(endLabelName);

            //Example: catch_2d
            String exceptionHandlerName = match.group(4);
            LabelNode exceptionHandlerLabel = labels.get(exceptionHandlerName);

            this.parse(exceptionTypeString, startLabel, endLabel, exceptionHandlerLabel);
        }
    }

    public TryBlockNode(String line, LabelNode startLabel, LabelNode endLabel, LabelNode exceptionHandlerLabel) {
        String exceptionTypeString = line.split("\\s+")[1];
        this.parse(exceptionTypeString, startLabel, endLabel, exceptionHandlerLabel);
    }

    private void parse(String exceptionTypeString, LabelNode startLabel, LabelNode endLabel, LabelNode exceptionHandlerLabel) {
        this.exceptionTypeString = exceptionTypeString;
        this.startLabel = startLabel;
        this.endLabel = endLabel;
        this.exceptionHandlerLabel = exceptionHandlerLabel;
        endLabel.tryBlocks.add(this);
    }

    public String getSmaliText() {
        //.catch Lorg/apache/http/client/ClientProtocolException; {:try_start_a .. :try_end_33} :catch_34
        return String.format(".catch %s {:%s .. :%s} :%s",
                this.exceptionTypeString,
                this.startLabel.name,
                this.endLabel.name,
                this.exceptionHandlerLabel.name
        );
    }

    @Override
    public String toString() {
        return String.format("Try: %s {%s .. %s} %s",
                this.exceptionTypeString,
                this.startLabel.index,
                this.endLabel.index,
                this.exceptionHandlerLabel.index);
    }
}
