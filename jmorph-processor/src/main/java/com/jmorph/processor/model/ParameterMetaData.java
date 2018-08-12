package com.jmorph.processor.model;

public class ParameterMetaData {
    private String parameterType;
    private String parameterName;
    private boolean primitive;

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    @Override
    public String toString() {
        return "ParameterMetaData{" +
                "parameterType='" + parameterType + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", primitive=" + primitive +
                '}';
    }
}
