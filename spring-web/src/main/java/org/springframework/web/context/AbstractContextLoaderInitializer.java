/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.WebApplicationInitializer;

/**
 * 为在servlet 上下文中注册一个WebApplicationInitializer接口的实现类提供基础抽象类.这个类只要求子类实现
 * createRootApplicationContext方法,以便于registerContextLoaderListener方法调用.
 *
 * Convenient base class for {@link WebApplicationInitializer} implementations
 * that register a {@link ContextLoaderListener} in the servlet context.
 *
 * <p>The only method required to be implemented by subclasses is
 * {@link #createRootApplicationContext()}, which gets invoked from
 * {@link #registerContextLoaderListener(ServletContext)}.
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @since 3.2
 */
public abstract class AbstractContextLoaderInitializer implements WebApplicationInitializer {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public void onStartup(ServletContext servletContext) throws ServletException {
		registerContextLoaderListener(servletContext);
	}

	/**
	 * Register a {@link ContextLoaderListener} against the given servlet context. The
	 * {@code ContextLoaderListener} is initialized with the application context returned
	 * from the {@link #createRootApplicationContext()} template method.
	 * @param servletContext the servlet context to register the listener against
	 */
	protected void registerContextLoaderListener(ServletContext servletContext) {
		WebApplicationContext rootAppContext = createRootApplicationContext();
		if (rootAppContext != null) {
			// 将关联的ContextLoaderListener注册到servlet监听器中,而ContextLoaderListener包含根应用上下文
			servletContext.addListener(new ContextLoaderListener(rootAppContext));
		}
		else {
			logger.debug("No ContextLoaderListener registered, as " +
					"createRootApplicationContext() did not return an application context");
		}
	}

	/**
	 * 这里就是创建根应用上下文,提供给 ContextLoaderListener,即注册到servlet context上,以便javax 的servlet容器可以
	 * 在创建时创发该监听器相关动作.一般,在WebApplicationContext中包含中间层服务,数据源操作等.
	 *
	 * Create the "<strong>root</strong>" application context to be provided to the
	 * {@code ContextLoaderListener}.
	 * <p>The returned context is delegated to
	 * {@link ContextLoaderListener#ContextLoaderListener(WebApplicationContext)} and will
	 * be established as the parent context for any {@code DispatcherServlet} application
	 * contexts. As such, it typically contains middle-tier services, data sources, etc.
	 * @return the root application context, or {@code null} if a root context is not
	 * desired
	 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
	 */
	protected abstract WebApplicationContext createRootApplicationContext();

}
