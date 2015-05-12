.class public Lno_root_privacy/apimonitor/Agent;
.super Ljava/lang/Object;
.source "Agent.java"


# static fields
.field static loggerTag:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 7
    const-string v0, "no_root_privacy"

    sput-object v0, Lno_root_privacy/apimonitor/Agent;->loggerTag:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static log(Ljava/lang/String;)V
    .registers 4
    .param p0, "paramString"    # Ljava/lang/String;

    .prologue
    .line 10
    sget-object v0, Lno_root_privacy/apimonitor/Agent;->loggerTag:Ljava/lang/String;

    const-string v1, "\\r?\\n"

    const-string v2, "\\\\n"

    invoke-virtual {p0, v1, v2}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I

    .line 11
    return-void
.end method

.method public static toString(Ljava/lang/Object;)Ljava/lang/String;
    .registers 6
    .param p0, "object"    # Ljava/lang/Object;

    .prologue
    .line 14
    if-nez p0, :cond_5

    .line 15
    const-string v3, "null"

    .line 24
    :goto_4
    return-object v3

    .line 16
    :cond_5
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/Class;->isArray()Z

    move-result v3

    if-eqz v3, :cond_3c

    .line 17
    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "{"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 18
    .local v2, "stringBuilder":Ljava/lang/StringBuilder;
    invoke-static {p0}, Ljava/lang/reflect/Array;->getLength(Ljava/lang/Object;)I

    move-result v0

    .line 19
    .local v0, "arrayLength":I
    const/4 v1, 0x0

    .local v1, "index":I
    :goto_1b
    if-ge v1, v0, :cond_31

    .line 20
    invoke-static {p0, v1}, Ljava/lang/reflect/Array;->get(Ljava/lang/Object;I)Ljava/lang/Object;

    move-result-object v3

    invoke-static {v3}, Lno_root_privacy/apimonitor/Agent;->toString(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, ", "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 19
    add-int/lit8 v1, v1, 0x1

    goto :goto_1b

    .line 22
    :cond_31
    const-string v3, "}"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    goto :goto_4

    .line 24
    .end local v0    # "arrayLength":I
    .end local v1    # "index":I
    .end local v2    # "stringBuilder":Ljava/lang/StringBuilder;
    :cond_3c
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/Class;->toString()Ljava/lang/String;

    move-result-object v3

    goto :goto_4
.end method
