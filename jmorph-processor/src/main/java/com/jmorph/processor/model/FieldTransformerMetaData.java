package com.jmorph.processor.model;

import java.util.ArrayList;

public class FieldTransformerMetaData {
    private FieldMetaData fieldMetaData;
    private String transformerClassName;
    private ArrayList<MethodMetaData> transformerMethodMetaData;

    public void setFieldMetaData(FieldMetaData fieldMetaData) {
        this.fieldMetaData = fieldMetaData;
    }

    public FieldMetaData getFieldMetaData() {
        return fieldMetaData;
    }

    public void setTransformerClassName(String transformerClassName) {
        this.transformerClassName = transformerClassName;
    }

    public String getTransformerClassName() {
        return transformerClassName;
    }

    public void setTransformerMethodMetaData(ArrayList<MethodMetaData> transformerMethodMetaData) {
        this.transformerMethodMetaData = transformerMethodMetaData;
    }

    public ArrayList<MethodMetaData> getTransformerMethodMetaData() {
        return transformerMethodMetaData;
    }

    @Override
    public String toString() {
        return "FieldTransformerMetaData{" +
                "fieldMetaData=" + fieldMetaData +
                ", transformerClassName='" + transformerClassName + '\'' +
                ", transformerMethodMetaData=" + transformerMethodMetaData +
                '}';
    }
}
