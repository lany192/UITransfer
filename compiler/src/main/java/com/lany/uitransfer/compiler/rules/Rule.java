package com.lany.uitransfer.compiler.rules;

import com.lany.uitransfer.compiler.exceptions.RuleRejectedException;

import javax.lang.model.element.Element;


/**
 * 规则校验接口
 */
public interface Rule<T extends Element> {

    void validateRule(T element) throws RuleRejectedException;

    RuleRejectedException throwException(T element);
}
