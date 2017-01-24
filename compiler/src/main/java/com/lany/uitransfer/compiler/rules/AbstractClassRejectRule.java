package com.lany.uitransfer.compiler.rules;

import com.lany.uitransfer.annotaion.TransferTarget;
import com.lany.uitransfer.compiler.exceptions.AbstractClassRejectedException;
import com.lany.uitransfer.compiler.exceptions.RuleRejectedException;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class AbstractClassRejectRule implements Rule<TypeElement> {

    @Override
    public void validateRule(TypeElement element) throws RuleRejectedException {
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw throwException(element);
        }
    }

    @Override
    public RuleRejectedException throwException(TypeElement element) {
        return new AbstractClassRejectedException(
                String.format("The class %s is abstract. You can't annotate abstract classes with @%s",
                        element.getQualifiedName().toString(), TransferTarget.class.getSimpleName()));
    }
}
