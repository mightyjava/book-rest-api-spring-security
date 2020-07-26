package com.mightyjava.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import com.mightyjava.service.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic()
				.authenticationEntryPoint(entryPoint()).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Bean
	public AuthenticationEntryPoint entryPoint() {
		return new BasicAuthenticationEntryPoint() {
			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) throws IOException, ServletException {
				JSONObject jsonObject = new JSONObject();
				try {
					response.addHeader("WWW-Authenticate", "Basic Realm - " + getRealmName());
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json");
					response.getWriter()
							.println(jsonObject.put("exception", "HTTP Status 401 - " + authException.getMessage()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void afterPropertiesSet() throws Exception {
				setRealmName("almightyjava");
				super.afterPropertiesSet();
			}
		};
	}
}
