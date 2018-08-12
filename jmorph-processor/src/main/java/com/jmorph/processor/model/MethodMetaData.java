package com.jmorph.processor.model;

import java.util.ArrayList;

public class MethodMetaData {

    private ArrayList<String> modifierList;
    private ParameterMetaData returnTypeMetaData;
    private String methodName;
    private ArrayList<ParameterMetaData> parameterMetaDataList;

    public void setModifierList(ArrayList<String> modifierList) {
        this.modifierList = modifierList;
    }

    public ArrayList<String> getModifierList() {
        return modifierList;
    }

    public void setReturnTypeMetaData(ParameterMetaData returnTypeMetaData) {
        this.returnTypeMetaData = returnTypeMetaData;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterMetaDataList(ArrayList<ParameterMetaData> parameterMetaDataList) {
        this.parameterMetaDataList = parameterMetaDataList;
    }

    public ParameterMetaData getReturnTypeMetaData() {
        return returnTypeMetaData;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<ParameterMetaData> getParameterMetaDataList() {
        return parameterMetaDataList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < modifierList.size(); i++) {
            String modifier = modifierList.get(i);
            stringBuilder.append(modifier);
            stringBuilder.append(" ");
        }
        stringBuilder.append(returnTypeMetaData);
        stringBuilder.append(" ");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < parameterMetaDataList.size(); i++) {
            ParameterMetaData parameterMetaData = parameterMetaDataList.get(i);
            stringBuilder.append(parameterMetaData.toString());
            if (i != parameterMetaDataList.size() - 1)
                stringBuilder.append(", ");
        }
        stringBuilder.append(");");
        return stringBuilder.toString();
    }
}
