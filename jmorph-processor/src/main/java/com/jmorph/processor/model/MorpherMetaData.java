package com.jmorph.processor.model;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;

public class MorpherMetaData {
    private String packageName;
    private String simpleName;

    private ArrayList<String> morphMethodMetaData;
    private ArrayList<String> reverseMorphMethodMetaData;
    private TypeName sourceClassType;
    private TypeName targetClassType;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public ArrayList<String> getMorphMethodMetaData() {
        return morphMethodMetaData;
    }

    public void setMorphMethodMetaData(ArrayList<String> morphMethodMetaData) {
        this.morphMethodMetaData = morphMethodMetaData;
    }

    public ArrayList<String> getReverseMorphMethodMetaData() {
        return reverseMorphMethodMetaData;
    }

    public void setReverseMorphMethodMetaData(ArrayList<String> reverseMorphMethodMetaData) {
        this.reverseMorphMethodMetaData = reverseMorphMethodMetaData;
    }

    public TypeName getSourceClassType() {
        return sourceClassType;
    }

    public void setSourceClassType(TypeName sourceClassType) {
        this.sourceClassType = sourceClassType;
    }

    public TypeName getTargetClassType() {
        return targetClassType;
    }

    public void setTargetClassType(TypeName targetClassType) {
        this.targetClassType = targetClassType;
    }
}
