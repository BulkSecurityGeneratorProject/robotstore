package com.rbc.ace.config;

import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.web.filter.CachingHttpHeadersFilter;
import org.h2.server.web.WebServlet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.*;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the WebConfigurer class.
 *
 * @see WebConfigurer
 */
public class WebConfigurerTest {

    private WebConfigurer webConfigurer;

    private MockServletContext servletContext;

    private MockEnvironment env;

    private JHipsterProperties props;

    @Before
    public void setup() {
        servletContext = spy(new MockServletContext());
        doReturn(new MockFilterRegistration())
            .when(servletContext).addFilter(anyString(), any(Filter.class));
        doReturn(new MockServletRegistration())
            .when(servletContext).addServlet(anyString(), any(Servlet.class));

        env = new MockEnvironment();
        props = new JHipsterProperties();

        webConfigurer = new WebConfigurer(env, props);
    }

    @Test
    public void testStartUpProdServletContext() throws ServletException {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        webConfigurer.onStartup(servletContext);
        verify(servletContext).addFilter(eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter.class));
    }

    @Test
    public void testStartUpDevServletContext() throws ServletException {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT);
        webConfigurer.onStartup(servletContext);
        verify(servletContext, never()).addFilter(eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter.class));
        verify(servletContext).addServlet(eq("H2Console"), any(WebServlet.class));
    }

    @Test
    public void testCorsFilterOnApiPath() throws Exception {
        props.getCors().setAllowedOrigins(Collections.singletonList("*"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            options("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
            .andExpect(header().string(HttpHeaders.VARY, "Origin"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800"));

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"));
    }

    @Test
    public void testCorsFilterOnOtherPath() throws Exception {
        props.getCors().setAllowedOrigins(Collections.singletonList("*"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/test/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void testCorsFilterDeactivated() throws Exception {
        props.getCors().setAllowedOrigins(null);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void testCorsFilterDeactivated2() throws Exception {
        props.getCors().setAllowedOrigins(new ArrayList<>());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    static class MockFilterRegistration implements FilterRegistration, FilterRegistration.Dynamic {

        @Override
        public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

        }

        @Override
        public Collection<String> getServletNameMappings() {
            return null;
        }

        @Override
        public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {

        }

        @Override
        public Collection<String> getUrlPatternMappings() {
            return null;
        }

        @Override
        public void setAsyncSupported(boolean isAsyncSupported) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Set<String> setInitParameters(Map<String, String> initParameters) {
            return null;
        }

        @Override
        public Map<String, String> getInitParameters() {
            return null;
        }
    }

    static class MockServletRegistration implements ServletRegistration, ServletRegistration.Dynamic {

        @Override
        public void setLoadOnStartup(int loadOnStartup) {

        }

        @Override
        public Set<String> setServletSecurity(ServletSecurityElement constraint) {
            return null;
        }

        @Override
        public void setMultipartConfig(MultipartConfigElement multipartConfig) {

        }

        @Override
        public void setRunAsRole(String roleName) {

        }

        @Override
        public void setAsyncSupported(boolean isAsyncSupported) {

        }

        @Override
        public Set<String> addMapping(String... urlPatterns) {
            return null;
        }

        @Override
        public Collection<String> getMappings() {
            return null;
        }

        @Override
        public String getRunAsRole() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Set<String> setInitParameters(Map<String, String> initParameters) {
            return null;
        }

        @Override
        public Map<String, String> getInitParameters() {
            return null;
        }
    }

}