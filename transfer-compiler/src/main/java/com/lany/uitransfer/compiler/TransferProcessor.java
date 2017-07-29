package com.lany.uitransfer.compiler;


import com.google.auto.service.AutoService;
import com.lany.uitransfer.annotaion.RequestParam;
import com.lany.uitransfer.annotaion.TransferInjector;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@AutoService(Processor.class)
public class TransferProcessor extends AbstractProcessor {
    private static final String PACKAGE_NAME = "com.github.lany192";
    private Filer filer;
    private Map<String, TransferEntity> map;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        map = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(RequestParam.class)) {
            if (!(element instanceof VariableElement)) {
                return false;
            }
            getEachVariableElement(element);
        }
        try {
            createTransfer();
            createInjectors();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getEachVariableElement(Element element) {
        VariableElement variableElement = (VariableElement) element;
        String packageName = processingEnv.getElementUtils().getPackageOf(variableElement).getQualifiedName().toString();
        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = variableElement.asType().toString();
        String className = variableElement.getEnclosingElement().getSimpleName().toString();
        RequestParam annotation = element.getAnnotation(RequestParam.class);
        String fieldValue = annotation.value().isEmpty() ? fieldName : annotation.value();
        String canonicalClassName = packageName + "." + className;
        TransferEntity TransferEntity;
        if (map.get(canonicalClassName) == null) {
            TransferEntity = new TransferEntity();
            TransferEntity.packageName = packageName;
            TransferEntity.className = className;
            map.put(canonicalClassName, TransferEntity);
        } else {
            TransferEntity = map.get(canonicalClassName);
        }
        if (fieldType.contains("<") && fieldType.contains(">")) {
            int startIndex = fieldType.indexOf("<");
            int endIndex = fieldType.indexOf(">");
            String class1 = fieldType.substring(0, startIndex);
            String class2 = fieldType.substring(startIndex + 1, endIndex);
            FieldEntity entity = new FieldEntity();
            entity.fieldName = fieldName;
            entity.fieldValue = fieldValue;
            entity.fieldType = class1;
            entity.fieldParam = class2;
            TransferEntity.fields.add(entity);
        } else {
            String[] typeArray = {
                    "boolean", "boolean[]",
                    "byte", "byte[]",
                    "short", "short[]",
                    "int", "int[]",
                    "long", "long[]",
                    "double", "double[]",
                    "float", "float[]",
                    "char", "char[]",
                    "java.lang.CharSequence", "java.lang.CharSequence[]",
                    "java.lang.String", "java.lang.String[]",
                    "android.os.Bundle"
            };
            if (Arrays.asList(typeArray).contains(fieldType)) {
                TransferEntity.fields.add(new FieldEntity(fieldName, fieldType, fieldValue));
            } else {
                String type = fieldType.contains("[]") ? "android.os.Parcelable[]" : "android.os.Parcelable";
                FieldEntity entity = new FieldEntity(fieldName, type, fieldValue);
                entity.originalType = fieldType.replace("[]", "");
                TransferEntity.fields.add(entity);
            }
        }
    }

    private void createTransfer() throws Exception {
        List<TypeSpec> targetActivitiesClassList = new LinkedList<>();
        List<MethodSpec> goToActivitiesMethodList = new LinkedList<>();
        for (Map.Entry<String, TransferEntity> entry : map.entrySet()) {
            String className = entry.getValue().className;
            String fullClassName = entry.getKey();
            List<MethodSpec> targetActivitiesMethodList = new LinkedList<>();
            for (FieldEntity field : entry.getValue().fields) {
                String methodName = "set" + field.fieldValue.substring(0, 1).toUpperCase() + field.fieldValue.substring(1, field.fieldValue.length());
                String paramName = "";
                if (field.fieldParam.length() > 0) {
                    String paramSimpleName = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                    switch (paramSimpleName) {
                        case "Integer":
                            paramName = "IntegerArrayList";
                            break;
                        case "String":
                            paramName = "StringArrayList";
                            break;
                        case "CharSequence":
                            paramName = "CharSequenceArrayList";
                            break;
                        default:
                            paramName = "ParcelableArrayList";
                            break;
                    }
                }
                MethodSpec method = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(getFieldType(field), field.fieldValue + "Extra")
                        .returns(ClassName.get(PACKAGE_NAME, "Transfer", "To" + className))
                        .addStatement("intent.put$LExtra($S, $L)", paramName, field.fieldValue, field.fieldValue + "Extra")
                        .addStatement("return this")
                        .build();
                targetActivitiesMethodList.add(method);
            }

            //Transfer里GoToXXXActivity类的go()
            MethodSpec start = MethodSpec.methodBuilder("start")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("Transfer.start($T.class)", ClassName.bestGuess(fullClassName))
                    .build();

            MethodSpec startForResult = MethodSpec.methodBuilder("start")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "requestCode")
                    .addStatement("Transfer.start($T.class, requestCode)", ClassName.bestGuess(fullClassName))
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build();

            TypeSpec type = TypeSpec.classBuilder("To" + className)
                    .addModifiers(Modifier.FINAL, Modifier.STATIC, Modifier.PUBLIC)
                    .addMethod(constructor)
                    .addMethods(targetActivitiesMethodList)
                    .addMethod(start)
                    .addMethod(startForResult)
                    .build();

            MethodSpec method = MethodSpec.methodBuilder("to" + className)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("@see 跳转到$T\n", ClassName.bestGuess(fullClassName))
                    .returns(ClassName.get(PACKAGE_NAME, "Transfer", "To" + className))
                    .addStatement("return new $T()", ClassName.get(PACKAGE_NAME, "Transfer", "To" + className))
                    .build();

            targetActivitiesClassList.add(type);
            goToActivitiesMethodList.add(method);
        }
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        MethodSpec addFlags = MethodSpec.methodBuilder("addFlags")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "flags")
                .returns(ClassName.get(PACKAGE_NAME, "Transfer", "ToActivity"))
                .addStatement("intent.addFlags(flags)")
                .addStatement("return this")
                .build();
        MethodSpec setAnim = MethodSpec.methodBuilder("setAnim")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "enterAnimId")
                .addParameter(TypeName.INT, "exitAnimId")
                .returns(ClassName.get(PACKAGE_NAME, "Transfer", "ToActivity"))
                .addStatement("enterAnim = enterAnimId")
                .addStatement("exitAnim = exitAnimId")
                .addStatement("return this")
                .build();
        TypeSpec TransferToActivity = TypeSpec.classBuilder("ToActivity")
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(constructor)
                .addMethod(setAnim)
                .addMethod(addFlags)
                .addMethods(goToActivitiesMethodList)
                .build();
        MethodSpec inject = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Object.class, "activity")
                .addStatement("inject(activity, null)")
                .build();
        MethodSpec inject2 = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Object.class, "activity")
                .addParameter(Object.class, "intent")
                .addCode("try {\n" +
                        "  String injectorName = activity.getClass().getCanonicalName() + \"_Transfer\";\n" +
                        "  (($T) Class.forName(injectorName).newInstance()).inject(activity, intent);\n" +
                        "} catch (Exception e) {\n" +
                        "  e.printStackTrace();\n" +
                        "}\n", TransferInjector.class)
                .build();
        MethodSpec from = MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.bestGuess("android.content.Context"), "ctx")
                .returns(ClassName.get(PACKAGE_NAME, "Transfer", "ToActivity"))
                .addStatement("context = ctx")
                .addStatement("intent = new Intent()")
                .addStatement("return new $T()", ClassName.get(PACKAGE_NAME, "Transfer", "ToActivity"))
                .build();
        MethodSpec go = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addStatement("intent.setClass(context, clazz)")
                .addStatement("context.startActivity(intent)")
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build();
        MethodSpec goForResult = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addParameter(int.class, "requestCode")
                .addStatement("intent.setClass(context, clazz)")
                .addCode("if (!(context instanceof $T)) {\n" +
                        "  throw new $T(\"非Activity的Context，不能startActivityForResult\");\n" +
                        "} else {\n" +
                        "  ((Activity) context).startActivityForResult(intent, requestCode);\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("java.lang.IllegalArgumentException"))
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build();
        MethodSpec reset = MethodSpec.methodBuilder("reset")
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.STATIC)
                .addStatement("intent = null")
                .addStatement("context = null")
                .addStatement("enterAnim = -1")
                .addStatement("exitAnim = -1")
                .build();
        MethodSpec setTransition = MethodSpec.methodBuilder("setTransition")
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.STATIC)
                .addCode("if(enterAnim < 0 || exitAnim < 0){\n" +
                        "  return;\n" +
                        "}\n" +
                        "if (!(context instanceof $T)) {\n" +
                        "  throw new $T(\"非Activity的Context，不能overridePendingTransition\");\n" +
                        "} else {\n" +
                        "  ((Activity) context).overridePendingTransition(enterAnim, exitAnim);\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("java.lang.IllegalArgumentException"))
                .build();
        FieldSpec enterAnim = FieldSpec.builder(TypeName.INT, "enterAnim", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("-1")
                .build();
        FieldSpec exitAnim = FieldSpec.builder(TypeName.INT, "exitAnim", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("-1")
                .build();
        TypeSpec Transfer = TypeSpec.classBuilder("Transfer")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(ClassName.bestGuess("android.content.Context"), "context", Modifier.PRIVATE, Modifier.STATIC)
                .addField(ClassName.bestGuess("android.content.Intent"), "intent", Modifier.PRIVATE, Modifier.STATIC)
                .addField(enterAnim)
                .addField(exitAnim)
                .addMethod(constructor)
                .addMethod(inject)
                .addMethod(inject2)
                .addMethod(from)
                .addMethod(go)
                .addMethod(goForResult)
                .addMethod(setTransition)
                .addMethod(reset)
                .addType(TransferToActivity)
                .addTypes(targetActivitiesClassList)
                .build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, Transfer).build();
        javaFile.writeTo(filer);
    }

    private void createInjectors() throws Exception {
        for (Map.Entry<String, TransferEntity> entry : map.entrySet()) {
            String fullClassName = entry.getKey();
            String packageName = entry.getValue().packageName;
            String className = entry.getValue().className;
            MethodSpec.Builder builder = MethodSpec.methodBuilder("inject");
            builder.addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(ClassName.bestGuess(fullClassName), "a")
                    .addParameter(Object.class, "i")
                    .addStatement("$T intent = i == null ? a.getIntent() : (Intent) i", ClassName.bestGuess("android.content.Intent"));
            for (FieldEntity field : entry.getValue().fields) {
                getExtras(builder, field);
            }
            TypeSpec typeSpec = TypeSpec.classBuilder(className + "_Transfer")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.bestGuess(TransferInjector.class.getCanonicalName()), ClassName.bestGuess(fullClassName)))
                    .addMethod(builder.build())
                    .build();
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
            javaFile.writeTo(filer);
        }
    }

    private void getExtras(MethodSpec.Builder builder, FieldEntity field) {
        builder.addCode("if (intent.hasExtra($S)) {\n", field.fieldValue);
        String[] typeArray = {"boolean", "byte", "short", "int", "long", "double", "float", "char"};
        if (Arrays.asList(typeArray).contains(field.fieldType)) {
            String statement = "  a.%s = intent.get%sExtra(\"%s\", %s)";
            String defaultValue = "";
            switch (field.fieldType) {
                case "int":
                case "long":
                case "double":
                case "float":
                    defaultValue = "0";
                    break;
                case "byte":
                    defaultValue = "(byte) 0";
                    break;
                case "short":
                    defaultValue = "(short) 0";
                    break;
                case "boolean":
                    defaultValue = "false";
                    break;
                case "char":
                    defaultValue = "'\0'";
                    break;
            }
            String extraType = field.fieldType.toUpperCase().substring(0, 1) + field.fieldType.substring(1, field.fieldType.length());
            builder.addStatement(String.format(statement, field.fieldName, extraType, field.fieldValue, defaultValue));
        } else {
            if (field.fieldType.contains("[]")) {
                String extraType = field.fieldType.replace("[]", "Array");
                String paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                if (Arrays.asList(typeArray).contains(extraType.replace("Array", ""))) {
                    extraType = extraType.substring(0, 1).toUpperCase() + extraType.substring(1, extraType.length());
                } else {
                    String type = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length());
                    extraType = type.substring(0, 1).toUpperCase() + type.substring(1, type.length()).replace("[]", "Array");
                }
                if (extraType.contentEquals("ParcelableArray")) {
                    ClassName originalTypeName = ClassName.bestGuess(field.originalType);
                    builder.addStatement("  $T[] $LArray = intent.getParcelableArrayExtra($S)", ClassName.bestGuess("android.os.Parcelable"), field.fieldValue, field.fieldValue);
                    builder.addStatement("  $T[] $LTempArray = new $T[$LArray.length]", originalTypeName, field.fieldValue, originalTypeName, field.fieldValue);
                    builder.beginControlFlow("  for (int n = 0; n < $LArray.length; n++)", field.fieldValue);
                    builder.addStatement("  $LTempArray[n] = ($T) $LArray[n]", field.fieldValue, originalTypeName, field.fieldValue);
                    builder.addCode(" ");
                    builder.endControlFlow();
                    builder.addStatement("  a.$L = $LTempArray", field.fieldName, field.fieldValue);
                } else {
                    builder.addStatement("  a.$L = intent.get$LExtra($S)", field.fieldName, paramType + extraType, field.fieldValue);
                }
            } else {
                String[] params = {"Integer", "String", "CharSequence", ""};
                String extraType = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length());
                String paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                if (!Arrays.asList(params).contains(paramType)) {
                    paramType = "Parcelable";
                }
                builder.addStatement("  a.$L = intent.get$LExtra($S)", field.fieldName, paramType + extraType, field.fieldValue);
            }
        }
        builder.addCode("}\n");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(RequestParam.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private TypeName getFieldType(FieldEntity field) {
        TypeName typeName;
        switch (field.fieldType) {
            case "boolean":
                typeName = TypeName.BOOLEAN;
                break;
            case "boolean[]":
                typeName = ArrayTypeName.of(TypeName.BOOLEAN);
                break;
            case "byte":
                typeName = TypeName.BYTE;
                break;
            case "byte[]":
                typeName = ArrayTypeName.of(TypeName.BYTE);
                break;
            case "short":
                typeName = TypeName.SHORT;
                break;
            case "short[]":
                typeName = ArrayTypeName.of(TypeName.SHORT);
                break;
            case "int":
                typeName = TypeName.INT;
                break;
            case "int[]":
                typeName = ArrayTypeName.of(TypeName.INT);
                break;
            case "long":
                typeName = TypeName.LONG;
                break;
            case "long[]":
                typeName = ArrayTypeName.of(TypeName.LONG);
                break;
            case "char":
                typeName = TypeName.CHAR;
                break;
            case "char[]":
                typeName = ArrayTypeName.of(TypeName.CHAR);
                break;
            case "float":
                typeName = TypeName.FLOAT;
                break;
            case "float[]":
                typeName = ArrayTypeName.of(TypeName.FLOAT);
                break;
            case "double":
                typeName = TypeName.DOUBLE;
                break;
            case "double[]":
                typeName = ArrayTypeName.of(TypeName.DOUBLE);
                break;
            case "java.lang.CharSequence":
                typeName = TypeName.get(CharSequence.class);
                break;
            case "java.lang.CharSequence[]":
                typeName = ArrayTypeName.of(CharSequence.class);
                break;
            case "java.lang.String":
                typeName = TypeName.get(String.class);
                break;
            case "java.lang.String[]":
                typeName = ArrayTypeName.of(String.class);
                break;
            case "android.os.Parcelable":
                typeName = ClassName.bestGuess("android.os.Parcelable");
                break;
            case "android.os.Parcelable[]":
                typeName = ArrayTypeName.of(ClassName.bestGuess(field.originalType));
                break;
            case "android.os.Bundle":
                typeName = ClassName.bestGuess("android.os.Bundle");
                break;
            default:
                if (field.fieldParam.length() > 0) {
                    typeName = ParameterizedTypeName.get(ClassName.bestGuess(field.fieldType), ClassName.bestGuess(field.fieldParam));
                } else {
                    typeName = ClassName.bestGuess(field.fieldType);
                }
                break;
        }
        return typeName;
    }
}
