package org.framework.lang;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.*;

/**
 * spring通用注释
 * 表示元素不可为空{@code null}
 *
 * <p>JSR-305
 *
 * <p>适用于入参、返回值以及对象属性
 * @author dengweichang
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierNickname
public @interface NonNull {
}
