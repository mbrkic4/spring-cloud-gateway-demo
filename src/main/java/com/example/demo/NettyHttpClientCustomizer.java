package com.example.demo;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;

import reactor.netty.http.client.HttpClient;

public class NettyHttpClientCustomizer implements HttpClientCustomizer {
	
	@Override
	public HttpClient customize(HttpClient httpClient) {

		return httpClient.followRedirect(true);
	}
}