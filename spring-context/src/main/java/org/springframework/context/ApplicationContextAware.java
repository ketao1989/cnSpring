/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;

/**
 * 一个接口，任务对象想要自己在ApplicationContext中运行，都可以去实现该接口。
 *
 * 实现该接口，比如，你想要一个对象访问一系列合作的beans。但是需要注意，通过在配置中使用bean引用由于通过该接口实现
 * bean查找目的。
 *
 * 这个接口也可以被一个需要访问文件资源的对象。比如，想要调用{@code getResource}，想要发布application 事件，或者需要访问
 * MessageSource。尽管如此，它还是更喜欢去实现更特定的接口，比如特定场景的{@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} 或者 {@link MessageSourceAware}。
 *
 * 注意，文件资源依赖也可以作为{@link org.springframework.core.io.Resource}类型的bean properties暴露出来，
 * 自动地通过字符串类型转化为bean，通过bean工厂方法。这移除了为了仅仅访问指定文件资源的目的而去实现任务回调接口的需求。
 *
 * {@link org.springframework.context.support.ApplicationObjectSupport}是这个接口的方便的实现基类。
 *
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link org.springframework.beans.factory.BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see ResourceLoaderAware
 * @see ApplicationEventPublisherAware
 * @see MessageSourceAware
 * @see org.springframework.context.support.ApplicationObjectSupport
 * @see org.springframework.beans.factory.BeanFactoryAware
 */
public interface ApplicationContextAware extends Aware {

	/**
	 * Set the ApplicationContext that this object runs in.
	 * Normally this call will be used to initialize the object.
	 * <p>Invoked after population of normal bean properties but before an init callback such
	 * as {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
	 * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
	 * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
	 * {@link MessageSourceAware}, if applicable.
	 * @param applicationContext the ApplicationContext object to be used by this object
	 * @throws ApplicationContextException in case of context initialization errors
	 * @throws BeansException if thrown by application context methods
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
