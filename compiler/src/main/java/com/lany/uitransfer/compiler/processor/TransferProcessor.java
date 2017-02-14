package com.lany.uitransfer.compiler.processor;

import com.google.auto.service.AutoService;
import com.lany.uitransfer.annotaion.TransferField;
import com.lany.uitransfer.annotaion.TransferTarget;
import com.lany.uitransfer.compiler.utils.ClassNameUtils;
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
                log.e(e.getMessage());
            }
        }
        Set<? extends Element> fieldElementSet = roundEnv.getElementsAnnotatedWith(TransferField.class);
        if (fieldElementSet != null && fieldElementSet.size() > 0) {
            for (Element fieldElement : fieldElementSet) {
                if (fieldElement.getKind() == ElementKind.FIELD) {
                    log.i("名称==" + fieldElement.getSimpleName());
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
        final String simpleName = typeElement.getSimpleName().toString();
        String methodName = "start" + simpleName;

        ClassName annotatedClassName = ClassName.get(packageName, simpleName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
        methodBuilder.addJavadoc("goto $N activity", methodName);
        methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        methodBuilder.returns(void.class);
        methodBuilder.addParameter(ClassNameUtils.CONTEXT, "context");
        methodBuilder.addStatement("$T intent= new $T(context,$T.class)", ClassNameUtils.INTENT, ClassNameUtils.INTENT, annotatedClassName);
        methodBuilder.addStatement("$T bundle = new $T()", ClassNameUtils.BUNDLE, ClassNameUtils.BUNDLE);
        List<Element> elements = filterFields(typeElement);
        for (Element fieldElement : elements) {
            TransferField annotation = fieldElement.getAnnotation(TransferField.class);
            String key = annotation.value();
            TypeName fieldTypeName = ClassName.get(fieldElement.asType());
            String fieldName = fieldElement.getSimpleName().toString();
            if (TextUtils.isEmpty(key)) {//如果key为空，用字段名称
                key = fieldName.toUpperCase();
            }
            methodBuilder.addParameter(fieldTypeName, fieldName);
            if (TypeName.CHAR == fieldTypeName) {
                methodBuilder.addStatement("bundle.putChar($S, $N)", key, fieldName);
            } else if (TypeName.BOOLEAN == fieldTypeName) {
                methodBuilder.addStatement("bundle.putBoolean($S, $N)", key, fieldName);
            } else if (TypeName.DOUBLE == fieldTypeName) {
                methodBuilder.addStatement("bundle.putDouble($S, $N)", key, fieldName);
            } else if (TypeName.FLOAT == fieldTypeName) {
                methodBuilder.addStatement("bundle.putFloat($S, $N)", key, fieldName);
            } else if (TypeName.INT == fieldTypeName) {
                methodBuilder.addStatement("bundle.putInt($S, $N)", key, fieldName);
            } else if (TypeName.LONG == fieldTypeName) {
                methodBuilder.addStatement("bundle.putLong($S, $N)", key, fieldName);
            } else {
                methodBuilder.addStatement("bundle.putString($S, $N)", key, fieldName);
            }
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
