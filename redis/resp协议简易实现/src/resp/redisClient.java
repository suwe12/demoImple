package resp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class redisClient {

	static Socket socket;
	static BufferedReader reader; //输入
	static PrintWriter writer;//输出
	static Scanner sc;


	public static void main(String[] args) throws IOException {

		//建立链接,向redis服务器发起连接
		try {

			int port = 6379;
			String host = "127.0.0.1";
			socket = new Socket(host, port);
			System.out.println("[日志]:redis服务器已连接");

			//若设置密码请先发送请求密码连接


			//获取输入流输出流
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); //可以一次性读取一行
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			sc = new Scanner(System.in);
			//发送请求

			System.out.println("[日志]:已获取流资源");
			System.out.print("请输入指令:");
			String[] split = sc.nextLine().split(" ");
			//sendRequest("get", "te4");
			sendRequest(split);
			//sendRequest("set", "te4", "suuuu\njjjj");
			System.out.println("[日志]:已发送请求");


			//处理响应
			Object o = handleResponse();
			System.out.println("[日志]:响应成功");
			System.out.println(o);

		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			System.out.flush(); // 强制刷新输出缓冲区
			if ( socket != null ) socket.close();
			if ( reader != null ) reader.close();
			if ( writer != null ) writer.close();
		}
	}

	//处理请求
	private static Object handleResponse() throws IOException {

		int read = reader.read();
		switch ( read ) {
			case '+':
				return reader.readLine();
			case '-':
				return "Error " + reader.readLine();
			case ':':
				return Long.parseLong(reader.readLine());
			case '$':
				int len = Integer.parseInt(reader.readLine());
				if ( len == -1 ) {
					return null;
				} else if ( len == 0 ) {
					return "";
				} else {  //要确保二进制安全

					System.out.println("[日志]:多行字符串长度为:" + len);
					char[] buff = new char[len];
//					reader.read(buff, 0, len);
					//数据在网络中可能分块到达，可能不会一次性读取
					int total = 0;
					while ( total < len ) {
						int count = reader.read(buff, total, len - total);
						if ( count == -1 ) throw new IOException("读取错误");
						total += count;
					}
					//跳过结尾的\r\n,让指针移动
					String nu = reader.readLine();
					return new String(buff);
				}
			case '*':
				return readArray();
			default:
				throw new RuntimeException("格式错误");

		}

	}

	//读取数组类型
	private static Object readArray() throws IOException {
		int len = Integer.parseInt(reader.readLine());
		if ( len <= 0 ) {
			return null;
		}
		ArrayList<Object> arr = new ArrayList<>();
		for ( int i = 0; i < len; i++ ) {
			Object o = handleResponse();
			arr.add(o);
		}
		return arr;
	}

	//发送请求
	private static void sendRequest(String... args) {

		writer.write("*" + args.length + "\r\n");
		for ( String a : args ) {
			writer.write("$" + a.getBytes(StandardCharsets.UTF_8).length + "\r\n");
			writer.write(a + "\r\n");
		}
		writer.flush();
	}
}
