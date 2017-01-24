package com.lany.uitransfer.compiler;

import com.lany.uitransfer.annotaion.TransferField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;


public class CodeGenerator {
    private final String mPackageName;
    private final ClassName mAnnotatedClassName;
    private final ClassName mIntentClassName;
    private final ClassName bundleClassName;
    private String methodName;

    private TypeElement typeElement;

    public CodeGenerator(String packageName, TypeElement typeElement) {
        this.typeElement = typeElement;
        mPackageName = packageName;
        methodName = typeElement.getSimpleName().toString();
        if (methodName.contains("activity")) {
            methodName = methodName.replace("activity", "");
        }
        if (methodName.contains("Activity")) {
            methodName = methodName.replace("Activity", "");
        }
        mAnnotatedClassName = ClassName.get(packageName, typeElement.getSimpleName().toString());
        mIntentClassName = ClassName.get("android.content", "Intent");
        bundleClassName = ClassName.get("android.os", "Bundle");
    }

    public void generateJavaFile(Filer filer) throws IOException {
        JavaFile javaFile = JavaFile.builder(mPackageName, generateCode())
                .addFileComment("Generated code from UITransfer. Do not modify!")
                .build();
        javaFile.writeTo(filer);
    }

    private TypeSpec generateCode() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("UITransfer")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("start" + methodName);
        //methodBuilder.addJavadoc("goto %S activity", methodName);
        methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        methodBuilder.returns(void.class);
        methodBuilder.addParameter(ClassName.get("android.content", "Context"), "context");
        methodBuilder.addStatement("$T intent= new $T(context,$T.class)", mIntentClassName, mIntentClassName, mAnnotatedClassName);
        methodBuilder.addStatement("$T bundle = new $T()", bundleClassName, bundleClassName);

        List<Element> elements = filterFields(typeElement);
        for (Element field : elements) {
            TypeName fieldClass = ClassName.get(field.asType());
            String fieldName = field.getSimpleName().toString();
            String key = fieldName;
            methodBuilder.addParameter(fieldClass, fieldName);
            methodBuilder.addStatement("bundle.putString($S, $N)", key, fieldName);
        }
        methodBuilder.addStatement("intent.putExtras(bundle)");
        methodBuilder.addStatement("context.startActivity(intent)");
        builder.addMethod(methodBuilder.build());
        return builder.build();
    }

    private List<Element> filterFields(TypeElement element) {
        List<Element> elements = new ArrayList<>();
        for (Element builderField : ElementFilter.fieldsIn(element.getEnclosedElements())) {
//            boolean isIgnored = builderField.getAnnotation(RouterData.class) != null
//                    || builderField.getModifiers().contains(Modifier.STATIC)
//                    || builderField.getModifiers().contains(Modifier.FINAL)
//                    || builderField.getModifiers().contains(Modifier.PRIVATE);
            if (builderField.getAnnotation(TransferField.class) != null) {
                elements.add(builderField);
            }
        }
        return elements;
    }
}
