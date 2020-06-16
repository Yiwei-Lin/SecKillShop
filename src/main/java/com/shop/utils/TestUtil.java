package com.shop.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shop.entity.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestUtil {
	
	private static void createUser(int count) throws Exception{
		System.out.println("started");
		List<User> users = new ArrayList<>(count);
		//生成用户
		for(int i=0;i<count;i++) {
			User user = new User();
			user.setId(13000000000L+i);
			user.setLoginCount(1);
			user.setNickname("user"+i);
			user.setRegisterDate(new Date());
			user.setSalt("mysalt");
			user.setPassword("5aedf3bb9fa6497f4ade2430cc9cd4a6");
			users.add(user);
		}
		//登录，生成token
		String urlString = "http://localhost:8080/login_for_test/";
		File file = new File("D:/tokens.txt");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(0);
		for (User user : users) {
			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection) url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();
			String params = "mobile=" + user.getId() + "&password=" + MD5Util.md5WithFixedSalt("123456");
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(buff)) >= 0) {
				bout.write(buff, 0, len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
			JSONObject jo = JSON.parseObject(response);
			String token = jo.getString("data");

			String row = user.getId() + "," + token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
		}
		raf.close();
		
		System.out.println("finished");
	}
	
	public static void main(String[] args)throws Exception {
		createUser(5000);
	}
}
