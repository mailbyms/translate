package com.gyjian.translate.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 微软 Azure 翻译接口封闭
 */
@Service
@Data
@Slf4j
public class AzureTransApiService {
    private static final String TRANS_API_HOST = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=zh-Hans";

    @Autowired
    RestTemplate restTemplate;

    private String subscriptionRegion;
    private String subscriptionKey;

    public void init(String region, String key) {
        this.subscriptionRegion = region;
        this.subscriptionKey = key;
    }

    public String getTransResult(String query) throws UnsupportedEncodingException {
        // 设置请求头信息
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
        headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));
        headers.add("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.add("Ocp-Apim-Subscription-Region", subscriptionRegion);

        // 设置请求体内容
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", query);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        // 创建 HttpEntity 对象
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonArray.toString(), headers);


        ResponseEntity<String> response = restTemplate.exchange(TRANS_API_HOST, HttpMethod.POST, requestEntity, String.class);

        String content = response.getBody();
        // 去掉 BOM
        if(content != null && content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        return content;
    }



}
