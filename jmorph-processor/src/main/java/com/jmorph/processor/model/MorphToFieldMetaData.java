package com.jmorph.processor.model;

public class MorphToFieldMetaData {
    private FieldMetaData fieldMetaData;
    private String morphedFieldName;

    public void setFieldMetaData(FieldMetaData fieldMetaData) {
        this.fieldMetaData = fieldMetaData;
    }

    public FieldMetaData getFieldMetaData() {
        return fieldMetaData;
    }

    public void setMorphedFieldName(String morphedFieldName) {
        this.morphedFieldName = morphedFieldName;
    }

    public String getMorphedFieldName() {
        return morphedFieldName;
    }

    @Override
    public String toString() {
        return "MorphToFieldMetaData{" +
                "fieldMetaData=" + fieldMetaData +
                ", morphedFieldName='" + morphedFieldName + '\'' +
                '}';
    }
}
