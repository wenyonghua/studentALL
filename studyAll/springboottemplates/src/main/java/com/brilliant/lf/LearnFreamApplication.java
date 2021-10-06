package com.brilliant.lf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LearnFreamApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(LearnFreamApplication.class, args);
    }

//    @Bean
//    public InternalResourceViewResolver setupViewResolver(){
//        InternalResourceViewResolver resolver =new InternalResourceViewResolver();
//        resolver.setPrefix("");
//        resolver.setSuffix(".jsp");
//        return resolver;
//    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return super.configure(builder);
    }

}
