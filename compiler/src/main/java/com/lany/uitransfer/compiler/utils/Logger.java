package com.lany.uitransfer.compiler.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class Logger {
    private Messager msg;

    public Logger(Messager messager) {
        msg = messager;
    }

    public void i(CharSequence info) {
        if (isEmpty(info)) {
            msg.printMessage(Diagnostic.Kind.NOTE, "Compiler:" + info);
        }
    }

    public void e(CharSequence error) {
        if (isEmpty(error)) {
            msg.printMessage(Diagnostic.Kind.ERROR, "Compiler:" + "An exception is encountered, [" + error + "]");
        }
    }

    public void e(Throwable error) {
        if (null != error) {
            msg.printMessage(Diagnostic.Kind.ERROR, "Compiler:" + "An exception is encountered, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }

    public void w(CharSequence warning) {
        if (isEmpty(warning)) {
            msg.printMessage(Diagnostic.Kind.WARNING, "Compiler:" + warning);
        }
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.equals("");
    }
}