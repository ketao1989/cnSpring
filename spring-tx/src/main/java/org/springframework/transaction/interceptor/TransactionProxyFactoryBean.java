/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.interceptor;

import java.util.Properties;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * proxy工厂bean是一个使用标准的AOP（ProxyFactoryBean）实现应用的类，
 * 但是，其核心使用的是 TransactionInterceptor 定义。
 *
 * 本类的实际作用就是，通过xml配置来完成对 TransactionInterceptor 相关属性进行设置。然后把该Interceptor
 * 赋值给AOP的通知器，这样就可以使用AOP 完成事务管理的功能。
 *
 * 需要注意的是，在spring 2.0之后，可以不在需要在xml中配置声明 TransactionProxyFactoryBean，就能
 * 实现事务管理相关操作，spring可以自己完成相关的默认配置。但是，分析代码的时候，入口依然从该代理类开始。
 *
 * 使用TransactionProxyFactoryBean声明，需要指定三个主要的配置项：
 * 1. transactionManager：事务管理器，对应的是PlatformTransactionManager的实现，一般有spring jdbc自带的，hibernate，mybatis等提供的事务管理器。
 * 2. transactionAttributes ：事务管理属性，比如事物策略的一些方法：对add*开头的方法采用required的事务策略，则在这里设置。还有指定只读，读写属性也是。
 * 3. target ：设置的目标。这里指定那些需要参与事务管理的类，比如一些DAO层相关的实现类。
 *
 *
 * <pre code="class">
 * {@code
 * <bean id="baseTransactionProxy" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"
 *     abstract="true">
 *   <property name="transactionManager" ref="transactionManager"/>
 *   <property name="transactionAttributes">
 *     <props>
 *       <prop key="insert*">PROPAGATION_REQUIRED</prop>
 *       <prop key="update*">PROPAGATION_REQUIRED</prop>
 *       <prop key="*">PROPAGATION_REQUIRED,readOnly</prop>
 *     </props>
 *   </property>
 * </bean>
 *
 * <bean id="myProxy" parent="baseTransactionProxy">
 *   <property name="target" ref="myTarget"/>
 * </bean>
 *
 * <bean id="yourProxy" parent="baseTransactionProxy">
 *   <property name="target" ref="yourTarget"/>
 * </bean>}</pre>
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @author Rod Johnson
 * @author Chris Beams
 * @since 21.08.2003
 * @see #setTransactionManager
 * @see #setTarget
 * @see #setTransactionAttributes
 * @see TransactionInterceptor
 * @see org.springframework.aop.framework.ProxyFactoryBean
 */
@SuppressWarnings("serial")
public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean
		implements BeanFactoryAware {

    //显然，这个Interceptor是代理Bean的核心，通过AOP的功能来调用TransactionInterceptor，完成事务处理的功能
	private final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();

	private Pointcut pointcut;


	/**
	 * Set the transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 * @see TransactionInterceptor#setTransactionManager
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionInterceptor.setTransactionManager(transactionManager);
	}

	/**
     * 通过注入设置事务管理属性。这些property的key时方法的名字，value是属性的描述（使用TransactionAttributeEditor解析）。
     * 比如，key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".、
     *
     * 此外，方法名字来着target类，不管是接口还是类本身。
     *
     * 其逻辑是在赋值的时候，会定义一个 NameMatchTransactionAttributeSource，然后这个类会内部处理解析事务属性，自我赋值
     * 然后，Interceptor实际设置的是NameMatchTransactionAttributeSource类实例。
     *
	 * @see #setTransactionAttributeSource
	 * @see TransactionInterceptor#setTransactionAttributes
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionInterceptor.setTransactionAttributes(transactionAttributes);
	}

	/**
     * 这里使用非xml方式来设置事务管理属性，比如Map存储属性MethodMapTransactionAttributeSource。
     *
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 * @see #setTransactionAttributes
	 * @see TransactionInterceptor#setTransactionAttributeSource
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource);
	}

	/**
	 * Set a pointcut, i.e a bean that can cause conditional invocation
	 * of the TransactionInterceptor depending on method and attributes passed.
	 * Note: Additional interceptors are always invoked.
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
     * 这个回调是可选的：如果在 BeanFactory 运行，并且没有事务管理器设置，那么一个类型为PlatformTransactionManager
     * 的bean将会被BeanFactory产生。
     *
	 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
	 * @see org.springframework.transaction.PlatformTransactionManager
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.transactionInterceptor.setBeanFactory(beanFactory);
	}


	/**
	 * Creates an advisor for this FactoryBean's TransactionInterceptor.
	 */
	@Override
	protected Object createMainInterceptor() {
		this.transactionInterceptor.afterPropertiesSet();
		if (this.pointcut != null) {
            // 使用默认的通知器
			return new DefaultPointcutAdvisor(this.pointcut, this.transactionInterceptor);
		}
		else {
            // 如果没有设置pointcut，则使用TransactionAttributeSourceAdvisor作为通知器，并且把transactionInterceptor设置给它。
			return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);
		}
	}

}
