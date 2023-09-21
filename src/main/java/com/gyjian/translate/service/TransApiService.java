package com.gyjian.translate.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransApiService {
    private static final String TRANS_API_HOST = "https://fanyi-api.baidu.com/api/trans/vip/translate?q={q}&from={from}&to={to}&appid={appid}&salt={salt}&sign={sign}";

    @Autowired
    RestTemplate restTemplate;

    private String appid;
    private String securityKey;

    public void init(String appid, String securityKey) {
        this.appid = appid;
        this.securityKey = securityKey;
    }

    public String getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);

        return restTemplate.getForObject(TRANS_API_HOST, String.class, params);
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", DigestUtils.md5Hex(src));

        return params;
    }

}
