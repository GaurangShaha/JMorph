package com.jmorph.processor.util;

import com.jmorph.annotation.FieldTransformer;
import com.jmorph.annotation.MorphToField;
import com.jmorph.processor.constant.Constant;
import com.jmorph.processor.exception.ValidationException;
import com.jmorph.processor.model.FieldMetaData;
import com.jmorph.processor.model.FieldTransformerMetaData;
import com.jmorph.processor.model.MethodMetaData;
import com.jmorph.processor.model.MorphToFieldMetaData;
import com.jmorph.processor.model.MorpherMetaData;
import com.jmorph.processor.model.ParameterMetaData;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

public class ElementUtil {
    public static AnnotationMirror getAnnotationMirror(Element element, Class<?> annotationType) {
        AnnotationMirror annotationMirror = null;

        String annotationClassName = annotationType.getName();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationClassName)) {
                annotationMirror = mirror;
                break;
            }
        }
        return annotationMirror;
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror annotation, String annotationParameterName) {
        if (annotation != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
                if (annotationParameterName == null)
                    return entry.getValue();
                else if (entry.getKey().getSimpleName().toString().equals(annotationParameterName)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static TypeElement getTypeElement(AnnotationValue value, Types typeUtils) {
        if (value != null) {
            TypeMirror typeMirror = (TypeMirror) value.getValue();
            return (TypeElement) typeUtils.asElement(typeMirror);
        }
        return null;
    }


    public static ArrayList<MethodMetaData> extractMethodMetaData(Element element) {
        ArrayList<MethodMetaData> methodMetaDataList = new ArrayList<>();
        for (ExecutableElement enclosedElement : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                MethodMetaData methodMetaData = new MethodMetaData();

                ArrayList<String> modifierList = new ArrayList<>();
                for (Modifier modifier : enclosedElement.getModifiers()) {
                    modifierList.add(modifier.toString());
                }
                methodMetaData.setModifierList(modifierList);

                ParameterMetaData returnTypeMetaData = new ParameterMetaData();
                returnTypeMetaData.setParameterType(enclosedElement.getReturnType().toString());
                returnTypeMetaData.setPrimitive(enclosedElement.getReturnType().getKind().isPrimitive());
                methodMetaData.setReturnTypeMetaData(returnTypeMetaData);

                methodMetaData.setMethodName(enclosedElement.getSimpleName().toString());

                ArrayList<ParameterMetaData> parameterList = new ArrayList<>();
                for (VariableElement variableElement : enclosedElement.getParameters()) {
                    ParameterMetaData parameterMetaData = new ParameterMetaData();
                    parameterMetaData.setParameterType(variableElement.asType().toString());
                    parameterMetaData.setParameterName(variableElement.getSimpleName().toString());
                    parameterList.add(parameterMetaData);
                }
                methodMetaData.setParameterMetaDataList(parameterList);

                methodMetaDataList.add(methodMetaData);
            }
        }
        return methodMetaDataList;
    }

    public static FieldMetaData extractFieldMetaData(Element fieldElement) {
        FieldMetaData fieldMetaData = new FieldMetaData();
        fieldMetaData.setPrimitive(fieldElement.asType().getKind().isPrimitive());
        fieldMetaData.setFieldType(fieldElement.asType().toString());
        fieldMetaData.setFieldName(fieldElement.getSimpleName().toString());
        return fieldMetaData;
    }


    public static MorpherMetaData generateGetterSetterMapping(TypeElement sourceClassElement, TypeElement targetClassElement, Types typeUtil, MorpherMetaData morpherMetaData) throws ValidationException {
        ArrayList<MorphToFieldMetaData> morphToFieldMetaDataList = null;
        ArrayList<FieldTransformerMetaData> fieldTransformerMetaDataList = null;
        ArrayList<String> getterSetterMappingListForMorph = new ArrayList<>();
        ArrayList<String> getterSetterMappingListForReverseMorph = new ArrayList<>();

        for (ExecutableElement sourceClassEnclosedElement : ElementFilter.methodsIn(sourceClassElement.getEnclosedElements())) {
            if (ValidationUtil.isValidGetter(sourceClassEnclosedElement)) {
                for (ExecutableElement targetClassEnclosedElement : ElementFilter.methodsIn(targetClassElement.getEnclosedElements())) {
                    if (ValidationUtil.isValidSetter(targetClassEnclosedElement)) {
                        if (ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), getPossibleSetterName(sourceClassEnclosedElement))) {
                            String getterSetterMapping = generateMappingAfterApplyingFieldTransformerForMorph(fieldTransformerMetaDataList, sourceClassElement, typeUtil, sourceClassEnclosedElement, targetClassEnclosedElement);
                            if (getterSetterMapping != null) {
                                getterSetterMappingListForMorph.add(getterSetterMapping);
                                break;
                            } else {
                                switch (ValidationUtil.hasCompatibleParamType(sourceClassEnclosedElement, targetClassEnclosedElement)) {
                                    case SAME_CLASS:
                                    case UPCAST_NEEDED:
                                        getterSetterMapping = String.format(Constant.STATEMENT_FOR_MORPH_METHOD, getMethodName(targetClassEnclosedElement), getMethodName(sourceClassEnclosedElement));
                                        break;
                                    case DOWNCAST_NEEDED:
                                        getterSetterMapping = String.format(Constant.STATEMENT_FOR_MORPH_METHOD_WITH_DOWNCAST, getMethodName(targetClassEnclosedElement), getParameterMetaDataList(targetClassEnclosedElement).get(0).getParameterType(), getMethodName(sourceClassEnclosedElement));
                                        break;
                                }
                                if (getterSetterMapping != null) {
                                    getterSetterMappingListForMorph.add(getterSetterMapping);
                                    break;
                                }
                            }

                        } else {
                            if (morphToFieldMetaDataList == null)
                                morphToFieldMetaDataList = ElementUtil.newExtractMorphToFieldMetaData(sourceClassElement);

                            String getterSetterMapping = null;
                            for (MorphToFieldMetaData morphToFieldMetaData : morphToFieldMetaDataList) {
                                String expectedSetterName = Constant.SET + CapitalizeFirstLetter(morphToFieldMetaData.getMorphedFieldName());
                                if (ValidationUtil.isMorphToFieldAnnotationPresent(sourceClassEnclosedElement, morphToFieldMetaData) && ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), expectedSetterName)) {
                                    switch (ValidationUtil.hasCompatibleParamType(sourceClassEnclosedElement, targetClassEnclosedElement)) {
                                        case SAME_CLASS:
                                        case UPCAST_NEEDED:
                                            getterSetterMapping = String.format(Constant.STATEMENT_FOR_MORPH_METHOD, expectedSetterName, getMethodName(sourceClassEnclosedElement));
                                            break;
                                        case DOWNCAST_NEEDED:
                                            getterSetterMapping = String.format(Constant.STATEMENT_FOR_MORPH_METHOD_WITH_DOWNCAST, expectedSetterName, getParameterMetaDataList(targetClassEnclosedElement).get(0).getParameterType(), getMethodName(sourceClassEnclosedElement));
                                            break;
                                        case NO_MATCH:
                                            getterSetterMapping = generateMappingAfterApplyingFieldTransformerForMorph(fieldTransformerMetaDataList, sourceClassElement, typeUtil, sourceClassEnclosedElement, targetClassEnclosedElement);
                                    }
                                    if (getterSetterMapping != null) {
                                        break;
                                    }
                                }
                            }

                            if (getterSetterMapping != null) {
                                getterSetterMappingListForMorph.add(getterSetterMapping);
                                break;
                            }
                        }

                    }
                }
            } else if (ValidationUtil.isValidSetter(sourceClassEnclosedElement)) {
                for (ExecutableElement targetClassEnclosedElement : ElementFilter.methodsIn(targetClassElement.getEnclosedElements())) {
                    if (ValidationUtil.isValidGetter(targetClassEnclosedElement)) {
                        String[] possibleGetterName = getPossibleGetterName(sourceClassEnclosedElement);
                        if (ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), possibleGetterName[0]) || ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), possibleGetterName[1])) {
                            String getterSetterMapping = generateMappingAfterApplyingFieldTransformerForReverseMorph(fieldTransformerMetaDataList, sourceClassElement, typeUtil, sourceClassEnclosedElement, targetClassEnclosedElement);
                            if (getterSetterMapping != null) {
                                getterSetterMappingListForReverseMorph.add(getterSetterMapping);
                                break;
                            } else {
                                switch (ValidationUtil.hasCompatibleParamType(targetClassEnclosedElement, sourceClassEnclosedElement)) {
                                    case SAME_CLASS:
                                    case UPCAST_NEEDED:
                                        getterSetterMapping = String.format(Constant.STATEMENT_FOR_REVERSE_MORPH_METHOD, getMethodName(sourceClassEnclosedElement), getMethodName(targetClassEnclosedElement));
                                        break;
                                    case DOWNCAST_NEEDED:
                                        getterSetterMapping = String.format(Constant.STATEMENT_FOR_REVERSE_MORPH_METHOD_WITH_DOWNCAST, getMethodName(sourceClassEnclosedElement), getParameterMetaDataList(sourceClassEnclosedElement).get(0).getParameterType(), getMethodName(targetClassEnclosedElement));
                                        break;
                                }
                            }

                            if (getterSetterMapping != null) {
                                getterSetterMappingListForReverseMorph.add(getterSetterMapping);
                                break;
                            }

                        } else {
                            if (morphToFieldMetaDataList == null)
                                morphToFieldMetaDataList = ElementUtil.newExtractMorphToFieldMetaData(sourceClassElement);


                            String getterSetterMapping = null;
                            for (MorphToFieldMetaData morphToFieldMetaData : morphToFieldMetaDataList) {
                                String[] expectedGetterName = new String[]{Constant.GET + CapitalizeFirstLetter(morphToFieldMetaData.getMorphedFieldName()), Constant.IS + CapitalizeFirstLetter(morphToFieldMetaData.getMorphedFieldName())};
                                boolean startsWithGet = ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), expectedGetterName[0]);
                                boolean startsWithIs = ValidationUtil.expectedMethodPresent(getMethodName(targetClassEnclosedElement), expectedGetterName[1]);
                                if (ValidationUtil.isMorphToFieldAnnotationPresent(sourceClassEnclosedElement, morphToFieldMetaData) && (startsWithGet || startsWithIs)) {
                                    switch (ValidationUtil.hasCompatibleParamType(targetClassEnclosedElement, sourceClassEnclosedElement)) {
                                        case SAME_CLASS:
                                        case UPCAST_NEEDED:
                                            getterSetterMapping = String.format(Constant.STATEMENT_FOR_REVERSE_MORPH_METHOD, getMethodName(sourceClassEnclosedElement), expectedGetterName[startsWithGet ? 0 : 1]);
                                            break;
                                        case DOWNCAST_NEEDED:
                                            getterSetterMapping = String.format(Constant.STATEMENT_FOR_REVERSE_MORPH_METHOD_WITH_DOWNCAST, getMethodName(sourceClassEnclosedElement), getParameterMetaDataList(sourceClassEnclosedElement).get(0).getParameterType(), expectedGetterName[startsWithGet ? 0 : 1]);
                                            break;
                                        case NO_MATCH:
                                            getterSetterMapping = generateMappingAfterApplyingFieldTransformerForReverseMorph(fieldTransformerMetaDataList, sourceClassElement, typeUtil, sourceClassEnclosedElement, targetClassEnclosedElement);
                                            break;
                                    }

                                    if (getterSetterMapping != null) {
                                        break;
                                    }
                                }
                            }

                            if (getterSetterMapping != null) {
                                getterSetterMappingListForReverseMorph.add(getterSetterMapping);
                                break;
                            }
                        }
                    }
                }

            }
        }
        morpherMetaData.setMorphMethodMetaData(getterSetterMappingListForMorph);
        morpherMetaData.setReverseMorphMethodMetaData(getterSetterMappingListForReverseMorph);
        return morpherMetaData;
    }

    private static String[] getPossibleGetterName(ExecutableElement sourceClassEnclosedElement) {
        return new String[]{Constant.IS + getMethodName(sourceClassEnclosedElement).substring(3), Constant.GET + getMethodName(sourceClassEnclosedElement).substring(3)};
    }

    private static String generateMappingAfterApplyingFieldTransformerForMorph(ArrayList<FieldTransformerMetaData> fieldTransformerMetaDataList, Element sourceClassElement, Types typeUtil, ExecutableElement sourceClassEnclosedElement, ExecutableElement targetClassEnclosedElement) throws ValidationException {
        if (fieldTransformerMetaDataList == null)
            fieldTransformerMetaDataList = ElementUtil.extractFieldTransformerMetaData(sourceClassElement, typeUtil);

        for (FieldTransformerMetaData fieldTransformerMetaData : fieldTransformerMetaDataList) {

            if (fieldTransformerMetaData.getFieldMetaData().getFieldName().equalsIgnoreCase(getVariableNameFromGetter(sourceClassEnclosedElement))) {
                for (MethodMetaData methodMetaData : fieldTransformerMetaData.getTransformerMethodMetaData()) {
                    if (methodMetaData.getMethodName().equals(Constant.TRANSFORM)) {
                        switch (ValidationUtil.hasCompatibleParamType(methodMetaData.getReturnTypeMetaData(), getParameterMetaDataList(targetClassEnclosedElement).get(0))) {
                            case SAME_CLASS:
                            case UPCAST_NEEDED:
                                return String.format(Constant.FIELD_TRANSFORMER_PATTER_FOR_MORPH, getMethodName(targetClassEnclosedElement), fieldTransformerMetaData.getTransformerClassName(), methodMetaData.getMethodName(), getMethodName(sourceClassEnclosedElement));
                            case DOWNCAST_NEEDED:
                                return String.format(Constant.FIELD_TRANSFORMER_PATTER_FOR_MORPH_FOR_DOWNCAST, getMethodName(targetClassEnclosedElement), getParameterMetaDataList(targetClassEnclosedElement).get(0).getParameterType(), fieldTransformerMetaData.getTransformerClassName(), methodMetaData.getMethodName(), getMethodName(sourceClassEnclosedElement));
                            case NO_MATCH:
                                throw new ValidationException(String.format(Constant.INCOMPATIBLE_TRANSFORM_METHOD, fieldTransformerMetaData.getTransformerClassName(), getParameterMetaDataList(targetClassEnclosedElement).get(0).getParameterType()));
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String generateMappingAfterApplyingFieldTransformerForReverseMorph(ArrayList<FieldTransformerMetaData> fieldTransformerMetaDataList, Element sourceClassElement, Types typeUtil, ExecutableElement sourceClassEnclosedElement, ExecutableElement targetClassEnclosedElement) throws ValidationException {
        if (fieldTransformerMetaDataList == null)
            fieldTransformerMetaDataList = ElementUtil.extractFieldTransformerMetaData(sourceClassElement, typeUtil);

        for (FieldTransformerMetaData fieldTransformerMetaData : fieldTransformerMetaDataList) {

            if (fieldTransformerMetaData.getFieldMetaData().getFieldName().equalsIgnoreCase(getVariableNameFromSetter(sourceClassEnclosedElement))) {
                for (MethodMetaData methodMetaData : fieldTransformerMetaData.getTransformerMethodMetaData()) {
                    if (methodMetaData.getMethodName().equals(Constant.REVERSE_TRANSFORM)) {
                        switch (ValidationUtil.hasCompatibleParamType(methodMetaData.getReturnTypeMetaData(), getParameterMetaDataList(sourceClassEnclosedElement).get(0))) {
                            case SAME_CLASS:
                            case UPCAST_NEEDED:
                                return String.format(Constant.FIELD_TRANSFORMER_PATTER_FOR_REVERSE_MORPH, getMethodName(sourceClassEnclosedElement), fieldTransformerMetaData.getTransformerClassName(), methodMetaData.getMethodName(), getMethodName(targetClassEnclosedElement));
                            case DOWNCAST_NEEDED:
                                return String.format(Constant.FIELD_TRANSFORMER_PATTER_FOR_REVERSE_MORPH_FOR_DOWNCAST, getMethodName(sourceClassEnclosedElement), getParameterMetaDataList(sourceClassEnclosedElement).get(0).getParameterType(), fieldTransformerMetaData.getTransformerClassName(), methodMetaData.getMethodName(), getMethodName(targetClassEnclosedElement));
                            case NO_MATCH:
                                throw new ValidationException(String.format(Constant.INCOMPATIBLE_REVERSE_TRANSFORM_METHOD, fieldTransformerMetaData.getTransformerClassName(), getParameterMetaDataList(sourceClassEnclosedElement).get(0).getParameterType()));
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String getVariableNameFromSetter(ExecutableElement executableElement) {
        return getMethodName(executableElement).substring(3);
    }

    private static String CapitalizeFirstLetter(String string) {
        if (string == null || string.length() == 0) {
            return null;
        } else if (string.length() == 1) {
            return string.toUpperCase();
        } else {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        }
    }

    public static String getVariableNameFromGetter(ExecutableElement targetClassEnclosedElement) {
        String methodName = getMethodName(targetClassEnclosedElement);
        if (ValidationUtil.startsWithIsWord(methodName)) {
            return methodName.substring(2);
        } else {
            return methodName.substring(3);
        }
    }

    public static ParameterMetaData getReturnTypeMetaData(ExecutableElement executableElement) {
        ParameterMetaData parameterMetaData = new ParameterMetaData();
        parameterMetaData.setParameterType(executableElement.getReturnType().toString());
        parameterMetaData.setPrimitive(executableElement.getReturnType().getKind().isPrimitive());
        return parameterMetaData;
    }

    public static String getMethodName(ExecutableElement executableElement) {
        return executableElement.getSimpleName().toString();
    }

    public static ArrayList<String> getModifierList(ExecutableElement executableElement) {
        ArrayList<String> modifierList = new ArrayList<>();
        for (Modifier modifier : executableElement.getModifiers()) {
            modifierList.add(modifier.toString());
        }
        return modifierList;
    }


    public static ArrayList<ParameterMetaData> getParameterMetaDataList(ExecutableElement executableElement) {
        ArrayList<ParameterMetaData> parameterList = new ArrayList<>();
        for (VariableElement variableElement : executableElement.getParameters()) {
            ParameterMetaData parameterMetaData = new ParameterMetaData();
            parameterMetaData.setParameterType(variableElement.asType().toString());
            parameterMetaData.setPrimitive(variableElement.asType().getKind().isPrimitive());
            parameterMetaData.setParameterName(variableElement.getSimpleName().toString());
            parameterList.add(parameterMetaData);
        }
        return parameterList;
    }

    public static ArrayList<MorphToFieldMetaData> newExtractMorphToFieldMetaData(Element element) {
        ArrayList<MorphToFieldMetaData> morphToFieldMetaDataList = new ArrayList<>();

        for (VariableElement enclosedElement : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            if (ValidationUtil.isFieldAnnotatedWithMorphTo(enclosedElement)) {
                MorphToFieldMetaData morphToFieldMetaData = new MorphToFieldMetaData();
                morphToFieldMetaData.setFieldMetaData(extractFieldMetaData(enclosedElement));
                morphToFieldMetaData.setMorphedFieldName(getAnnotationValue(enclosedElement, MorphToField.class, null).toString());

                morphToFieldMetaDataList.add(morphToFieldMetaData);
            }
        }

        return morphToFieldMetaDataList;
    }

    private static Object getAnnotationValue(VariableElement element, Class annotationClass, String annotationParameterName) {
        return getAnnotationValue(getAnnotationMirror(element, annotationClass), annotationParameterName).getValue();
    }


    private static String getPossibleSetterName(ExecutableElement element) {
        if (getMethodName(element).startsWith(Constant.I)) {
            return Constant.SET + getMethodName(element).substring(2);
        } else {
            return Constant.SET + getMethodName(element).substring(3);
        }
    }

    private static ArrayList<FieldTransformerMetaData> extractFieldTransformerMetaData(Element element, Types typeUtil) throws ValidationException {
        ArrayList<FieldTransformerMetaData> fieldTransformerMetaDataList = new ArrayList<>();

        for (VariableElement enclosedElement : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            if (ValidationUtil.isFieldAnnotatedWithFieldTransformer(enclosedElement)) {
                TypeElement transformerClassElement = ElementUtil.getTypeElement(ElementUtil.getAnnotationValue(ElementUtil.getAnnotationMirror(enclosedElement, FieldTransformer.class), null), typeUtil);

                if (ValidationUtil.isValidValueForFieldTransformerAnnotation(transformerClassElement)) {
                    FieldTransformerMetaData fieldTransformerMetaData = new FieldTransformerMetaData();
                    fieldTransformerMetaData.setFieldMetaData(extractFieldMetaData(enclosedElement));
                    fieldTransformerMetaData.setTransformerMethodMetaData(extractMethodMetaData(transformerClassElement));
                    fieldTransformerMetaData.setTransformerClassName(transformerClassElement.getQualifiedName().toString());

                    fieldTransformerMetaDataList.add(fieldTransformerMetaData);
                }
            }
        }

        return fieldTransformerMetaDataList;
    }

    public static MorpherMetaData generateMorpherMetaData(TypeElement sourceClassElement, TypeElement targetClassElement, Types typeUtils) throws ValidationException {
        MorpherMetaData morpherMetaData = new MorpherMetaData();

        ClassName sourceClassName = ClassName.bestGuess(sourceClassElement.getQualifiedName().toString());

        morpherMetaData.setPackageName(sourceClassName.packageName());
        morpherMetaData.setSimpleName(sourceClassName.simpleName());

        morpherMetaData.setSourceClassType(sourceClassName);
        morpherMetaData.setTargetClassType(ClassName.bestGuess(targetClassElement.getQualifiedName().toString()));

        ElementUtil.generateGetterSetterMapping(sourceClassElement, targetClassElement, typeUtils, morpherMetaData);

        return morpherMetaData;
    }

    public static boolean isNoArgConstructorFound(Element type) {
        boolean noArgConstructorFound = false;
        for (ExecutableElement enclosedElement : ElementFilter.constructorsIn(type.getEnclosedElements())) {
            noArgConstructorFound = enclosedElement.getParameters().size() == 0;
        }
        return noArgConstructorFound;
    }
}
