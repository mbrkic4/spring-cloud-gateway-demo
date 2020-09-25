package com.example.demo;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
@RestController
public class DemoApplication {

	private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

	@Value("${server.port}")
	int port;

	@Bean
	public NettyHttpClientCustomizer nettyHttpClientCustomizer() {

		return new NettyHttpClientCustomizer();
	}

	@PostMapping("/post")
	public ResponseEntity<String> post(@RequestBody String body) {
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@PostMapping("/redirect")
	public ResponseEntity<String> redirect() {
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create("http://localhost:" + port + "/post"));
		return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		LOG.info("direct post: {}", test("/post"));
		LOG.info("direct post following redirect: {}", test("/redirect"));
		LOG.info("post via gateway: {}", test("/proxy/post"));
		LOG.info("post via gateway following redirect: {}", test("/proxy/redirect"));
		
	}
	
	private String test(String uri) {
		return HttpClient.create()
				.followRedirect(true)
				.post()
				.uri("http://localhost:" + port + uri)
				.send(ByteBufFlux.fromString(Mono.just("successful")))
				.responseContent() 
		        .aggregate()       
		        .asString()
				.block();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
