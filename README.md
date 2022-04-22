# Liang Tomcat

@[TOC](MyTomcat)
# 介绍
- 程序运行在Linux下，且会用到较多linux指令进行进程、网络相关的分析
- 代码开源在我的GitHub项目[LiangTomcat](https://github.com/LiangYang666/LiangTomcat)
- 该项目将会先使用传统BIO方式进行服务器的实现，再使用JDK1.4发布的New IO进行升级，最后使用Netty进行实现，多版本都能够进行共存，可选择性调用并进行对比测试分析
# 一、准备工作
## 1.1 Tomcat工作分析
1. Tomcat主要负责监端口
2. 当有客户端连接到端口时，创建新的Socket并进行处理
3. 分析http请求头，将得到的URL与指定的处理函数或资源进行匹配
4. 执行相对应的处理函数或传输相应的资源，返回结果
## 1.2 创建工程
新建工程->一直Next
设置项目名称以及项目位置
![在这里插入图片描述](https://img-blog.csdnimg.cn/1abe967a457847f3aa29c268d61eb50b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
创建包 com.liang
![在这里插入图片描述](https://img-blog.csdnimg.cn/6d57a5e812324c3ba60a8a0b47ea7f95.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)

# 二、简易版编写
## 2.1 监听连接
### 2.1.1 分析
这里我们需要建立一个serverSocket，并绑定端口，阻塞式调用accept方法监听端口等待连接，连接完成后，创建一个新的socket用于通信
### 2.1.2 程序编写
创建一个`com.liang.MyTomcat.java`类，[GitHub链接](https://github.com/LiangYang666/LiangTomcat)
程序如下：

```java
public class MyTomcat {
    public static void main(String[] args) {
        int port = 8099;
        try {
            ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
            System.out.printf("端口%d 等待连接\n", port);
            Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
            System.out.println(clientSocket);
            InputStream inputStream = clientSocket.getInputStream();
            byte[] data = new byte[1024];
            int length = inputStream.read(data);
            String s = new String(data, 0, length);
            System.out.println("+++++++++++++++++HTTP Request+++++++++++++++++++++++++");
            System.out.println(s);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
### 2.1.2 访问与网络状态分析
==这里的状态分析均在Linux系统中进行，非Linux系统可略过==
1. 点击运行程序，使用`jps -l`指令可以查看到java虚拟机运行的所有进程，可以查看到刚运行的进程以及进程号
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/cbe3087181ba422c9b8b52b182c641df.png)
   `lsof -p 397887` 查看所有打开的文件
   `lsof -i :8099`查看端口8099上的网络
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/12a9ec299b78494c8c3fc61d67140a0c.png)
   新建终端，输入`watch -n 0.1 "netstat -anp | grep 8099"`开启一个定时查看窗口，实时查看端口变化
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/1377453735114bf3a138283cf25d34dd.png)
2. 本地浏览器地址栏输入`127.0.0.1:8099/getTest`通过ipv4访问，也可输入`[::1]:8099/getTest`通过ipv6进行访问
   运行窗口将会打印出连接socket以及请求报文的内容，如下图所示，本质http报文就是一堆文本
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/3613965f9d7a4cf28f3fa7663ac170f2.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
3. 这里其实会建立多个连接，可在打印HTTP request后面加上如下代码，等待我们输入回车再关闭
   ```java
   System.out.println("回车以关闭客户端连接socket");
   Scanner sc = new Scanner(System.in);
   sc.nextLine();
   clientSocket.close();
   System.out.println("回车以关闭监听socket");
   sc.nextLine();
   serverSocket.close();
   ```
4. 重新启动监听
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/5f051a2a9fa44b75b35023756b80fce6.png)
5. 浏览器输入地址访问后，其实可以看出建立了两个tcp连接，这是因为chrome预加载加速机制的原因，(如终端通过`curl 127.0.0.1:8099/curlTest`指令访问将只会产生一个连接)
   这里我们使用chrome访问，打印出来的对端端口为59874，因此我们后续关注59874即可
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/9c2707a408be46469b2b9ec79463b72b.png)
   可以看出8099有两个已建立的连接，其中一个59874就是正常连接，59876是预加载相关的不用理会
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/f8c64d70f95340088fb28b447d6523af.png)
6. 程序运行窗口输入一次回车，关闭连接socket后，即8099端口主动与59874端口断开连接，由于59874没有想发送的数据，因此会立即同意关闭并发送FIN包，按照计算机网络的四次挥手这其中的状态变化应该为
   ```bash
   8099->59874: ESTABLISHED->发送FIN->FIN_WAIT1---->FIN_WAIT2···------>发送ACK----->TIME_WAIT-->CLOSED
   59874->8099: ESTABLISHED--->CLOSE_WAIT->发送ACK->CLOSEWAIT···->发送FIN->LAST_ACK->CLOSED
   ```
   然而中间状态变化太快，未能捕捉到，因此看到的59874端口状态为`TIME_WAIT`，2MSL后将会消失
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/0b8d5d8baf4f45cebc01ffbab051ceb9.png)
7. 打开浏览器可以看到标签页正在旋转，这是因为预加载机制的存在
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/682d81b8c2854186a087c96eb37ab660.png)
   当我们点击X进行关闭后，59896端口主动发起断开连接，首先59876->8099将变为FIN_WAIT2，让8099继续发送并等待对方同意断开
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/1b61dc1b9ba942779e667703b613de68.png)
   然而8099并没有东西发送，只是被迫建立了tcp连接(chrome的预加载)，程序并未做相关处理，因此不会发送数据又不会断开连接，因此59896在FIN_WAIT2状态下一直没收到任何数据，那么会强制关闭该socket，因此59896->8099的连接消失了
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/a373a6955c6f400698960f52a61b6f9d.png)
   再次回车后所有8099有关连接都会消失，因为进程退出了
8. 其他方式连接分析
   上面主要是http相关请求，我们也可以使用nc指令进行连接分析
   执行 `nc 127.0.0.1 8099`
   输入 `Test nc`，程序运行窗口将会显示出来
   ctrl+c关闭连接
   可注意观察变化
## 2.2 请求报文解析
### 2.2.1 分析
分析请求报文，我们暂时只分析请求头，2.1节已获取到整个报文，报文头如下所示
```bash
GET /getTest HTTP/1.1
```
### 2.2.1 程序编写
需要解析出url访问地址和请求方法，创建一个`com.liang.Request.java`类，代码如下
```java
public class Request {
    private String method;
    private String url;
    public Request(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length = inputStream.read(bytes);
        String requestStr = new String(bytes, 0, length);
        String requestHead = requestStr.split("\n")[0].trim();
        String[] heads = requestHead.split("\\s+");
        method = heads[0];
        url = heads[1];
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
```
### 2.2.2 报文解析测试
更改MyTomcat代码为如下
```java
public class MyTomcat {
    public static void main(String[] args) {
        int port = 8099;
        try {
            ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
            System.out.printf("端口%d 等待连接\n", port);
            Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
            System.out.println(clientSocket);
            InputStream inputStream = clientSocket.getInputStream();
            Request request = new Request(inputStream);
            System.out.println(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
打印出的结果如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/7ff1edd2ca674c0e91b11efa521ba70a.png)
### 2.2.3 字节码分析
Request类文件较为简单，如了解类文件结构，可使用字节码分析工具`javap -verbose Request.class`查看编译出来的字节码文件
如对字节码进行直接读取查看，可使用 `xxd Request.class`，你会看到字节码的开头标有咖啡的magic
当然使用`od -x `也可以查看字节码，但是是小端打印的，看起来会不习惯

## 2.3 响应报文封装
### 2.3.1 分析
这里我们写一个简单的响应，响应包含响应头和响应体，响应头常见形式可打开chrome浏览器，右键检查，打开一个网页，点击Network，点一个响应，查看Response Heathers的源档
![在这里插入图片描述](https://img-blog.csdnimg.cn/00e2cbcc293c4b3489e4e6e69601f609.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
常见响应报文格式如下所示

```bash
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
Content-Length: 101
Date: Wed, 06 Jun 2018 07:08:42 GMT
​
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
  hello , response
  </body>
</html>
```

### 2.3.2 程序编写
创建com.liang.Response.java类，编写程序如下
```java
public class Response {
    private  OutputStream outputStream;

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) throws IOException {
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html; charset=utf-8\n")
                .append("\r\n")
                .append("<html><body>\n")
                .append(content).append("\n")
                .append("</body></html>\n");
        outputStream.write(httpResponse.toString().getBytes());
        outputStream.close();
    }
}
```
### 2.3.3 测试使用
更改MyTomcat类的内容为如下
```java
public class MyTomcat {
    public static void main(String[] args) {
        int port = 8099;
        try {
            ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
            System.out.printf("端口%d 等待连接\n", port);
            Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
            System.out.println(clientSocket);
            InputStream inputStream = clientSocket.getInputStream();
            Request request = new Request(inputStream);
            System.out.println(request);
            Response response = new Response(clientSocket.getOutputStream());
            response.write(request.getUrl()+"---Hello world！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```
重新启动并输入网址进行显示
网页显示如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/1bdc7e81bb6141f092c98fea294588b6.png)
## 2.4 Servlet编写
### 2.4.1 分析
在后台开发中，我们常用servlet来表示一系列的web服务处理类，类中包含doPost和doGet方法对应于两种请求方式，后续分发时根据url网址找到对应的处理类，通过反射获取到类，再使用类中对应的方法进行处理和响应
### 2.4.2 程序编写
创建一个com.liang.HttpServlet抽象类

```java
public abstract class HttpServlet {
    public abstract void doGet(Request request, Response response);
    public abstract void doPost(Request request, Response response);
    public void service(Request request, Response response){
        if(request.getMethod().equalsIgnoreCase("GET")){
            doGet(request, response);
        } else if(request.getMethod().equalsIgnoreCase("POST")){
            doPost(request, response);
        }
    }
}
```
创建一个测试Servlet，例 `com.liang.HelloServlet`，响应内容我们返回一个请求地址以及Hello servlet！

```java
public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(Request request, Response response) {
        try {
            response.write(request.getUrl()+"--- Hello Servlet！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(Request request, Response response) {

    }
}
```
### 2.4.3 测试分析
1. 更改MyTomcat内容为如下
   ```java
   public class MyTomcat {
       public static void main(String[] args) {
           int port = 8099;
           try {
               ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
               System.out.printf("端口%d 等待连接\n", port);
               Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
               System.out.println(clientSocket);
               InputStream inputStream = clientSocket.getInputStream();
               Request request = new Request(inputStream);
               System.out.println(request);
               Response response = new Response(clientSocket.getOutputStream());
               HelloServlet helloServlet = new HelloServlet();
               helloServlet.service(request, response);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
   ```
2. 运行后，输入网址访问的结果
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/7837db86f9c947788941c33a1b6feaaa.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_14,color_FFFFFF,t_70,g_se,x_16)

## 2.4 映射解析
### 2.4.1 分析
在使用时，我们经常要针对用户请求的不同网址来进行分发，分发至不同的servlet、controller进行后续的处理。因此，我们需要解析用户请求头中的URL，再通过反射将对应的方法全限定名来找到对应的类方法进行处理，在`java web`中，我们常使用web.xml来指定请求路径和处理类的对应关系，`web.xml`的常见格式如下所示
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                      http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0"
         metadata-complete="true">
    <servlet>
        <servlet-name>hello</servlet-name>
        <servlet-class>com.liang.HelloServlet</servlet-class>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>hello</servlet-name>
        <url-pattern>/hello</url-pattern>
    </servlet-mapping>
</web-app>
```
### 2.4.2 手动导入工具包
我们对web.xml文件进行解析，这里需要导入dom4j.jar这个工具进行解析，当然我们可以通过maven进行管理，我们这里手动进行导入，通过[链接](https://mvnrepository.com/artifact/org.dom4j/dom4j/2.1.1)进行下载
![在这里插入图片描述](https://img-blog.csdnimg.cn/77d77337aeb24ba88e873743b763ba53.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
在MyTomcat目录下新建一个lib目录，将下载的包放入lib目录下
![在这里插入图片描述](https://img-blog.csdnimg.cn/d910ada90df944b1894b9dd780132bdb.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_15,color_FFFFFF,t_70,g_se,x_16)
![在这里插入图片描述](https://img-blog.csdnimg.cn/ffb5853af7654ad9a9c475b1bda6cdd7.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
然后在弹出的界面中选择刚才创建的lib文件夹即可导入

### 2.4.3 程序编写
新建com.liang.XmlHandler类，编写代码如下

```java
public class XmlHandler {
    private final HashMap<String, HttpServlet> servletMapping = new HashMap<>();
    private final HashMap<String, String> urlMapping = new HashMap<>();
    public XmlHandler(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            SAXReader sr = new SAXReader();
            Document doc = sr.read(in);
            List<Element> elements = doc.getRootElement().elements();
            for (Element element : elements) {
                if (element.getName().equals("servlet-mapping")) {
                    String servletName = element.elementTextTrim("servlet-name");
                    String url = element.elementTextTrim("url-pattern");
                    urlMapping.put(url, servletName);
                } else if (element.getName().equals("servlet")) {
                    String servletName = element.elementTextTrim("servlet-name");
                    String className = element.elementTextTrim("servlet-class");
                    HttpServlet HttpServlet = (HttpServlet) Class.forName(className).newInstance();
                    servletMapping.put(servletName, HttpServlet);
                }
            }
        }catch (FileNotFoundException | IllegalAccessException
                | InstantiationException | ClassNotFoundException
                | DocumentException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, HttpServlet> getServletMapping() {
        return servletMapping;
    }

    public HashMap<String, String> getUrlMapping() {
        return urlMapping;
    }
}
```
### 2.4.4 解析测试
1. 在MyTomcat工程目录下创建一个`web.xml`，内容为本节前面的分析部分所展示的，其中的`servlet-class`记得改为你自己使用的

2. 在XmlHandler类添加一个main函数来测试一下编写的类是否OK
   ```java
   public static void main(String[] args) {
       String filePath ="web.xml";
       XmlHandler xmlHandler = new XmlHandler(filePath);
       System.out.println(xmlHandler.getServletMapping());
       System.out.println(xmlHandler.getUrlMapping());
   }
   ```
3. 输出如下，表示我们成功对xml文件进行解析了
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/87c5e5bd943b4f27aff1197903b20bd9.png)
### 2.4.5 使用测试
1. 删除XmlHandler中刚才创建的main函数
2. 改写MyTomcat类为如下
   ```java
   public class MyTomcat {
       public static void main(String[] args) {
           int port = 8099;
           try {
               XmlHandler xmlHandler = new XmlHandler("web.xml");
               HashMap<String, HttpServlet> servletMapping = xmlHandler.getServletMapping();
               HashMap<String, String> urlMapping = xmlHandler.getUrlMapping();
               ServerSocket serverSocket = new ServerSocket(port);   // 绑定端口，创建监听socket
               System.out.printf("端口%d 等待连接\n", port);
               Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
               System.out.println(clientSocket);
               InputStream inputStream = clientSocket.getInputStream();
               Request request = new Request(inputStream);
               System.out.println(request);
               Response response = new Response(clientSocket.getOutputStream());
   
               String servletName = urlMapping.get(request.getUrl());
               HttpServlet httpServlet = servletMapping.get(servletName);
               httpServlet.service(request, response);
   
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
   ```

3. 启动Tomcat，输入localhost:8099/hello进行访问，得到结果如下
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/59a76b4aae2843419e14b874d1558e28.png)
## 2.5 多线程优化
### 2.5.1 分析
这里我们已经完成了基本的分发和处理返回，但我们的程序一直有一个问题，那就是，只能处理一个请求。每次启动，用户请求一次后就结束了进程。因此我们还需要将其改进以支持多次访问，我们先不改写为多线程版本，先使用循环进行监听，并进行压力测试。
### 2.5.2 循环等待处理实现
1. 首先我们将MyTomcat中的代码规范化改写，并使用循环来接收连接，但接收到连接后暂不新开线程进行处理，而是依然在循环内进行处理，MyTomcat中的程序如下

```java
public class MyTomcat {
    private final int port;
    private final HashMap<String, HttpServlet> servletMapping;
    private final HashMap<String, String> urlMapping;

    public static void main(String[] args) throws IOException {
        MyTomcat myTomcat = new MyTomcat(8099);
        myTomcat.start();
    }

    public MyTomcat(int port) {
        this.port = port;
        XmlHandler xmlHandler = new XmlHandler("web.xml");
        servletMapping = xmlHandler.getServletMapping();
        urlMapping = xmlHandler.getUrlMapping();
    }
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            while (true){
                System.out.printf("端口%d 等待连接\n", port);
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                System.out.println(clientSocket);
                InputStream inputStream = clientSocket.getInputStream();
                Request request = new Request(inputStream);
                System.out.println(request);
                Response response = new Response(clientSocket.getOutputStream());
                dispatch(request, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatch(Request request, Response response){
        String servletName = urlMapping.get(request.getUrl());
        HttpServlet httpServlet = servletMapping.get(servletName);
        if(httpServlet != null)
            httpServlet.service(request, response);
    }
}
```
2. 为了方便显示出每次访问都能显示不同的效果，我们在HelloServlet中的doGet方法中添加一个时间打印

```java
    public void doGet(Request request, Response response) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        try {
            response.write(dateStr+" - "+request.getUrl()+"--- Hello Servlet！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
### 2.5.3 访问分析及压力测试
1. 浏览器输入http://localhost:8099/hello，多次刷新均可进入， 每次显示时间也不同
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/e4f2e700d2bc456e8ab5ebe50d19ad1d.png)
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/ce7f29f52b934bd9b366df45991579de.png)

2. 安装ab压力测试命令，这里介绍类Ubuntu、Debian系统的安装方式，即执行`sudo apt-get install apache2-utils`，Windows端的压力测试可[参考](https://blog.csdn.net/qq_39165617/article/details/123889844?spm=1001.2014.3001.5501)
3. 压力测试
   执行`ab -c 20000 -n 100000  http://localhost:8099/hello`进行压力测试，共发生10万请求，并发2万请求，进行测试，如下图所示为测试结果，从图中可看出，测试总花费6.7s，大部分请求最大等待时间都是106ms，所有的请求都能保证在106ms内得到响应
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/7750a5d2169b49cc87a94f8e489afc20.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
### 2.5.4 多线程版本编写
更改MyTomcat类里面的start方法为如下

```java
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            while (true){
                System.out.printf("端口%d 等待连接\n", port);
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                new Thread(() -> {
                    System.out.println(clientSocket);
                    InputStream inputStream = null;
                    try {  //  创建线程连接
                        inputStream = clientSocket.getInputStream();
                        Request request = new Request(inputStream);
                        System.out.println(request);
                        Response response = new Response(clientSocket.getOutputStream());
                        dispatch(request, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
### 2.5.5 多线程版本压力测试及分析
采用同样的参数进行测试，得到的结果如下，测试总耗时虽然长达17s，但所有的请求都能保证在8ms内得到响应，
分析主要原因有两点
1. 一个是我们对于我们的响应，由于是同机进行的io传输不存在网络拥堵情况，以及处理逻辑十分简单，因此能够很快的将响应发到客户端，因此即使循环等待处理也能有较好的响应时间，但一旦某一个请求出现网络拥堵或者该请求的处理逻辑较为耗时，那么后面的请求必须等待其完成，将浪费大量时间
2. 二是线程的的新建和销毁较为耗时，这个时间超过了或者几乎赶上处理和响应的时间了，因此多线程版本很多时间浪费在了线程新建上，但对于所有请求，多线程版本能够保证处理的最大延时更短一些，因为它不用像循环等待那样必须等待上一个请求被响应完
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/45058afe1f2a402f8ba6a3ccc70e491a.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
### 2.5.6 线程池版本编写
创建一个线程池，，来执行任务，有新连接则放入任务队列中，可执行watch -n 0.1 "netstat -anp | grep 8099 | wc -l"查看连接的变化
```java
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            ThreadPoolExecutor executors = new ThreadPoolExecutor(100, 100, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            while (true){
                System.out.printf("端口%d 等待连接\n", port);
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                executors.submit(() -> {
                    System.out.println(clientSocket);
                    InputStream inputStream;
                    try {
                        inputStream = clientSocket.getInputStream();
                        Request request = new Request(inputStream);
                        System.out.println(request);
                        Response response = new Response(clientSocket.getOutputStream());
                        dispatch(request, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
### 2.5.7 线程池版本压力测试
线程池版本下，测试结果如下，可以看出总时间相对于多线程版本有了降低，能够保证的最大延时相对于单线程也有了降低
![在这里插入图片描述](https://img-blog.csdnimg.cn/8bd25c93e8894b4b86d485b7762b31d0.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_20,color_FFFFFF,t_70,g_se,x_16)
# 三、进行框架升级，复杂版本准备
之前我们只是实现了普通的响应封装和返回，后面我们需要做两件事
1. 支持文件传输，很多时候我们的网页是有文件的，因此我们需要支持对文件的传输
2. IO复用升级，对于服务器的实现我们实现的是BIO版本，我们还能够使用java中的new IO库进行升级，并最后使用更方便的netty进行升级

由于我们需要多版本IO共存支持，为了尽可能减少代码的重复编写和利用多态增强调用耦合性，我们需要创建一些抽象类和利用面向对象的特点对代码整体进行优化，这也是博主在编写过程中对面向对象的一个理解，中间花了较多时间进行思考和代码框架形式考虑，另外，思考出来的框架格式不一定是最好的，但必然具有一定的参考
## 3.1 核心类编写
创建com.liang.core包，里面将建立多个公用的核心类，包括抽象类和接口以及普通类
### 3.1.1 Request和Response抽象类
1. 创建com.liang.core.Request.java类，不同IO类型对于Requset的获取有不同，即构造方法不相同，而均需要解析出method、url等字段，因此我们对这些字段进行定义和对获取方法定义，内容如下
   ```java
   public abstract class Request {
       public String method;
       public String url;
   
       public String getMethod() {
           return method;
       }
   
       public String getUrl() {
           return url;
       }
   
       @Override
       public String toString() {
           return "Request{" +
                   "method='" + method + '\'' +
                   ", url='" + url + '\'' +
                   '}';
       }
   }
   ```
2. 创建`com.liang.core.Response.java`类，不同IO类型将有不同的write方法实现，需要继承于本类，内容如下
   当然，这里也可以使用接口，暂时用抽象类实现
   ```java
   public abstract class Response {
       public abstract void write(String content);
       public abstract void write(File file);
   }
   ```
### 3.1.2 Servlet抽象类
1. 创建`com.liang.core.Servlet.java`类（之前有创建过，只是改名和移动位置了），主要是不同功能Servlet中doGet和doPost方法有不同的实现方式，因此需要定义出抽象类，内容如下
   ```java
   public abstract class Servlet {
       public abstract void doGet(Request request, Response response);
       public abstract void doPost(Request request, Response response);
       public void service(Request request, Response response){
           if(request.getMethod().equalsIgnoreCase("GET")){
               doGet(request, response);
           } else if(request.getMethod().equalsIgnoreCase("POST")){
               doPost(request, response);
           }
       }
   }
   ```
### 3.1.3 文件资源发送--ResourceServlet类
- 创建`com.liang.core.ResourceServlet.java`类，这是在文件发送时使用的一个公用类， 内容如下，这里由于服务开启后，将会多次使用ResourceServlet类，但每次调用的都只是使用其方法，因此我们使用一个单例来进行获取，我这里使用的是双重校验锁的懒汉式
- 在客户端请求文件时，通过url和我们自己定义的资源位置进行定位，若服务器不存在该文件则返回404，若存在则调用各种Response对应的文件传输方法
  ```java
  public class ResourceServlet extends Servlet {
      private static volatile ResourceServlet resourceServlet = null;
      public static ResourceServlet getResourceServlet(){
          if (resourceServlet==null){
              synchronized (ResourceServlet.class){
                  if (resourceServlet==null){
                      resourceServlet = new ResourceServlet();
                  }
              }
          }
          return resourceServlet;
      }
      @Override
      public void doGet(Request request, Response response) {
          String url = request.getUrl();
          String path = "resources"+url;
          File file = new File(path);
          if (file.isHidden() || !file.exists()) {
              response.write("<h1>404 Not Found</h1>");
              return;
          }
          if (!file.isFile()) {
              response.write("<h1>404 Not Found</h1>");
              return;
          }
          response.write(file);
      }
  
      @Override
      public void doPost(Request request, Response response) {
  
      }
  }
  ```
### 3.1.4 xml解析和请求分发类--Dispatcher类
- 创建`com.liang.core.Dispatcher.java`类，主要内容是之前创建的XmlHandler类，这里我将分发(dispatch)集成到了这里一起，服务启动时将会解析xml映射文件，解析完成后将用于后期各种服务的分发调用
  ```java
  public class Dispatcher {
      private final HashMap<String, Servlet> servletMapping = new HashMap<>();
      private final HashMap<String, String> urlMapping = new HashMap<>();
  
      public Dispatcher(String filePath) {
          try {
              FileInputStream in = new FileInputStream(filePath);
              SAXReader sr = new SAXReader();
              Document doc = sr.read(in);
              List<Element> elements = doc.getRootElement().elements();
              for (Element element : elements) {
                  if (element.getName().equals("servlet-mapping")) {
                      String servletName = element.elementTextTrim("servlet-name");
                      String url = element.elementTextTrim("url-pattern");
                      urlMapping.put(url, servletName);
                  } else if (element.getName().equals("servlet")) {
                      String servletName = element.elementTextTrim("servlet-name");
                      String className = element.elementTextTrim("servlet-class");
                      Servlet HttpServlet = (Servlet) Class.forName(className).newInstance();
                      servletMapping.put(servletName, HttpServlet);
                  }
              }
          }catch (FileNotFoundException | IllegalAccessException
                  | InstantiationException | ClassNotFoundException
                  | DocumentException e) {
              e.printStackTrace();
          }
      }
      public void dispatch(Request request, Response response){
          String servletName = urlMapping.get(request.getUrl());
          Servlet httpServlet = servletMapping.get(servletName);
          if(httpServlet != null)
              httpServlet.service(request, response);
          else
              ResourceServlet.getResourceServlet().service(request, response);
      }
  }
  ```
### 3.1.5 Server抽象类
- 创建`com.liang.core.Servlet.java`类，这个是服务器的基本类，后期的BIO、New IO和Netty版本服务器都需要实现该类，不同类型的start方法不同，而均需要对xml映射进行解析，以及端口的设置，因此我们这里定义了这两个变量，并设置了构造方法，内容如下
  ```java
  public abstract class Server {
      public Dispatcher dispatcher;
      public int port;
  
      public Server(int port, String xmlPath) {
          this.dispatcher = new Dispatcher(xmlPath);
          this.port = port;
      }
      public abstract void start();
  }
  ```
## 3.2 BIO版本
创建`com.liang.server.BIO`包，存放我们编写BIO版本的Server，该版本就是我们前面编写的简易版Tomcat，
### 3.2.1 BIORequest编写
- 内容与之前类似，继承于核心Request类
  ```java
  public class BIORequest extends Request {
  
      public BIORequest(InputStream inputStream) throws IOException {
          byte[] bytes = new byte[1024];
          int length = inputStream.read(bytes);
          String requestStr = new String(bytes, 0, length);
          String requestHead = requestStr.split("\n")[0].trim();
          String[] heads = requestHead.split("\\s+");
          method = heads[0];
          url = heads[1];
      }
  }
  ```

### 3.2.2 Response分析
对于Response，我们在之前实现了普通String字符串类型数据的打印显示，这里我们还需要实现文件的传输，因为服务器不可避免的要进行文件的传输，例如显示网页时需要传输html文件，并且html文件中可能包含一些静态资源，例如图片、视频等这些资源都是需要http进行传输的

在这里文件传输的方案其实有很多种，包括
1. 使用用户缓冲区(即自己定义的byte数组)进行读写传输，两次DMA拷贝、两次cpu拷贝，且上下文切换次数多
2. 使用内存映射(mmap)技术进行传输，两次DMA拷贝、一次cpu拷贝
3. 使用零拷贝(sendfile)技术进行传输，两次DMA拷贝、一次cpu拷贝

### 3.2.3 BIOResponse编写
这里我们使用普通方式，定义一个缓冲区数组进行传输，每次读取一定长度的数据到缓冲区，后续使用New IO的时候再进行升级改造
```java
public class BIOResponse extends Response {
    private  OutputStream outputStream;

    public BIOResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content){
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html; charset=utf-8\n")
                .append("Content-Length: ").append(content.getBytes().length).append("\n")
                .append("\r\n")
                .append(content).append("\n");
        try {
            outputStream.write(httpResponse.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(File file) {
        StringBuffer httpResponse = new StringBuffer();
        long fileLength = file.length();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Length: ").append(fileLength).append("\n")
                .append("\r\n");
        byte[] bytes = new byte[1024];
        if (file.exists()) {
            try {
                outputStream.write(httpResponse.toString().getBytes());
                FileInputStream fileInputStream = new FileInputStream(file);
                int length;
                while ((length=fileInputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, length);
                }
                fileInputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```
### 3.2.4 服务主程序编写
创建`com.liang.server.BIO.BIOServer`这部分程序也是之前我们创建的MyTomcat程序，使用是线程池进行任务处理，程序内容如下，这里

```java
public class BIOServer extends Server {

    public BIOServer(int port, String xmlPath) {
        super(port, xmlPath);
    }
    public void start(){
        ServerSocket serverSocket = null;   // 绑定端口，创建监听socket
        try {
            serverSocket = new ServerSocket(port);
            System.out.printf("端口%d 等待连接\n", port);
            ThreadPoolExecutor executors = new ThreadPoolExecutor(100, 100, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            while (true){
                Socket clientSocket = serverSocket.accept();     // 等待连接，连接到则创建新socket
                executors.submit(() -> {
                    System.out.println(clientSocket);
                    InputStream inputStream;
                    try {
                        inputStream = clientSocket.getInputStream();
                        BIORequest bioRequest = new BIORequest(inputStream);
//                        System.out.println(bioRequest);
                        BIOResponse bioResponse = new BIOResponse(clientSocket.getOutputStream());
                        dispatcher.dispatch(bioRequest, bioResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
## 3.3 静态文件导入和使用
### 3.3.1 资源导入
1. 在项目文件夹下创建一个`resources`文件夹，创建一个`index.html`，以及图片和视频等，文件及结构如下图所示
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/cee7978d74584ca2aa7a77a18b0d0118.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_12,color_FFFFFF,t_70,g_se,x_16)
2. index.html中的内容如下所示
   ```html
   <!DOCTYPE html>
   <html lang="en">
   
   <head>
     <meta charset="utf-8">
     <meta content="width=device-width, initial-scale=1.0" name="viewport">
     <title>测试网页</title>
   </head>
   
   <body>
     <header>
       <div>
         <div>
           <h1>Test Tomcat</h1>
           <img src="assets/img/test.png" alt="">
           <h1><a href="assets/video/test.mp4">下载 mp4 文件测试</a></h1>
         </div>
       </div>
     </header>
   </body>
   
   </html>
   ```


### 3.3.2 添加Servlet服务处理类
1. 创建com.liang.servlet.HelloServlet类
   ```java
   public class HelloServlet extends Servlet {
       @Override
       public void doGet(Request request, Response response) {
           Date date = new Date();
           StringBuffer htmlContent = new StringBuffer();
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           String dateStr = sdf.format(date);
           String content = dateStr+"测试 - "+request.getUrl()+"--- Hello Servlet！";
           htmlContent.append("<html><body>\n")
                   .append(content).append("\n")
                   .append("</body></html>\n");
           response.write(htmlContent.toString());
       }
   
       @Override
       public void doPost(Request request, Response response) {
   
       }
   }
   ```
2. 创建com.liang.servlet.IndexServlet类
   ```java
   public class IndexServlet extends Servlet {
       @Override
       public void doGet(Request request, Response response) {
           File file = new File("resources/index.html");
           response.write(file);
       }
   
       @Override
       public void doPost(Request request, Response response) {
   
       }
   }
   ```
3. 在web.xml中添加一个映射，添加的映射如下，另外由于我们更改了代码的框架，还需要自行更改之前的servlet映射

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
            version="4.0"
            metadata-complete="true">
       <servlet>
           <servlet-name>hello</servlet-name>
           <servlet-class>com.liang.servlet.HelloServlet</servlet-class>
       </servlet>
   
       <servlet-mapping>
           <servlet-name>hello</servlet-name>
           <url-pattern>/hello</url-pattern>
       </servlet-mapping>
   
       <servlet>
           <servlet-name>index</servlet-name>
           <servlet-class>com.liang.servlet.IndexServlet</servlet-class>
       </servlet>
   
       <servlet-mapping>
           <servlet-name>index</servlet-name>
           <url-pattern>/index</url-pattern>
       </servlet-mapping>
   </web-app>
   ```
### 3.3.3 测试
1. 创建一个com.liang.ServerStart类
   ```java
   public class ServerStart {
       public static void main(String[] args) {
           Server server = new BIOServer(8099, "web.xml");
           server.start();
       }
   }
   ```
2. 启动后即可使用，在浏览器地址栏中可输入
   localhost:8099/index 进行访问
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/e6adef4d27354ddb9daa69c7c089444a.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rSq5Z-O5biD6KGj,size_19,color_FFFFFF,t_70,g_se,x_16)
   点击下面的==下载mp4文件测试==链接可以进行下载测试，另外对于静态文件，你可以在resource文件夹下放置任何静态类网页文件，均可以进行链接显示

### 3.3.3 分析
这里我们可以进行压力测试
可以本机测试执行，也可远程主机进行测试 填写本机ip地址进行测试
`jps` 查看进程id为932684
新终端执行`watch -n 0.1 "netstat -anp | grep 8099 | wc -l "`查看网络连接的数量
新终端执行 `watch -n 0.1 "lsof -p 932684 | wc -l"`查看使用文件描述符的数量
新终端使用`jstat -gcutil 932684 200 100`  查看gc变化
新终端执行 `ab -c 200 -n 1000  http://localhost:8099/index` 进行压测
# 四、New IO 版本
后续博客补充
# 五、Netty版本
后续博客补充