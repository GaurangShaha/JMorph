package com.jmorph.processor.util;

import com.jmorph.annotation.FieldTransformer;
import com.jmorph.annotation.MorphToField;
import com.jmorph.processor.constant.Constant;
import com.jmorph.processor.exception.ValidationException;
import com.jmorph.processor.model.MorphToFieldMetaData;
import com.jmorph.processor.model.ParameterMatchType;
import com.jmorph.processor.model.ParameterMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ValidationUtil {
    private static final HashMap<String, ArrayList<String>> upCastingHashMap = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> downCastingHashMap = new HashMap<>();
    private static final HashMap<String, String> primitiveWrapperClassHashMap = new HashMap<>();

    static {
        upCastingHashMap.put("byte", new ArrayList<>(Arrays.asList("short", "int", "long", "float", "double")));
        upCastingHashMap.put("short", new ArrayList<>(Arrays.asList("int", "long", "float", "double")));
        upCastingHashMap.put("char", new ArrayList<>(Arrays.asList("int", "long", "float", "double")));
        upCastingHashMap.put("int", new ArrayList<>(Arrays.asList("long", "float", "double")));
        upCastingHashMap.put("long", new ArrayList<>(Arrays.asList("float", "double")));
        upCastingHashMap.put("float", new ArrayList<>(Arrays.asList("double")));

        downCastingHashMap.put("double", new ArrayList<>(Arrays.asList("byte", "short", "char", "int", "long", "float")));
        downCastingHashMap.put("float", new ArrayList<>(Arrays.asList("byte", "short", "char", "int", "long")));
        downCastingHashMap.put("long", new ArrayList<>(Arrays.asList("byte", "short", "char", "int")));
        downCastingHashMap.put("int", new ArrayList<>(Arrays.asList("byte", "short", "char")));
        downCastingHashMap.put("char", new ArrayList<>(Arrays.asList("byte", "short")));
        downCastingHashMap.put("short", new ArrayList<>(Arrays.asList("byte", "char")));

        primitiveWrapperClassHashMap.put("double", Double.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("byte", Byte.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("short", Short.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("char", Character.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("int", Integer.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("long", Long.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("float", Float.class.getCanonicalName());
        primitiveWrapperClassHashMap.put("boolean", Boolean.class.getCanonicalName());

    }

    public static boolean isValidElementForMorphToAnnotation(Element type) throws ValidationException {
        if (!(type instanceof TypeElement))
            return false;

        if (type.getKind() == ElementKind.INTERFACE) {
            throw new ValidationException(String.format(Constant.ERROR_INTERFACE_FOUND, type.getSimpleName()));
        }

        if (type.getKind() == ElementKind.ENUM) {
            throw new ValidationException(String.format(Constant.ERROR_ENUM_FOUND, type.getSimpleName()));
        }

        if (type.getKind() == ElementKind.ANNOTATION_TYPE) {
            throw new ValidationException(String.format(Constant.ERROR_ANNOTATION_TYPE_FOUND, type.getSimpleName()));
        }

        if (type.getModifiers().contains(Modifier.PRIVATE)) {
            throw new ValidationException(String.format(Constant.ERROR_PRIVATE_CLASS_FOUND, type.getSimpleName()));
        }

        if (type.getModifiers().contains(Modifier.PROTECTED)) {
            throw new ValidationException(String.format(Constant.ERROR_PROTECTED_CLASS_FOUND, type.getSimpleName()));
        }

        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ValidationException(String.format(Constant.ERROR_ABSTRACT_CLASS_FOUND, type.getSimpleName()));
        }

        if (!ElementUtil.isNoArgConstructorFound(type)) {
            throw new ValidationException(String.format(Constant.ERROR_NO_ARG_CONSTRUCTOR_IS_MISSING, type.getSimpleName()));
        }

        return true;
    }

    public static boolean isValidSetter(ExecutableElement executableElement) {
        return startsWithSetWord(executableElement) && isPublicMethod(executableElement) && hasValidNoOfParameterForSetter(executableElement);
    }

    private static boolean hasValidNoOfParameterForSetter(ExecutableElement executableElement) {
        return ElementUtil.getParameterMetaDataList(executableElement).size() == 1;
    }

    private static boolean isPublicMethod(ExecutableElement executableElement) {
        return ElementUtil.getModifierList(executableElement).contains(Constant.PUBLIC);
    }

    private static boolean startsWithSetWord(ExecutableElement executableElement) {
        return ElementUtil.getMethodName(executableElement).startsWith(Constant.SET);
    }

    public static boolean isValidGetter(ExecutableElement executableElement) {
        return (startsWithGetWord(executableElement) || startsWithIsWord(executableElement)) && isPublicMethod(executableElement) && hasValidNoOfParameter(executableElement);
    }

    private static boolean hasValidNoOfParameter(ExecutableElement executableElement) {
        return ElementUtil.getParameterMetaDataList(executableElement).size() == 0;
    }

    private static boolean startsWithIsWord(ExecutableElement executableElement) {
        return ElementUtil.getMethodName(executableElement).startsWith(Constant.IS);
    }

    private static boolean startsWithGetWord(ExecutableElement executableElement) {
        return ElementUtil.getMethodName(executableElement).startsWith(Constant.GET);
    }

    public static boolean isFieldAnnotatedWithMorphTo(VariableElement variableElement) {
        return ElementUtil.getAnnotationMirror(variableElement, MorphToField.class) != null;
    }

    public static boolean expectedMethodPresent(String actualName, String expectedName) {
        return actualName.equals(expectedName);
    }

    public static ParameterMatchType hasCompatibleParamType(ExecutableElement getterExecutableElement, ExecutableElement setterExecutableElement) {
        return hasCompatibleParamType(ElementUtil.getReturnTypeMetaData(getterExecutableElement), ElementUtil.getParameterMetaDataList(setterExecutableElement).get(0));
    }

    public static boolean startsWithIsWord(String methodName) {
        return methodName.startsWith(Constant.IS);
    }

    public static boolean isMorphToFieldAnnotationPresent(ExecutableElement sourceClassEnclosedElement, MorphToFieldMetaData morphToFieldMetaData) {
        return ElementUtil.getVariableNameFromGetter(sourceClassEnclosedElement).equalsIgnoreCase(morphToFieldMetaData.getFieldMetaData().getFieldName());
    }

    public static boolean isFieldAnnotatedWithFieldTransformer(VariableElement variableElement) {
        return ElementUtil.getAnnotationMirror(variableElement, FieldTransformer.class) != null;
    }

    public static boolean isValidValueForFieldTransformerAnnotation(TypeElement typeElement) throws ValidationException {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            throw new ValidationException(String.format(Constant.ERROR_INTERFACE_USED_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (typeElement.getKind() == ElementKind.ENUM) {
            throw new ValidationException(String.format(Constant.ERROR_ENUM_USED_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
            throw new ValidationException(String.format(Constant.ERROR_ANNOTATION_TYPE_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
            throw new ValidationException(String.format(Constant.ERROR_PRIVATE_CLASS_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (typeElement.getModifiers().contains(Modifier.PROTECTED)) {
            throw new ValidationException(String.format(Constant.ERROR_PROTECTED_CLASS_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ValidationException(String.format(Constant.ERROR_ABSTRACT_CLASS_AS_FIELD_TRANSFORMER, typeElement.getSimpleName()));
        }

        if (!typeElement.getInterfaces().toString().startsWith(Constant.TRANSFORMER_CONTRACT_QUALIFIED_NAME)) {
            throw new ValidationException(String.format(Constant.ERROR_TRANSFORMER_CLASS_NOT_IMPLEMENTED_FIELD_TRANSFORMER_CONTRACT, typeElement.getSimpleName()));
        }

        return true;
    }

    public static ParameterMatchType hasCompatibleParamType(ParameterMetaData getterParameterMetaData, ParameterMetaData setterParameterMetaData) {
        if (getterParameterMetaData.isPrimitive() && setterParameterMetaData.isPrimitive()) {
            if (upCastingHashMap.get(getterParameterMetaData.getParameterType()) != null && upCastingHashMap.get(getterParameterMetaData.getParameterType()).contains(setterParameterMetaData.getParameterType()))
                return ParameterMatchType.UPCAST_NEEDED;
            else if (downCastingHashMap.get(getterParameterMetaData.getParameterType()) != null && downCastingHashMap.get(getterParameterMetaData.getParameterType()).contains(setterParameterMetaData.getParameterType()))
                return ParameterMatchType.DOWNCAST_NEEDED;
        }
        if (setterParameterMetaData.getParameterType().equals(getterParameterMetaData.getParameterType()))
            return ParameterMatchType.SAME_CLASS;

        if ((getterParameterMetaData.isPrimitive() && setterParameterMetaData.getParameterType().equals(primitiveWrapperClassHashMap.get(getterParameterMetaData.getParameterType()))) || (setterParameterMetaData.isPrimitive() && getterParameterMetaData.getParameterType().equals(primitiveWrapperClassHashMap.get(setterParameterMetaData.getParameterType()))))
            return ParameterMatchType.SAME_CLASS;

        return ParameterMatchType.NO_MATCH;
    }
}

