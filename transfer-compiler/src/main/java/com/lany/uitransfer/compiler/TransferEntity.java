package com.lany.uitransfer.compiler;

import java.util.ArrayList;
import java.util.List;

public class TransferEntity {
    public String packageName;
    public String className;
    public List<FieldEntity> fields = new ArrayList<>();
}