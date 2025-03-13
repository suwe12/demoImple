<h2 id="UljAQ">resp协议格式有以下五种数据类型：</h2>
类型一：`*` 数组类型，其后面格式为$字节\r\n内容\r\n

类型二：`+`正确响应

类型三：`-Error`错误响应

类型四：`:`返回值

类型五：`$`多行字符串

```java
*3\r\n$3\r\nset\r\n$4\r\nname\r\n$6\r\n你好\r\n

+ok\r\n
-Error msg\r\n
:10\r\n
$5\r\nhello\r\n
$6\r\nhe\nllo\r\n
```



---

<h2 id="Gd75m">处理二进制安全问题</h2>
在多行字符串$中，需要处理二进制安全，例如`$6\r\nhe\nllo\r\n`需要读取`he\nllo`

同时需要循环读取尽可能多字符，因为在实际中<font style="color:#080808;background-color:#ffffff;">数据在网络中可能分块到达，可能不会一次性读取</font>

既体现在代码中：

```java
int total = 0;
while ( total < len ) {
    int count = reader.read(buff, total, len - total);
    if ( count == -1 ) throw new IOException("读取错误");
    total += count;
}
//跳过结尾的\r\n,让指针移动
String nu = reader.readLine();
```





<h2 id="fkYCY">运行</h2>
用户点击运行，在控制台输入命令即可，注意需要运行redis.server

![](https://cdn.nlark.com/yuque/0/2025/png/40910320/1741870854291-a22308a5-49a1-4f66-a0e1-4e9a163a007f.png)

