package com.lany.uitransfer.compiler.utils;

import com.squareup.javapoet.ClassName;


public class ClassNameUtils {
    public static final ClassName VOID = ClassName.get("java.lang", "Void");
    public static final ClassName BOOLEAN = ClassName.get("java.lang", "Boolean");
    public static final ClassName BYTE = ClassName.get("java.lang", "Byte");
    public static final ClassName SHORT = ClassName.get("java.lang", "Short");
    public static final ClassName INT = ClassName.get("java.lang", "Integer");
    public static final ClassName LONG = ClassName.get("java.lang", "Long");
    public static final ClassName CHAR = ClassName.get("java.lang", "Character");
    public static final ClassName FLOAT = ClassName.get("java.lang", "Float");
    public static final ClassName DOUBLE = ClassName.get("java.lang", "Double");

    public static final ClassName INTENT = ClassName.get("android.content", "Intent");
    public static final ClassName BUNDLE = ClassName.get("android.os", "Bundle");
    public static final ClassName CONTEXT = ClassName.get("android.content", "Context");
}
