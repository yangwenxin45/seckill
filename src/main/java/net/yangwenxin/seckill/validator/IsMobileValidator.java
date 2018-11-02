package net.yangwenxin.seckill.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import net.yangwenxin.seckill.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = true;
	
	@Override
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (required) {
			return ValidatorUtil.isMobile(value);
		} else {
			if (StringUtils.isEmpty(value)) {
				return true;
			} else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}

}
