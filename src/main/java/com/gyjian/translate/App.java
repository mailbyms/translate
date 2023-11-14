package com.gyjian.translate;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gyjian.translate.service.TransApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SpringBootApplication
public class App implements CommandLineRunner {
    // 在平台申请的APP_ID 详见
    // http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    private static final String APP_ID = "20190709000316278";
    private static final String SECURITY_KEY = "XODK2pODq31WwXHujurS";

    @Autowired
    private TransApiService apiService;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            log.info("Usage:java -jar translate.jar xxx.srt");
            return;
        }
        String inFile = args[0];
        //String inFile = "d:\\Downloads\\Poker.Face.2023.S01E01.Dead.Mans.Hand.REPACK.1080p.STAN.WEB-DL.DDP5.1.H.264-NTb.srt";

        log.info("input srt file:{}", inFile);

        int dotIndex = inFile.lastIndexOf('.'); // 获取最后一个点的位置
        String nameWithoutExt = inFile.substring(0, dotIndex); // 获取没有后缀的文件名

        String outFile = nameWithoutExt + ".deeplx.srt";
        try (InputStream is = new FileInputStream(inFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             FileOutputStream fileOutputStream = new FileOutputStream(outFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            String reg = "^(\\d)*:(\\d)*"; // 用来匹配时间轴格式，如 00:00:14,000 --> 00:00:17,760
            Pattern pattern = Pattern.compile(reg);

            String reg2 = "<[^>]+>";        // 用来匹配某些 srt如下xml格式，<font color="#ffffff">lays the foundation</font>
            Pattern pattern2 = Pattern.compile(reg2);

            StringBuilder lyric = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                str = str.trim();

                Matcher matcher2 = pattern2.matcher(str);
                if (matcher2.find()) {
                    str = matcher2.replaceAll("");    // 去掉 xml 格式 <>
                }

                log.info(str);

                Matcher matcher = pattern.matcher(str);

                if ((str.length() < 5) || matcher.find()) {
                    // 如果有积聚英文原句，刚翻译并写到文件里
                    if (!lyric.isEmpty()) {

                        String query = apiService.getTransResult(lyric.toString(), "en", "zh");

                        JSONObject jo = JSON.parseObject(query);
                        Integer code = jo.getInteger("code");
                        if (code == null || code != 200) {
                            log.warn("翻译失败，接口结果为：{}", query);
                            continue;
                        }

                        // Convert from Unicode to UTF-8
                        String string = jo.getString("data");
                        byte[] utf8 = string.getBytes(StandardCharsets.UTF_8);
                        // Convert from UTF-8 to Unicode
                        string = new String(utf8, StandardCharsets.UTF_8);

                        log.info(string);
                        bufferedOutputStream.write(utf8);
                        bufferedOutputStream.write('\n');
                        bufferedOutputStream.write(lyric.toString().getBytes());
                        bufferedOutputStream.write('\n');
                        bufferedOutputStream.flush();

                        lyric.setLength(0);            // 翻译完，复位到空串

                        Thread.sleep(20); // 等待1秒钟
                    }

                    // 把原文写到文件
                    bufferedOutputStream.write(str.getBytes());
                    bufferedOutputStream.write('\n');
                    bufferedOutputStream.flush();
                } else {                        // 读到一句英文句子，先存起来
                    lyric.append(str);
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("InterruptedException", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Other Exception", e);
        }

        log.info("翻译完成");
    }
}
