package com.jmorph.processor.util;

import com.jmorph.processor.constant.Constant;
import com.jmorph.processor.model.MorpherMetaData;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class JavaPoetUtil {

    public static void createMorpherFile(MorpherMetaData morpherMetaData, Filer filer) throws IOException {
        MethodSpec.Builder morphMethodSpecBuilder = MethodSpec.methodBuilder(Constant.MORPH)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(morpherMetaData.getTargetClassType())
                .addParameter(morpherMetaData.getSourceClassType(), Constant.SOURCE)
                .addStatement(Constant.NEW_TARGET_OBJECT_PATTERN, morpherMetaData.getTargetClassType());
        for (String getterSetterMapping : morpherMetaData.getMorphMethodMetaData()) {
            morphMethodSpecBuilder.addStatement(getterSetterMapping);
        }
        morphMethodSpecBuilder.addStatement(Constant.RETURN_TARGET);


        MethodSpec.Builder reverseMorphMethodSpecBuilder = MethodSpec.methodBuilder(Constant.REVERSE_MORPH)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(morpherMetaData.getSourceClassType())
                .addParameter(morpherMetaData.getTargetClassType(), Constant.TARGET)
                .addStatement(Constant.NEW_SOURCE_OBJECT_PATTERN, morpherMetaData.getSourceClassType());
        for (String getterSetterMapping : morpherMetaData.getReverseMorphMethodMetaData()) {
            reverseMorphMethodSpecBuilder.addStatement(getterSetterMapping);
        }
        reverseMorphMethodSpecBuilder.addStatement(Constant.RETURN_SOURCE);


        TypeSpec morpherTypeSpec = TypeSpec.classBuilder(String.format(Constant.MORPHER_CLASS_NAME_PATTERN, morpherMetaData.getSimpleName()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc(Constant.AUTO_GENERATE_CLASS_FROM_JMORPH_LIBRARY)
                .addMethod(morphMethodSpecBuilder.build())
                .addMethod(reverseMorphMethodSpecBuilder.build())
                .build();


        JavaFile.builder(Constant.MORPHER_PACKAGE_PATTERN, morpherTypeSpec)
                .build()
                .writeTo(filer);
    }
}
