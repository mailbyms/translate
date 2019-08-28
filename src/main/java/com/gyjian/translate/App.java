package com.gyjian.translate;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Hello world!
 *
 */

import com.baidu.translate.demo.TransApi;

public class App {
	// 在平台申请的APP_ID 详见
	// http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
	private static final String APP_ID = "20180515000160090";
	private static final String SECURITY_KEY = "kLvyAJ0f6gbOFDhv1hes";

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage:java -jar translate.jar xxx.srt");
			return;
		}
		String inFile = args[0]; //"/Users/jane/Downloads/a.srt";
		System.out.println("input srt file:" + inFile);
		
		String outFile = inFile.replaceAll("\\.srt", "\\.baidu.srt");

		FileOutputStream outSTr = null;
		BufferedOutputStream Buff = null;

		try {
			InputStream is = new FileInputStream(inFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			outSTr = new FileOutputStream(new File(outFile));
			Buff = new BufferedOutputStream(outSTr);
			String reg = "^([0-9])*:([0-9])*"; // 用来匹配时间轴格式，如 00:00:14,000 --> 00:00:17,760
			Pattern pattern = Pattern.compile(reg);
			
			String reg2 = "<[^>]+>";		// 用来匹配某些 srt如下xml格式，<font color="#ffffff">lays the foundation</font>
			Pattern pattern2 = Pattern.compile(reg2);

			String lyric = "";
			String str = null;
			while (true) {
				str = reader.readLine();
				if (str != null) {
					Matcher matcher2 = pattern2.matcher(str);
					if (matcher2.find()) {
						str = matcher2.replaceAll("");	// 去掉 xml 格式 <>
					}
					
					System.out.println(str);

					Matcher matcher = pattern.matcher(str);

					if ((str.length() < 5) || matcher.find()) {
						// 如果有积聚英文原句，刚翻译并写到文件里
						if (lyric.length() > 0) {
							try {
								TransApi api = new TransApi(APP_ID, SECURITY_KEY);

								String query = lyric;
								query = (api.getTransResult(query, "en", "zh"));
								
								JSONObject jo = JSONObject.parseObject(query);
								JSONArray jArray = jo.getJSONArray("trans_result");
								if(jArray == null) {
									System.out.println("翻译失败，接口结果为：" + query);
									int errno = jo.getIntValue("error_code");
									if (errno == 54003) {
										
									}
								}
								
								JSONObject jo2 = (JSONObject) jArray.get(0);

								// Convert from Unicode to UTF-8
								String string = jo2.getString("dst");
								byte[] utf8 = string.getBytes("UTF-8");
								// Convert from UTF-8 to Unicode
								string = new String(utf8, "UTF-8");

								System.out.println(string);
								Buff.write(utf8);
								Buff.write('\n');
								Buff.write(lyric.getBytes());
								Buff.write('\n');

								Buff.flush();
								
								Thread.sleep(1000);
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							lyric = "";			// 翻译完，复位到空串
						}
												
						
						// 把原文写到文件
						Buff.write(str.getBytes());
						Buff.write('\n');
						Buff.flush();
					} else {						// 读到一句英文句子，先存起来
						lyric += " " + str;
						lyric = lyric.trim();
					}
				}

				else
					break;
			}

			is.close();

			Buff.close();
			outSTr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("完成");
	}
}
