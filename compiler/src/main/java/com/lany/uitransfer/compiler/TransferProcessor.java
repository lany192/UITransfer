package com.lany.uitransfer.compiler;

import com.google.auto.service.AutoService;
import com.lany.uitransfer.annotaion.TransferField;
import com.lany.uitransfer.annotaion.TransferTarget;
import com.lany.uitransfer.compiler.rules.AbstractClassRejectRule;
import com.lany.uitransfer.compiler.rules.ConstructorRejectRule;
import com.lany.uitransfer.compiler.rules.Rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class TransferProcessor extends AbstractProcessor {
    private List<Rule> mRules;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mRules = new ArrayList<>();
        mRules.add(new AbstractClassRejectRule());
        mRules.add(new ConstructorRejectRule());
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(TransferTarget.class)) {
            //判断当前Element是否是类,不用 annotatedElement instanceof TypeElement的原因是interface也是TypeElement.
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                TypeElement annotatedClass = (TypeElement) annotatedElement;
                String packageName = processingEnv.getElementUtils().getPackageOf(annotatedClass).getQualifiedName().toString();
                CodeGenerator codeGenerator = new CodeGenerator(packageName, annotatedClass);
                try {
                    codeGenerator.generateJavaFile(processingEnv.getFiler());
                } catch (IOException e) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
            }
        }
        return true;
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
