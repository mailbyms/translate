package com.gyjian.translate.service;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

        //RestTemplate设置编码
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        //  创建请求配置信息
        RequestConfig  requestConfig = RequestConfig.custom()
                // 设置连接超时时间
                .setConnectTimeout(Timeout.of(5000, TimeUnit.MILLISECONDS))
                // 设置响应超时时间
                .setResponseTimeout(5000, TimeUnit.MILLISECONDS)
                // 设置从连接池获取链接的超时时间
                .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                .build();

        // 创建 CloseableHttpClient 对象
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 创建 factory
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);

        return factory;
    }
}