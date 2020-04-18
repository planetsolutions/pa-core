package ru.doccloud.webapp;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import net.bull.javamelody.MonitoringFilter;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.chemistry.opencmis.server.impl.endpoints.SimpleCmisEndpointsDocumentServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import ru.doccloud.cmis.server.*;
import ru.doccloud.common.datasources.DatasourceSettingsBean;
import ru.doccloud.common.elasticsearch.ElasticsearchSettingsBean;
import ru.doccloud.service.UserService;
import ru.doccloud.webapp.config.MultiDSConfiguration;
import ru.doccloud.webapp.settings.*;

import javax.servlet.*;
import javax.sql.DataSource;
import java.util.*;

//import ru.doccloud.config.DoccloudApplicationContext;

@Configuration
@ComponentScan({
        "ru.doccloud.config",
        "ru.doccloud.document.controller",
        "ru.doccloud.service",
        "ru.doccloud.repository",
        "ru.doccloud.common.service",
        "ru.doccloud.storage",
        "ru.doccloud.amazon",
        "ru.doccloud.filestorage",
        "ru.doccloud.storagemanager",
        "ru.doccloud.cmis.server",
        "ru.doccloud.webapp.audit.aspect",
        "ru.doccloud.webapp.settings",
        "ru.doccloud.cmis.server.service"
})
@Import({
        MultiDSConfiguration.class,
})
@ImportResource("classpath:org/jtransfo/spring/jTransfoContext.xml")
@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

    private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
    private static final String DISPATCHER_SERVLET_MAPPING = "/";

    private final RealmDsSettings realmDsSettings;

    private final LoginConfigSettings loginConfigSettings;

    private final DataSourcesSettings dataSourcesSettings;

    private final ElasticSearchSettings elasticSearchSettings;

    private static final Set<String> noJWT;

    static {
        noJWT = new HashSet<>();

        noJWT.add("/services11/**");
        noJWT.add("/services/**");
        noJWT.add("/atom11/**");
        noJWT.add("/atom/**");
        noJWT.add("/monitoring/**");
        noJWT.add("/v2/**");
    }

    @Autowired
    public WebApplication(RealmDsSettings realmDsSettings, LoginConfigSettings loginConfigSettings,
                          DataSourcesSettings dataSourcesSettings, ElasticSearchSettings elasticSearchSettings) {
        this.realmDsSettings = realmDsSettings;
        this.loginConfigSettings = loginConfigSettings;
        this.dataSourcesSettings = dataSourcesSettings;
        this.elasticSearchSettings = elasticSearchSettings;

    }


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplication.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApplication.class, args);
    }


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(WebApplication.class);

        ServletRegistration.Dynamic dispatcher = servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(rootContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping(DISPATCHER_SERVLET_MAPPING);

        EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);

        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        FilterRegistration.Dynamic characterEncoding = servletContext.addFilter("characterEncoding", characterEncodingFilter);
        characterEncoding.addMappingForUrlPatterns(dispatcherTypes, true, "/*");

        servletContext.addListener(new ContextLoaderListener(rootContext));
    }


    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory() {

        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {


                LOGGER.trace("entering postProcessContext(realmDsSettings={}, dataSourcesSettings ={}," +
                        " elasticSearchSettings={})",
                        realmDsSettings, dataSourcesSettings, elasticSearchSettings);
                ArrayList<String> datasources = new ArrayList<String>();
                for (DataSourcesSettings.Datasource dataSourceSettings : dataSourcesSettings.getDatasources()) {
                    context.getNamingResources().addResource(PostProcessContextHelper.buildDSContextResource(dataSourceSettings));
                    datasources.add(dataSourceSettings.getName());
                }
                DatasourceSettingsBean.INSTANCE.initSettings(datasources);
                		
                context.setRealm(PostProcessContextHelper.buildDataSourceRealm(realmDsSettings));


                context.addConstraint(PostProcessContextHelper.buildSecurityConstraint(context, loginConfigSettings));

//                todo move elastic search to separate microservices
                ElasticsearchSettingsBean.INSTANCE.initSettings(elasticSearchSettings.getHost(), elasticSearchSettings.getPort(),
                        elasticSearchSettings.getClusterName(), elasticSearchSettings.getClientTransportSniff());

                LOGGER.trace("leaving postProcessContext(): context={}", context.getRealm());
            }

        };

    }

    @Bean
    public MyCmisWebServicesServlet_10 cmis10WebServiceServlet(){
        return new MyCmisWebServicesServlet_10();
    }

    @Bean
    public ServletRegistrationBean cmisws10() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis10WebServiceServlet(), "/services/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.0");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public MyCmisWebServicesServlet cmis11WebServiceServlet(){
        return new MyCmisWebServicesServlet();
    }

    @Bean
    public ServletRegistrationBean cmisws11() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis11WebServiceServlet(), "/services11/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.1");
        registration.setInitParameters(params);
        return registration;
    }


    @Bean
    public MyCmisAtomPubServlet_10 cmis10AtomPubServlet(){
        return new MyCmisAtomPubServlet_10();
    }

    @Bean
    public ServletRegistrationBean cmisatom10() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis10AtomPubServlet(), "/atom/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.0");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public MyCmisAtomPubServlet cmis11AtomPubServlet(){
        return new MyCmisAtomPubServlet();
    }

    @Bean
    public ServletRegistrationBean cmisatom11() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis11AtomPubServlet(), "/atom11/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.1");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public MyCmisBrowserBindingServlet cmisbrowserServlet(){
        return new MyCmisBrowserBindingServlet();
    }

    @Bean
    public ServletRegistrationBean cmisbrowser() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmisbrowserServlet(), "/browser/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        registration.setInitParameters(params);
        return registration;
    }


    @Bean
    public SimpleCmisEndpointsDocumentServlet simpleCmisEndpointsDocumentServlet() {
        return new SimpleCmisEndpointsDocumentServlet();
    }


    @Bean
    public ServletRegistrationBean cmisendpoints() {
        ServletRegistrationBean registration = new ServletRegistrationBean(simpleCmisEndpointsDocumentServlet(), "/cmis-endpoints.json");
        Map<String,String> params = new HashMap<>();
        params.put("template","/cmis-endpoints.json");
        registration.setInitParameters(params);
        registration.setLoadOnStartup(3);

        LOGGER.info("cmisendpoints registered");
        return registration;
    }

    /**
     * <filter>
     <filter-name>javamelody</filter-name>
     <filter-class>net.bull.javamelody.MonitoringFilter</filter-class>
     <async-supported>true</async-supported>
     </filter>
     <filter-mapping>
     <filter-name>javamelody</filter-name>
     <url-pattern>/*</url-pattern>
     <dispatcher>REQUEST</dispatcher>
     <dispatcher>ASYNC</dispatcher>
     </filter-mapping>
     <listener>
     <listener-class>net.bull.javamelody.SessionListener</listener-class>
     </listener>
     * @return
     */

    @Bean
    public FilterRegistrationBean javamelody() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(javamelodyFilter());
        registration.addUrlPatterns("/*");
        registration.setName("javamelody");
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
//        registration.setOrder(1);
        return registration;
    }

    @Bean
    public Filter javamelodyFilter() {
        return new MonitoringFilter();
    }
//
//    @Bean
//    public FilterRegistrationBean hiddenHttpMethodFilterRegistration() {
//
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(hiddenHttpMethodFilter());
//        registration.addUrlPatterns("/updatecontent/*");
//        registration.setName("hiddenHttpMethodFilter");
//        registration.setOrder(1);
//        return registration;
//    }

//    @Bean
//    public Filter hiddenHttpMethodFilter() {
//        return new HiddenHttpMethodFilter();
//    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        private final DataSource dataSource;

        private final BCryptPasswordEncoder passwordEncoder;

        private final UserService userService;

        private final LdapsSettings ldapsSettings;

        @Autowired
        public ApplicationSecurity(DataSource dataSource, BCryptPasswordEncoder passwordEncoder, UserService userService, LdapsSettings ldapsSettings) {
            this.dataSource = dataSource;
            this.passwordEncoder = passwordEncoder;
            this.userService = userService;
//            this.ldapSettings = ldapSettings;
            this.ldapsSettings = ldapsSettings;
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception {
            LOGGER.trace("configure(): http: {}", http);


            http.authorizeRequests().antMatchers("/css/**").permitAll()
                    .antMatchers("/").permitAll()
                    .antMatchers(HttpMethod.POST, "/login/*").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/token/*").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and().antMatcher("/**").httpBasic()
//                    .and()
//                        .logout()
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll()
                    .and()
                    // We filter the api/login requests
                    .addFilterAfter(new JWTLoginFilter("/login", authenticationManager(), userService),
                            UsernamePasswordAuthenticationFilter.class)
                    // And filter other requests to check the presence of JWT in header
                    .addFilterAfter(new JWTAuthenticationFilter(noJWT),
                            UsernamePasswordAuthenticationFilter.class);
            http.csrf().disable();
        }


        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {

            LOGGER.info("configure(ldapSettings = {})", ldapsSettings);
            auth.jdbcAuthentication()
                        .dataSource(this.dataSource)
                        .authoritiesByUsernameQuery(getAuthoritiesQuery())
                        .passwordEncoder(passwordEncoder);

            if(ldapsSettings != null && ldapsSettings.getLdaps() != null) {
                for(LdapsSettings.Ldap ldap : ldapsSettings.getLdaps()) {

                    auth.ldapAuthentication()
                        .userSearchBase(ldap.getUserSearchBase())
                        .userSearchFilter(ldap.getUserSearchFilter())
                        .groupSearchBase(ldap.getGroupSearchBase())
                        .contextSource()
                        .url(ldap.getUrl())
                        .managerDn(ldap.getManagerDn())
                        .managerPassword(ldap.getManagerPassword())
                    .and()
                        .passwordCompare()
                        .passwordEncoder(new LdapShaPasswordEncoder())
                        .passwordAttribute(ldap.getPasswordAttribute());

                }
            }
        }

        private String getAuthoritiesQuery() {
            return "select u.username,r.role from users u inner join user_roles ur on(u.userid=ur.userid) " +
                    "inner join roles r on(ur.role=r.role)  where u.username=?";
        }
    }

    @Configuration
    @EnableCaching
    public static class CachingConfiguration extends CachingConfigurerSupport {

        private final HazelcastSettings hazelcastSettings;

        private static final Logger LOGGER = LoggerFactory.getLogger(CachingConfiguration.class);

        @Autowired
        public CachingConfiguration(HazelcastSettings hazelcastSettings) {
            LOGGER.trace("CachingConfiguration(): hazelcastSettings \n {}", hazelcastSettings);
            this.hazelcastSettings = hazelcastSettings;
        }

        @Bean(name = "springCM")
        @Override
        public CacheManager cacheManager() {
            ClientConfig config = new ClientConfig();
            ClientNetworkConfig networkConfig = config.getNetworkConfig();

            LOGGER.trace("cacheManager(): hazelcastSettings \n {}", hazelcastSettings);
            networkConfig.addAddress(hazelcastSettings.getAddress());
            networkConfig.setConnectionAttemptLimit(Integer.parseInt(hazelcastSettings.getСonnectionAttemptLimit()));
            networkConfig.setConnectionAttemptPeriod(Integer.parseInt(hazelcastSettings.getСonnectionAttemptPeriod()));
            networkConfig.setConnectionTimeout(Integer.parseInt(hazelcastSettings.getСonnectionTimeout()));
            try {
                HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
                LOGGER.trace("cacheManager(): hazelcastClient \n {}", client);
                return new HazelcastCacheManager(client);
            }catch (IllegalStateException e) {
                LOGGER.trace("cacheManager(): hazelcast is not available");
                final SimpleCacheManager cacheManager = new SimpleCacheManager();
                cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("docsByType"),
                        new ConcurrentMapCache("userByLoginAndPwd"),
                        new ConcurrentMapCache("countByType"),
                        new ConcurrentMapCache("userByLogin")));
                return cacheManager;
            }
        }
        @Bean
        @Override
        public KeyGenerator keyGenerator() {
            return null;
        }
        @Override
        public CacheErrorHandler errorHandler() {
             return new CacheErrorHandler() {
                    @Override
                    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                        LOGGER.trace("cache get error");
                    }

                    @Override
                    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                        LOGGER.trace("cache put error");
                    }

                    @Override
                    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                        LOGGER.trace("cache evict error");
                    }

                    @Override
                    public void handleCacheClearError(RuntimeException exception, Cache cache) {
                        LOGGER.trace("cache clear error");
                    }
             };
        }

    }
}