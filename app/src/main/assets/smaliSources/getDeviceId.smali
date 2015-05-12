.method public static getDeviceId(Landroid/telephony/TelephonyManager;)Ljava/lang/String;
    .registers 13
    .param p0    # Landroid/telephony/TelephonyManager;

    const-wide v4, 0x144a21cd245a1L

    const-wide v2, 0x1449d74ba7da1L

    new-instance v1, Ljava/util/Random;

    invoke-direct {v1}, Ljava/util/Random;-><init>()V

    invoke-virtual {v1}, Ljava/util/Random;->nextDouble()D

    move-result-wide v8

    sub-long v10, v2, v4

    long-to-double v10, v10

    mul-double/2addr v8, v10

    double-to-long v8, v8

    add-long v6, v4, v8

    invoke-static {v6, v7}, Ljava/lang/String;->valueOf(J)Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
