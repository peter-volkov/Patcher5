.method public postData(Ljava/lang/String;)V
    .registers 9
    .param p1, "URL"    # Ljava/lang/String;
    .prologue
    .line 99
    new-instance v1, Lorg/apache/http/impl/client/DefaultHttpClient;
    invoke-direct {v1}, Lorg/apache/http/impl/client/DefaultHttpClient;-><init>()V
    .line 100
    .local v1, "httpclient":Lorg/apache/http/client/HttpClient;
    new-instance v2, Lorg/apache/http/client/methods/HttpPost;
    invoke-direct {v2, p1}, Lorg/apache/http/client/methods/HttpPost;-><init>(Ljava/lang/String;)V
    .line 104
    .local v2, "httppost":Lorg/apache/http/client/methods/HttpPost;
    :try_start_a
    new-instance v3, Ljava/util/ArrayList;
    const/4 v4, 0x2
    invoke-direct {v3, v4}, Ljava/util/ArrayList;-><init>(I)V
    .line 105
    .local v3, "nameValuePairs":Ljava/util/List;, "Ljava/util/List<Lorg/apache/http/NameValuePair;>;"
    new-instance v4, Lorg/apache/http/message/BasicNameValuePair;
    const-string v5, "secretInfo1"
    const-string v6, "testtest"
    invoke-direct {v4, v5, v6}, Lorg/apache/http/message/BasicNameValuePair;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 106
    new-instance v4, Lorg/apache/http/message/BasicNameValuePair;
    const-string v5, "secretInfo2"
    const-string v6, "testtest"
    invoke-direct {v4, v5, v6}, Lorg/apache/http/message/BasicNameValuePair;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 107
    new-instance v4, Lorg/apache/http/client/entity/UrlEncodedFormEntity;
    invoke-direct {v4, v3}, Lorg/apache/http/client/entity/UrlEncodedFormEntity;-><init>(Ljava/util/List;)V
    invoke-virtual {v2, v4}, Lorg/apache/http/client/methods/HttpPost;->setEntity(Lorg/apache/http/HttpEntity;)V
    .line 110
    invoke-interface {v1, v2}, Lorg/apache/http/client/HttpClient;->execute(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;
    :try_end_33
    .catch Lorg/apache/http/client/ClientProtocolException; {:try_start_a .. :try_end_33} :catch_34
    .catch Ljava/io/IOException; {:try_start_a .. :try_end_33} :catch_3f
    .line 117
    .end local v3    # "nameValuePairs":Ljava/util/List;, "Ljava/util/List<Lorg/apache/http/NameValuePair;>;"
    :goto_33
    return-void
    .line 112
    :catch_34
    move-exception v0
    .line 113
    .local v0, "e":Lorg/apache/http/client/ClientProtocolException;
    const-string v4, "HTTP send error"
    invoke-virtual {v0}, Lorg/apache/http/client/ClientProtocolException;->getMessage()Ljava/lang/String;
    move-result-object v5
    invoke-virtual {p0, v4, v5}, Lml/peter_volkov/testapp/MainActivity;->showAlert(Ljava/lang/String;Ljava/lang/String;)V
    goto :goto_33
    .line 114
    .end local v0    # "e":Lorg/apache/http/client/ClientProtocolException;
    :catch_3f
    move-exception v0
    .line 115
    .local v0, "e":Ljava/io/IOException;
    const-string v4, "HTTP send error"
    invoke-virtual {v0}, Ljava/io/IOException;->getMessage()Ljava/lang/String;
    move-result-object v5
    invoke-virtual {p0, v4, v5}, Lml/peter_volkov/testapp/MainActivity;->showAlert(Ljava/lang/String;Ljava/lang/String;)V
    goto :goto_33
.end method
