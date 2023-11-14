package com.gyjian.translate.service;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransApiService {
    private static final String TRANS_API_HOST = "http://192.168.2.69:1188/translate";

    @Autowired
    RestTemplate restTemplate;

    public String getTransResult(String query, String from, String to) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", query);
        jsonObject.put("source_lang", "EN");
        jsonObject.put("target_lang", "ZH");

        return restTemplate.postForObject(TRANS_API_HOST, jsonObject.toJSONString(), String.class);
    }

}
