package com.gyjian.translate;

import com.gyjian.translate.service.AzureTransApiService;
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
    // 在平台申请的APP_ID 详见 Azure 控制台，服务下的"密钥和终结点"，密钥1 和 位置/区域
    private static final String SUBSCRIPTION_REGION = "eastasia";
    private static final String SUBSCRIPTION_KEY = "e1346fac1a364dfe891c3575fabcfe36";

    @Autowired
    private AzureTransApiService apiService;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            log.info("Usage:java -jar translate.jar xxx.srt");
            //return;
        }
        //String inFile = args[0];
        String inFile = "D:\\Downloads\\Poker.Face.2023.S01E01.Dead.Mans.Hand.REPACK.1080p.STAN.WEB-DL.DDP5.1.H.264-NTb.srt";

        log.info("input srt file:{}", inFile);

        int dotIndex = inFile.lastIndexOf('.'); // 获取最后一个点的位置
        String nameWithoutExt = inFile.substring(0, dotIndex); // 获取没有后缀的文件名

        String outFile = nameWithoutExt + ".azure.srt";
        try (
                InputStream is = new FileInputStream(inFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
        ) {
            String reg = "^([0-9])*:([0-9])*"; // 用来匹配时间轴格式，如 00:00:14,000 --> 00:00:17,760
            Pattern pattern = Pattern.compile(reg);

            String reg2 = "<[^>]+>";        // 用来匹配某些 srt如下xml格式，<font color="#ffffff">lays the foundation</font>
            Pattern pattern2 = Pattern.compile(reg2);

            String lyric = "";
            String str;
            apiService.init(SUBSCRIPTION_REGION, SUBSCRIPTION_KEY);
            while (true) {
                str = reader.readLine();
                if (str != null) {
                    Matcher matcher2 = pattern2.matcher(str);
                    if (matcher2.find()) {
                        str = matcher2.replaceAll("");    // 去掉 xml 格式 <>
                    }

                    log.info(str);

                    Matcher matcher = pattern.matcher(str);

                    if ((str.length() < 5) || matcher.find()) {
                        // 如果有积聚英文原句，刚翻译并写到文件里
                        if (lyric.length() > 0) {
                            try {
                                String query = lyric;
                                String result = apiService.getTransResult(query);
                                log.info(result);

                                bufferedOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
                                bufferedOutputStream.write('\n');
                                bufferedOutputStream.write(lyric.getBytes());
                                bufferedOutputStream.write('\n');

                                bufferedOutputStream.flush();

                                //Thread.sleep(20);

                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Exception", e);
                                break;
                            }

                            lyric = "";            // 翻译完，复位到空串
                        }

                        // 把原文写到文件
                        bufferedOutputStream.write(str.getBytes());
                        bufferedOutputStream.write('\n');
                        bufferedOutputStream.flush();
                    } else {                        // 读到一句英文句子，先存起来
                        lyric += " " + str;
                        lyric = lyric.trim();
                    }
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception", e);
        }

        log.info("翻译完成");
    }
}
