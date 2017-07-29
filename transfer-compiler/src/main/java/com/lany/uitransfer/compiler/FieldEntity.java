package com.lany.uitransfer.compiler;

public class FieldEntity {
    public String fieldName;
    public String fieldType;
    public String fieldValue;
    public String originalType = "";
    public String fieldParam = "";

    public FieldEntity() {

    }

    public FieldEntity(String fieldName, String fieldType, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldValue = fieldValue;
    }
}
