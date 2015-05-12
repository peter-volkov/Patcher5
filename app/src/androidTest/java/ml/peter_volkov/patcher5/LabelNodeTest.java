package ml.peter_volkov.patcher5;

import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class LabelNodeTest extends TestCase {
    public void setUp() {
        String a = "sdsa";
        int dd = 12;
        //this.arbitraryArgs(a, dd);
    }

    public String arbitraryArgs(Object... arguments) {
        List<Object> argumentsList = Arrays.asList(arguments);
        StringBuilder sb = new StringBuilder();
        String instance = argumentsList.get(0).toString();
        sb.append(instance);
        //argumentsList.remove(0);
        sb.append("(");
        for (Object argument : argumentsList) {
            sb.append(argument.toString());
            sb.append(",");
        }
        sb.append(");");
        return sb.toString();
    }

    public void testSomething() {

    }

    public void testAgent() {
        StringBuilder stringBuilder = new StringBuilder("{").append("dsada");
        Agent.log("asdasd\nasdfasf\naasdasf");
        Agent.log(Agent.toString(stringBuilder));
    }
}
