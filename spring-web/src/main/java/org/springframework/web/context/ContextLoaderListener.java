/*
 * Copyright 2002-2012 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 开启应用的引导程序的监听器类，以及 关闭Spring的根应用上下文 WebApplicationContext。
 * 
 * web.xml文件中 ，监听器必须注册在 Log4jConfigListener 之后。
 * 
 * 在Spring 3.1 之后，ContextLoaderListener 支持通过构造函数ContextLoaderListener(WebApplicationContext) 注入根web应用上下文， 允许在 Servlet
 * 3.0+ 环境里 通过程序编码配置。
 *
 * TODO 所以,在spring 3.1 之后,如果是默认的ContextLoaderListener的话,则可以不在web.xml中配置listener,在dispatch中,会通过servletContext.addListener来
 * TODO 设置这个变量到servlet上下文中.
 *
 * Bootstrap listener to start up and shut down Spring's root {@link WebApplicationContext}. Simply delegates to
 * {@link ContextLoader} as well as to {@link ContextCleanupListener}.
 * 
 * <p>
 * This listener should be registered after {@link org.springframework.web.util.Log4jConfigListener} in {@code web.xml},
 * if the latter is used.
 * 
 * <p>
 * As of Spring 3.1, {@code ContextLoaderListener} supports injecting the root web application context via the
 * {@link #ContextLoaderListener(WebApplicationContext)} constructor, allowing for programmatic configuration in Servlet
 * 3.0+ environments. See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 * 
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 17.02.2003
 * @see org.springframework.web.WebApplicationInitializer
 * @see org.springframework.web.util.Log4jConfigListener
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

    private ContextLoader contextLoader;

    /**
     * 创建一个ContextLoaderListener 对象的时候，将会创建一个web应用的上下文，而这个上下文，需要基于contextConfigLocation配置中 的context-params参数值，因此，在
     * web.xml 文件内部需要配置contextConfigLocation 项，否则会出现Error，无法启动spring 应用。
     * Note：如果没有配置，在ContextLoader内部会默认配置一个文件地址：/WEB-INF/applicationContext.xml,所以如果你不想配置web.xml，
     * 则需要创建/WEB-INF/applicationContext.xml文件。
     * 
     * 此外，创建了应用上下文 将被注册到 ServletContext，其属性名为：WebApplicationContext.ROOT .ServletContext维护的是一个Map的数据结构.
     * 
     * Create a new {@code ContextLoaderListener} that will create a web application context based on the "contextClass"
     * and "contextConfigLocation" servlet context-params. See {@link ContextLoader} superclass documentation for
     * details on default values for each.
     * <p>
     * This constructor is typically used when declaring {@code ContextLoaderListener} as a {@code <listener>} within
     * {@code web.xml}, where a no-arg constructor is required.
     * <p>
     * The created application context will be registered into the ServletContext under the attribute name
     * {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and the Spring application context will be
     * closed when the {@link #contextDestroyed} lifecycle method is invoked on this listener.
     * 
     * @see ContextLoader
     * @see #ContextLoaderListener(WebApplicationContext)
     * @see #contextInitialized(ServletContextEvent)
     * @see #contextDestroyed(ServletContextEvent)
     */
    public ContextLoaderListener() {
    }

    /**
     * 使用给定的 应用上下文 来创建一个新的 ContextLoaderListener 对象。该方法让我们可以通过 addListener 方法在 Servlet 3.0+ 环境下注册监听器
     * 
     * 
     * 
     * Create a new {@code ContextLoaderListener} with the given application context. This constructor is useful in
     * Servlet 3.0+ environments where instance-based registration of listeners is possible through the
     * {@link javax.servlet.ServletContext#addListener} API.
     * <p>
     * The context may or may not yet be
     * {@linkplain org.springframework.context.ConfigurableApplicationContext#refresh() refreshed}. If it (a) is an
     * implementation of {@link ConfigurableWebApplicationContext} and (b) has <strong>not</strong> already been
     * refreshed (the recommended approach), then the following will occur:
     * <ul>
     * <li>If the given context has not already been assigned an
     * {@linkplain org.springframework.context.ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
     * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to the application context</li>
     * <li>{@link #customizeContext} will be called</li>
     * <li>Any {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer}s
     * specified through the "contextInitializerClasses" init-param will be applied.</li>
     * <li>{@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()} will be called</li>
     * </ul>
     * If the context has already been refreshed or does not implement {@code ConfigurableWebApplicationContext}, none
     * of the above will occur under the assumption that the user has performed these actions (or not) per his or her
     * specific needs.
     * <p>
     * See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
     * <p>
     * In any case, the given application context will be registered into the ServletContext under the attribute name
     * {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and the Spring application context will be
     * closed when the {@link #contextDestroyed} lifecycle method is invoked on this listener.
     * 
     * @param context the application context to manage
     * @see #contextInitialized(ServletContextEvent)
     * @see #contextDestroyed(ServletContextEvent)
     */
    public ContextLoaderListener(WebApplicationContext context) {
        super(context);
    }

    /**
     * 初始化根web应用上下文
     * 
     * Initialize the root web application context.
     */
    public void contextInitialized(ServletContextEvent event) {
        this.contextLoader = createContextLoader();//创建子上下文加载监听器，默认为null
        if (this.contextLoader == null) {
            this.contextLoader = this;
        }
        this.contextLoader.initWebApplicationContext(event.getServletContext());//使用给定的上下文环境配置来初始化spring web context
    }

    /**
     * Create the ContextLoader to use. Can be overridden in subclasses.
     * 
     * @return the new ContextLoader
     * @deprecated in favor of simply subclassing ContextLoaderListener itself (which extends ContextLoader, as of
     *             Spring 3.0)
     */
    @Deprecated
    protected ContextLoader createContextLoader() {
        return null;
    }

    /**
     * Return the ContextLoader used by this listener.
     * 
     * @return the current ContextLoader
     * @deprecated in favor of simply subclassing ContextLoaderListener itself (which extends ContextLoader, as of
     *             Spring 3.0)
     */
    @Deprecated
    public ContextLoader getContextLoader() {
        return this.contextLoader;
    }

    /**
     * Close the root web application context.
     */
    public void contextDestroyed(ServletContextEvent event) {
        if (this.contextLoader != null) {
            this.contextLoader.closeWebApplicationContext(event.getServletContext());
        }
        ContextCleanupListener.cleanupAttributes(event.getServletContext());
    }

}
