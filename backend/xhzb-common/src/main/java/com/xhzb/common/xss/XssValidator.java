package com.xhzb.common.xss;

import com.xhzb.common.utils.StringUtils;
import com.xhzb.common.utils.html.EscapeUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 自定义xss校验注解实现
 * 
 * @author xhzb
 */
public class XssValidator implements ConstraintValidator<Xss, String>
{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        return !containsHtml(value);
    }

    public static boolean containsHtml(String value)
    {
        // 如果清洗后和原文不一致，说明包含危险内容
        String cleaned = EscapeUtil.clean(value);
        return !cleaned.equals(value);
    }
}