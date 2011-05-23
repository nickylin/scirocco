package com.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class HTTPUtil {
	private static String generateBoundary() {
		String chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
		Random rand = new Random();
		String boundary = "";
		for (int i = 0; i < 40; i++) {
			int r = rand.nextInt(chars.length());
			boundary += chars.substring(r, r + 1);
		}
		return boundary;
	}

	private static void doOutput(String boundary, OutputStream out,
			Map<String, String> parameters , File file) throws IOException {
//		String charset = "Shift_JIS";
		String charset = "Shift_JIS";
		// テキストフィールド送信
		for (Entry<String, String> entry : parameters.entrySet()) {
			if (entry != null && entry.getValue() instanceof String) {
				out.write(("--" + boundary + "\r\n").getBytes(charset));
				out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n")
						.getBytes(charset));
				out.write(("Content-Type: text/plain; charset=Shift_JIS\r\n\r\n")
						.getBytes(charset));
				out.write((entry.getValue()).getBytes(charset));
				out.write(("\r\n").getBytes(charset));
			}
		}
		
		// ファイルフィールド送信
		out.write(("--" + boundary + "\r\n").getBytes(charset));
		out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"")
				.getBytes(charset));
		out.write((file.getName()).getBytes(charset));
		out.write(("\"\r\n").getBytes(charset));
		out.write(("Content-Type: application/octet-stream\r\n\r\n")
				.getBytes(charset));
		InputStream in = new FileInputStream(file);
		byte[] bytes = new byte[1024];
		while (true) {
			int ret = in.read(bytes);
			// if(ret == 0) break;
			if (ret == -1)
				break;

			out.write(bytes, 0, ret);
			out.flush();
		}
		out.write(("\r\n").getBytes(charset));
		in.close();

		// 送信終わり
		out.write(("--" + boundary + "--").getBytes(charset));
	}

	public static void postUploadFile(String upload_cgi_url ,String uploadFilePath,Map<String, String> parameters ) {
		String boundary = generateBoundary();
		File file = new File(uploadFilePath);

		// ダミーに書き込んで、データ量を調べる
		DummyOutputStream dummy = new DummyOutputStream();
		try {
			doOutput(boundary, dummy, parameters, file);
			int contentLength = dummy.getSize();

			// 接続
			URL url = new URL(upload_cgi_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setFixedLengthStreamingMode(contentLength);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
			conn.connect();

			// 実際にデータを送信する
			OutputStream out = conn.getOutputStream();
			doOutput(boundary, out, parameters, file);
			out.flush();
			out.close();

			// レスポンスを受信 (これをやらないと通信が完了しない)
			InputStream stream = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream));
			String responseData = null;
			while ((responseData = reader.readLine()) != null) {
				System.out.print(responseData);
			}
			stream.close();
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
