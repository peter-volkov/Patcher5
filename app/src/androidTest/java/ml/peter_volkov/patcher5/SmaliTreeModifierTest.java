package ml.peter_volkov.patcher5;

import junit.framework.TestCase;

public class SmaliTreeModifierTest extends TestCase {
    String originalSmaliSourcesDir;
    SmaliTree smaliTree;
    SmaliTreeModifier smaliTreeModifier;

    public void setUp() {
        this.originalSmaliSourcesDir = "/sdcard/smali_games/signing/original_smali/";
        this.smaliTree = new SmaliTree(originalSmaliSourcesDir);
        this.smaliTreeModifier = new SmaliTreeModifier(smaliTree);
    }
    public void testConfigLoad() {

        String configFilePath = "assets/config/method_hooks.json";
        //this.smaliTreeModifier.modify(configFilePath);
    }
    public void testModifyInstruction() {
        //original invoke-direct {v0, v3}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
        //modified invoke-static {v3}, Lno_root_privacy/android/content/Intent;->no_root_privacy_cons(Ljava/lang/String;)V
        //modified invoke-direct {v0, v3}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

        //original invoke-direct {v2, p1}, Lorg/apache/http/client/methods/HttpPost;-><init>(Ljava/lang/String;)V
        //modified invoke-static {p1}, Lno_root_privacy/org/apache/http/client/methods/HttpPost;->no_root_privacy_cons(Ljava/lang/String;)V
        //modified invoke-direct {v2, p1}, Lorg/apache/http/client/methods/HttpPost;-><init>(Ljava/lang/String;)V

        InstructionNode testCase = new InstructionNode("invoke-virtual {v0, v5}, Landroid/location/LocationManager;->getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;");
        String correct = String.format("invoke-static {v0, v5}, L%s/android/location/LocationManager;->getLastKnownLocation(Landroid/location/LocationManager;Ljava/lang/String;)Landroid/location/Location;",
                SmaliTreeModifier.agentPackagePrefix);
        this.smaliTreeModifier.modifyInstruction(testCase);
        String actual = testCase.getSmaliText();
        assertEquals(correct, actual);

        testCase = new InstructionNode("invoke-virtual {v4}, Landroid/net/wifi/WifiManager;->getConfiguredNetworks()Ljava/util/List;");
        correct = String.format("invoke-static {v4}, L%s/android/net/wifi/WifiManager;->getConfiguredNetworks(Landroid/net/wifi/WifiManager;)Ljava/util/List;",
                SmaliTreeModifier.agentPackagePrefix);
        this.smaliTreeModifier.modifyInstruction(testCase);
        actual = testCase.getSmaliText();
        assertEquals(correct, actual);

        testCase = new InstructionNode("invoke-static {v3}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;");
        correct = String.format("invoke-static {v3}, L%s/android/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;",
                SmaliTreeModifier.agentPackagePrefix);
        this.smaliTreeModifier.modifyInstruction(testCase);
        actual = testCase.getSmaliText();
        assertEquals(correct, actual);

        testCase = new InstructionNode("invoke-interface {v1, v2}, Lorg/apache/http/client/HttpClient;->execute(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;");
        correct = String.format("invoke-static {v1, v2}, L%s/org/apache/http/client/HttpClient;->execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;",
                SmaliTreeModifier.agentPackagePrefix);
        this.smaliTreeModifier.modifyInstruction(testCase);
        actual = testCase.getSmaliText();
        assertEquals(correct, actual);

        testCase = new InstructionNode("invoke-virtual/range {v0 .. v5}, Landroid/telephony/SmsManager;->sendTextMessage(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V");
        correct = String.format("invoke-static/range {v0 .. v5}, L%s/android/telephony/SmsManager;->sendTextMessage(Landroid/telephony/SmsManager;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V",
                SmaliTreeModifier.agentPackagePrefix);
        this.smaliTreeModifier.modifyInstruction(testCase);
        actual = testCase.getSmaliText();
        assertEquals(correct, actual);

    }

}
