package com.lany.uitransfer.compiler.processor;

import com.google.auto.service.AutoService;
import com.lany.uitransfer.annotaion.TransferField;
import com.lany.uitransfer.annotaion.TransferTarget;
import com.lany.uitransfer.compiler.utils.Logger;
import com.lany.uitransfer.compiler.utils.TextUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

@AutoService(Processor.class)
public class TransferProcessor extends AbstractProcessor {
    private Logger log;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        log = new Logger(processingEnv.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(TransferTarget.class);
        if (elementSet != null && elementSet.size() > 0) {
            String packageName = "com.lany.uitransfer";
            JavaFile javaFile = JavaFile.builder(packageName, generateCode(elementSet, packageName))
                    .addFileComment("Generated code from UITransfer. Do not modify!")
                    .build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
                //如果有执行到Diagnostic.Kind.ERROR就会出现错误提示
                log.e("错误啊" + e.getMessage());
            }
        }
        Set<? extends Element> fieldElementSet = roundEnv.getElementsAnnotatedWith(TransferField.class);
        if (fieldElementSet != null && fieldElementSet.size() > 0) {
            for (Element item : fieldElementSet) {
                if (item.getKind() == ElementKind.FIELD) {
                    log.i("名称==" + item.getSimpleName());
                }
            }
        }
        return true;
    }

    private TypeSpec generateCode(Set<? extends Element> elementSet, String packageName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("UITransfer")
                .addJavadoc("界面跳转调用此类")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (Element item : elementSet) {
            //判断当前Element是否是类,不用 item instanceof TypeElement的原因是interface也是TypeElement.
            if (item.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) item;
                builder.addMethod(getMethodSpecFromTypeElement(typeElement, packageName));
            }
        }
        return builder.build();
    }

    private MethodSpec getMethodSpecFromTypeElement(TypeElement typeElement, String packageName) {
        String methodName = typeElement.getSimpleName().toString();
        if (methodName.contains("activity")) {
            methodName = methodName.replace("activity", "");
        }
        if (methodName.contains("Activity")) {
            methodName = methodName.replace("Activity", "");
        }
        methodName = "start" + methodName;
        ClassName annotatedClassName = ClassName.get(packageName, typeElement.getSimpleName().toString());
        ClassName intentClassName = ClassName.get("android.content", "Intent");
        ClassName bundleClassName = ClassName.get("android.os", "Bundle");


        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
        methodBuilder.addJavadoc("goto $N activity", methodName);
        methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        methodBuilder.returns(void.class);
        methodBuilder.addParameter(ClassName.get("android.content", "Context"), "context");
        methodBuilder.addStatement("$T intent= new $T(context,$T.class)", intentClassName, intentClassName, annotatedClassName);
        methodBuilder.addStatement("$T bundle = new $T()", bundleClassName, bundleClassName);
        List<Element> elements = filterFields(typeElement);
        for (Element field : elements) {
            TransferField annotation = field.getAnnotation(TransferField.class);
            String key = annotation.value();
            TypeName fieldClass = ClassName.get(field.asType());
            String fieldName = field.getSimpleName().toString();
            if (TextUtils.isEmpty(key)) {//如果key为空，用字段名称
                key = fieldName;
            }
            methodBuilder.addParameter(fieldClass, fieldName);
            methodBuilder.addStatement("bundle.putString($S, $N)", key, fieldName);
        }
        methodBuilder.addStatement("intent.putExtras(bundle)");
        methodBuilder.addStatement("context.startActivity(intent)");

        return methodBuilder.build();
    }

    private List<Element> filterFields(TypeElement element) {
        List<Element> elements = new ArrayList<>();
        for (Element builderField : ElementFilter.fieldsIn(element.getEnclosedElements())) {
//            boolean isIgnored = builderField.getAnnotation(TransferField.class) != null
//                    || builderField.getModifiers().contains(Modifier.STATIC)
//                    || builderField.getModifiers().contains(Modifier.FINAL)
//                    || builderField.getModifiers().contains(Modifier.PRIVATE);
            if (builderField.getAnnotation(TransferField.class) != null) {
                elements.add(builderField);
            }
        }
        return elements;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new HashSet<>();
        supportedAnnotationTypes.add(TransferTarget.class.getCanonicalName());
        supportedAnnotationTypes.add(TransferField.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
