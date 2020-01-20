package com.viktority.trials;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	Contact contact = new Contact("Chukwuka Onyekachukwu Victor", "github.com/viktority", "viktority@yahoo.com");

	// List<VendorExtension> vendorExtensions = new ArrayList<>();

	ApiInfo apiInfo = new ApiInfo("Authentication Service",
			"This pages documents my authentication service. from creating of account to login to authorization", "1.0",
			"", contact, "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", new ArrayList<>());

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				// .protocols(new HashSet<>(Arrays.asList("HTTPs", "HTTP")))
				.apiInfo(apiInfo).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
	}

}
