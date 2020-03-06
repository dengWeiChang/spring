package org.framework.lang;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

/**
 * spring通用注释
 * 指定package下参数及返回值不可为空
 *
 * <p>JSR-305
 *
 * <p>用于package级别
 *
 * @author dengweichang
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})
public @interface NonNullApi {
}
