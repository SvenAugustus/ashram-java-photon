/*
 * Copyright 2018-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.flysium.io.photon.lang.c004_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * <p>
 * 注解的继承方式
 * 1、对于类上的注解，如果没有@Inherited，子类是无法继承注解定义，如果带@Inherited，则可以继承。
 * 2、对于方法上的注解，无论有没有@Inherited，子类没有实现或覆盖重新的方法是可以继承注解定义的。
 * 3、对于方法上的注解，无论有没有@Inherited，子类实现或覆盖重新的方法是不可以继承注解定义的。
 * </p>
 *
 * @author Sven Augustus
 * @version 1.0
 */
public class AnnotationTests {

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)  //可以通过反射读取注解
	@Inherited
			//可以被继承
	@interface MyTypeAnnotation {

		String value();
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)  //可以通过反射读取注解
	@Inherited
			//可以被继承
	@interface MyAnnotation {

		String value();
	}


	@MyTypeAnnotation(value = "类名上的注解")
	abstract class AbstractParentClass {

		@MyAnnotation(value = "父类的abstractMethod方法")
		public abstract void abstractMethod();

		@MyAnnotation(value = "父类的doExtends方法")
		public void doExtends() {
			System.out.println(" ParentClass doExtends ...");
		}

		@MyAnnotation(value = "父类的doHandle方法")
		public void doHandle() {
			System.out.println(" ParentClass doHandle ...");
		}
	}

	class SubClass extends AbstractParentClass {

		//子类实现父类的抽象方法
		@Override
		public void abstractMethod() {
			System.out.println("子类实现父类的abstractMethod抽象方法");
		}

		//子类继承父类的doExtends方法

		//子类覆盖父类的doHandle方法
		@Override
		public void doHandle() {
			System.out.println("子类覆盖父类的doHandle方法");
		}
	}

	public static void main(String[] args) throws NoSuchMethodException {
		Class<SubClass> clazz = SubClass.class;

		if (clazz.isAnnotationPresent(MyTypeAnnotation.class)) {
			MyTypeAnnotation cla = clazz
					.getAnnotation(MyTypeAnnotation.class);
			System.out.println("子类继承到父类类上Annotation,其信息如下：" + cla.value());
		} else {
			System.out.println("子类没有继承到父类类上Annotation");
		}

		// 实现抽象方法测试
		Method method = clazz.getMethod("abstractMethod", new Class[]{});
		if (method.isAnnotationPresent(MyAnnotation.class)) {
			MyAnnotation ma = method
					.getAnnotation(MyAnnotation.class);
			System.out
					.println("子类实现父类的abstractMethod抽象方法，继承到父类抽象方法中的Annotation,其信息如下：" + ma.value());
		} else {
			System.out.println("子类实现父类的abstractMethod抽象方法，没有继承到父类抽象方法中的Annotation");
		}

		//覆盖测试
		Method methodOverride = clazz.getMethod("doExtends", new Class[]{});
		if (methodOverride.isAnnotationPresent(MyAnnotation.class)) {
			MyAnnotation ma = methodOverride
					.getAnnotation(MyAnnotation.class);
			System.out
					.println("子类继承父类的doExtends方法，继承到父类doExtends方法中的Annotation,其信息如下：" + ma.value());
		} else {
			System.out.println("子类继承父类的doExtends方法，没有继承到父类doExtends方法中的Annotation");
		}

		//继承测试
		Method method3 = clazz.getMethod("doHandle", new Class[]{});
		if (method3.isAnnotationPresent(MyAnnotation.class)) {
			MyAnnotation ma = method3
					.getAnnotation(MyAnnotation.class);
			System.out
					.println("子类覆盖父类的doHandle方法，继承到父类doHandle方法中的Annotation,其信息如下：" + ma.value());
		} else {
			System.out.println("子类覆盖父类的doHandle方法，没有继承到父类doHandle方法中的Annotation");
		}
	}

}
