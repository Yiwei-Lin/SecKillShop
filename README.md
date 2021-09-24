#### **目录**

[项目简介](#项目简介)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[技术栈](#技术栈)

[用户手册](#用户手册)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[项目开发环境](#项目开发环境)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[项目配置与启动步骤](#项目配置与启动步骤)

[秒杀业务场景详解](#秒杀业务场景详解)

[项目结构](#项目结构)

[详细设计](#详细设计)

[总结](#总结)


### 项目简介

- 本项目实现了一个高并发的秒杀业务场景，完成了登录模块、商品模块与秒杀模块，关于每个模块的设计细节和优化过程本文档将会详细说明。

- 在保证业务逻辑正确的情况下（不发生超卖、少卖等情况），尽可能利用现有技术栈优化QPS。
- 本项目在完成之初，经JMeter压测1000左右QPS，最终优化到QPS达到5000+。注意本数据是在Redis集群、RabbitMQ和应用服务都通过虚拟机部署的条件下测试（硬件资源有限哈~），如果部署到真实的服务器、同时用户并发量进一步增大的条件下将会获得更大程度QPS的提升。

#### 技术栈

- MySQL
- Redis
- SpringBoot
- Mybatis
- RabbitMQ
- html+jQuery+Bootstrap

### 用户手册

&nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; 在启动项目时，请参照本手册进行配置环境和参数。

#### 项目开发环境

- JDK 1.8
- Maven 3.6.1
- Springboot 1.5.9
- MySQL 8.0.11
- Mybatis 1.3.1
- Redis 5.0.3 （集群）
- RabbitMQ 3.7.5

#### 项目配置与启动步骤

1. 按照项目开发环境中列出的版本清单配置环境，注意Redis集群、MySQL、RabbitMQ和应用服务安装在不同机器上，当然也可都在本机或部署在虚拟机上。

 2. 运行resources目录下的sql文件导入数据库表和测试数据

 3. 修改SpringBoot配置文件application.properties中如下相关配置

    ```properties
    # 数据源url，修改MySQL服务器IP
    spring.datasource.url=jdbc:mysql://192.168.0.103:3306/seckillshop?useUnicode=true&characterEncoding=utf-8&serverTimezone=PRC&allowMultiQueries=true&useSSL=false
    # 修改为MySQL用户名
    spring.datasource.username=root
    # 修改为MySQL密码
    spring.datasource.password=abc123def
    # MySQL8.0以上务必使用cj包下的驱动
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    
    # 修改redis集群的IP地址和端口号，不同机器的IP和端口号用逗号隔开
    redis.clusterNodes=192.168.81.131:6379,192.168.81.132:6379,192.168.81.133:6379,192.168.81.136:6379,192.168.81.137:6379,192.168.81.138:6379
    
    # 修改为RabbitMQ部署机器的IP地址
    spring.rabbitmq.host=192.168.0.103
    # 修改为RabbitMQ部署机器的端口号
    spring.rabbitmq.port=5672
    # 修改为RabbitMQ用户名
    spring.rabbitmq.username=dev
    # 修改为上述用户名对应的密码
    spring.rabbitmq.password=dev
    # 修改为RabbitMQ使用的虚拟主机
    spring.rabbitmq.virtual-host=/
    ```

4. 由于Springboot自带tomcat，直接运行MainApplication类的main方法即可，也可将项目打成jar包部署到应用服务器通过java -jar命令运行。
5. JMeter进行压测的相关配置也导出放在了JMter目录下，可自行参考JMeter教程导入jmx文件即可。

### 秒杀业务场景详解

- **秒杀场景的特点**

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;秒杀的商品具有**价格低、库存有限、定时开始**的特点，因此秒杀场景最大的特点就是**高并发**。数以千万的用户的流量集中在某个时间点上（即秒杀开始时），给后端服务器造成很大压力，如果不能进行有效削峰、限流，所有请求一次性打到某一台服务器或数据库上，必然造成服务的不可用，给用户造成不良体验。

- **整体架构设计**

  ![image-20200612215327156](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200612215327156.png?raw=true)

&nbsp;&nbsp; &nbsp;&nbsp; 在整体架构上，用户的请求首先通过一个网关进行负载均衡，根据负载均衡算法的不同，应用不同的策略将请求转发给对应的应用服务器，服务器对用户的请求进行限流，拒绝大部分的请求。根据Redis中缓存的商品信息，判断当前秒杀是否已结束，如果秒杀结束，拒接请求并设置内存标记，后续的请求直接拒绝，无需再查询Redis缓存；如果秒杀未结束，将请求入队MQ，进行异步处理，进一步削峰。

&nbsp;&nbsp; &nbsp;&nbsp; 多个消费者从MQ中取消息，进行处理，即检查对应商品的库存情况，减库存并生成订单，要注意这里需要原子操作的控制，也有多种解决方案，在后面会详细讨论。对秒杀成功的订单，设置有效期，用户在有效期内成功付款，则生成支付订单，持久化到MySQL，否则会随着Redis缓存中key的过期而失效。

- **秒杀系统常见的问题**

  &nbsp;&nbsp; &nbsp;&nbsp; 秒杀系统由于高并发的特点，会带来一系列问题，这些问题不仅是秒杀系统独有，任何高并发系统都需要考虑这些问题的解决方案。

  - **高并发带来的响应缓慢甚至服务不可用**

    &nbsp;&nbsp; &nbsp;&nbsp; MySQL由于数据都存放在磁盘中，并发量十分有限，并且为了保证业务逻辑的正确性，每个请求都需要对商品数据进行一次加锁和解锁操作（行锁的效率低），更加降低了效率，造成不好的用户体验。更糟的情况是MySQL在高并发环境下，还可能会崩溃，造成服务不可用。

  - **超卖、少卖**

    &nbsp;&nbsp; &nbsp;&nbsp; 超卖问题来源于核减库存的操作其实不具备原子性，它分为了三步：查询库存->检查库存不为0->扣减库存。那么设想一个场景：当服务查询库存为1，但还未扣减库存时，另一个服务查询库存也为1，两个服务都会进行扣减库存、生成订单的操作，造成了超卖。

    &nbsp;&nbsp; &nbsp;&nbsp; 少卖问题来源于已经成功扣减库存，但生成秒杀订单因为各种原因失败了，导致库存被扣减却没有订单生成的情况。该问题出现的根本原因是扣减库存和生成订单两步操作没有保证原子性。

  - **用户作弊**

    &nbsp;&nbsp; &nbsp;&nbsp; 某些不法用户可能通过自动化脚本模拟HTTP请求的方式反复刷秒杀接口，既对系统造成了更大的流量压力也产生了造成了不公平。

- **优化方案**

  - **将大部分请求拦截在上游**

    &nbsp;&nbsp; &nbsp;&nbsp; 通过随机算法、哈希算法或根据当前负载情况动态地拦截请求，快速失败，只将少部分的请求放入MQ等待消费者消费。

  - **验证码机制**

    &nbsp;&nbsp; &nbsp;&nbsp; 通过验证码机制能够很好限制用户的请求速度，同时也能防止作弊。验证码的选择上可以选择图片、算式、滑块等，为了防止验证码识别工具，尽可能选择较复杂的验证码。

  - **限制用户的每秒请求次数**

    &nbsp;&nbsp; &nbsp;&nbsp; 每次用户请求后在缓存中进行计数，并设置相应有效期，当用户请求达到阈值直接拒绝请求，实现限制单用户每秒请求次数的功能，防止单一用户的高频访问给系统造成更大压力，这个策略根据业务需求的不同可对账号的限制、IP的限制或账号和IP共同限制。

  - **页面静态化、页面缓存**

    &nbsp;&nbsp; &nbsp;&nbsp; 通过页面静态化、页面缓存的方式降低响应的时间。

    &nbsp;&nbsp; &nbsp;&nbsp; 页面静态化是进行页面缓存的第一步，将一个动态页面分离成静态的页面模板和动态的数据部分，将静态的部分拆分出来进行缓存，动态的部分根据服务器的业务处理返回json数据再进行渲染。比如在秒杀页面，页面的模板框架是静态资源，可以进行缓存，而商品名称、库存数量、价格、秒杀剩余时间、验证码等信息根据请求的响应结果进行动态渲染。

    &nbsp;&nbsp; &nbsp;&nbsp; 静态页面抽离出来之后就要进行缓存，缓存可以放在用户浏览器、服务端或CDN，放在浏览器上的缓存具有不可控性，如果用户不进行及时刷新，很可能看到错误的不一致信息，对于秒杀系统而言，信息的实时性非常重要，这点看来放在浏览器上的缓存并不合适。另外服务端主要进行业务逻辑的计算和加载，不擅长处理大量连接，如果进行页面的缓存和加载会带来性能的降低。因此页面缓存常放在CDN上。

    ​		![image-20200613121321101](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200613121321101.png?raw=true)

    &nbsp;&nbsp; &nbsp;&nbsp; CDN的节点一般选择访问量集中的地区附近且要保证节点与主站之间的网络通信，即考虑缓存的命中率和及时失效的问题，这点对于高并发、信息变化快的秒杀系统而言及其重要。

  - **利用MQ异步削峰**

    &nbsp;&nbsp; &nbsp;&nbsp; 大量的请求并不直接到达应用服务器，而是先进入MQ队列，多个消费者根据处理能力从MQ中拿到请求消息并进行业务处理，类似于著名的“漏桶算法”。MQ还可以通过设置最大队列的长度或消息的有效期TTL来进行限流，通过设置消息超时快速失败，防止大量的消息堆积给用户带来不好的体验。

  - **Redis缓存提高并发量**

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Redis作为一个内存数据库比MySQL的QPS高100倍以上，并且由于底层操作处理是单线程的，在高并发、分布式架构下无需反复的加锁、解锁和线程上下文的切换，更进一步提高了效率。通常将热点数据缓存到Redis中，比如秒杀系统中商品的ID、库存等信息，秒杀过程中进行核减库存等操作都是在Redis中，能显著提高效率。Redis通常以集群部署且设置若干哨兵以保证服务的高可用性。

  - **定时任务进行缓存预热**

    &nbsp;&nbsp; &nbsp;&nbsp; 秒杀场景比较特殊（相较于微博热搜等热点数据而言），大流量会集中在秒杀开始的时间点上，如果此时缓存中没有相关商品数据，会导致瞬间请求全部转向数据库，造成缓存击穿。因此，秒杀场景的缓存预热非常有必要，由于秒杀都有一个确定的开始时间，可以通过定时任务在秒杀开始前的某个时刻将商品的相关信息预热到缓存中，这样当秒杀开始时，在缓存中就有相关的数据了，这个定时任务可以不需要非常频繁进行检查，性能消耗并不大，比如规定在秒杀开始前5分钟进行缓存预热，那么每1分钟进行1次检查完全足够，这个策略在本项目中也有应用，详见详细设计。

### 项目结构

- **顶级目录**

![image-20200613174719022](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200613174719022.png?raw=true)

&nbsp;&nbsp; &nbsp;&nbsp; 顶级目录包括java和resources两个目录，其中java目录是项目源码目录，resources目录是资源目录，包括配置文件、页面、静态资源（如商品图片）等。

- **com.shop**

![image-20200613180133262](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200613180133262.png?raw=true)

  下面对每个包/类的作用简要说明：	

1. config包：存放配置类
2. controller包：存放controller层相关类，调用业务逻辑处理后返回ModelAndView对象
3. dao包：存放持久层相关类，对MySQL数据库的操作
4. entity包：存放实体类，用于ORM
5. exception包：存放全局异常类，自定义的全局异常，对产生的异常信息进行处理并反馈对应错误码和错误信息
6. limit包：用于限制单个用户的访问频率
7. message包：存放消息相关类，包括返回结果消息、错误消息、MQ中存放的消息等
8. rabbitmq包：MQ的生产者和消费者
9. redis包：对redis缓存的操作，并对key进行前缀封装，以区分不同用途的key
10. service包：存放service层相关类，处理业务逻辑
11. utils包：存放工具类，包括数据库、MD5加解密、UUID生成等
12. vo包：存放表现层对象实体
13. MainApplication：主启动类

- **resources**

![image-20200616110302469](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200616110302469.png?raw=true)

1. pages：存放前台页面

 	2. sql：存放sql文件，启动之前务必导入sql文件到MySQL数据库中
 	3. static：存放静态文件，如：静态页面、css样式、js等
 	4. application.properties：Springboot配置文件

### 详细设计

- **用户登录**

  &nbsp;&nbsp; &nbsp;&nbsp; 用户登录流程图如下：

![image-20200616082836217](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200616082836217.png?raw=true)

&nbsp;&nbsp; &nbsp;&nbsp; 首先用户进入登录页面，输入用户名和密码后，在发送登录请求之前，由前台进行固定盐值MD5加密，以保证请求过程的安全性。请求到达后台后，每个用户都有一个随机的盐值，进一步保证数据库中密码的安全性，后台从数据库中拿到用户对应的盐值和经加密后的密码，进行比对验证。如果验证成功则生成一个token放入redis缓存并设置登录有效期同时token设置为客户端的cookie。

- 解析用户

&nbsp;&nbsp; &nbsp;&nbsp; 由于HTTP是无状态连接，因此在后续的请求中，需要从cookie中或URL中（用户禁用cookie时）拿到token，根据缓存的token记录获取用户信息，这样服务器才知道是哪个用户进行的秒杀操作。通过实现spring的方法参数解析器HandlerMethodArgumentResolver接口的resolveArgument()方法实现token和用户对象的映射关系。这部分的实现在config包下的UserArgumentResolver类中。

- **消息封装**

  ​	&nbsp;&nbsp; &nbsp;&nbsp; 消息封装相关的类在message包中，包括对返回结果、错误消息和MQ消息的封装。

  ​	![image-20200614094429982](https://github.com/Yiwei-Lin/SecKillShop/blob/master/pics/image-20200614094429982.png?raw=true)

  - **返回结果**

    &nbsp;&nbsp; &nbsp;&nbsp; 对应ResultMsg类，包括了返回码、消息和数据（vo对象）。

    ```java
    private int code;        // 返回码
    private String message;  // 消息
    private T data;          // 数据
    ```

  - **错误消息**

    &nbsp;&nbsp; &nbsp;&nbsp; 对应ErrorMsg类，包括错误码和错误提示信息，并定义了系统中可能出现的错误和错误码。

    ```java
    private int errorCode;   // 错误码
    private String message;  // 错误消息
    // 通用异常
    public static final ErrorMsg GENERAL_ERROR = new ErrorMsg(50010, "服务器异常");
    public static final ErrorMsg VERIFY_CODE_ERROR = new ErrorMsg(50011, "验证码错误");
    // 登录异常
    public static final ErrorMsg LOGIN_ERROR = new ErrorMsg(50020, "登录异常");
    public static final ErrorMsg MOBILE_NOT_EXIST = new ErrorMsg(50021, "手机号未注册");
    public static final ErrorMsg PASSWORD_ERROR = new ErrorMsg(50022, "密码错误");
    public static final ErrorMsg SESSION_ERROR = new ErrorMsg(50023, "Session已失效");
    //订单异常
    public static final ErrorMsg ORDER_ERROR = new ErrorMsg(50030, "订单异常");
    public static final ErrorMsg ORDER_NOT_EXIST = new ErrorMsg(50031, "订单不存在");
    //秒杀异常
    public static final ErrorMsg SECKILL_FAIL = new ErrorMsg(50050, "秒杀失败");
    public static final ErrorMsg SECKILL_OVER = new ErrorMsg(50051, "商品已售空");
    public static final ErrorMsg REPEATED_SECKILL = new ErrorMsg(50052, "请勿重复秒杀");
    public static final ErrorMsg SECKILL_LIMIT= new ErrorMsg(50053, "请求太频繁，请稍候再试！");
    ```

  - **MQ消息**

    &nbsp;&nbsp; &nbsp;&nbsp; 对应MQMsg类，由于MQ入队之前已经检查内存结束标志、用户校验等信息，MQ中的消息比较简单，只需记录用户和商品ID即可，当然也可根据实际需求增加属性。

    ```java
    private User user;    // 用户对象
    private long goodsId; // 秒杀的商品ID
    ```

- **定时任务**

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;定时任务的目的是进行缓存预热，因为秒杀开始时间是确定的，因此是能够预计商品在何时会出现高并发抢购的情况，那么提前进行缓存预热很有必要。

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;定时任务的执行周期既可以通过配置文件指定，也可通过数据库指定。一般而言对于大型系统会采取后者方案，并通过后台管理系统动态设置定时任务，在有秒杀活动时进行周期性预热。

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;定时任务的配置在config包下的ScheduleTaskConfig配置类中。

  ```java
  @Scheduled(fixedRate = SCAN_GOODS_RATE)
  public void scanGoodsToRedis() {
      List<GoodsVo> goodsList = goodsService.listGoodsVo();
      if (goodsList == null) {
          return;
      }
      Date curTime = new Date();
      for (GoodsVo goods : goodsList) {
          if(redisService.get(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goods.getId())) != null)   // 已在缓存
              continue;
          // 不在缓存且距离开始时间小于5分钟
          if (goods.getStartDate().getTime() - curTime.getTime() <= 5 * 60 * 1000) { 
              redisService.set(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goods.getId()), goods.getStockCount());
          }
      }
  ```

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本项目中定时任务的策略是：每分钟进行一次扫描，如果商品不在缓存中，且距离秒杀开始时间小于5分钟，则将商品的相关信息（至少包括商品ID、库存）预热到缓存。

- **用户秒杀频率限制**

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;系统需要对用户的秒杀频率做限制，一方面通过验证码机制可以防止用户频繁的秒杀或自动化脚本作弊，也可适当削峰；另一方面通过对用户ID或IP的每秒请求限制，防止单一用户的频繁请求进一步加大系统压力。

  - **验证码**

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本项目中验证码机制的实现在service包下的SeckillService类中，通过BufferedImage创建一个图像缓存中，然后生成随机数字得到算式绘图，再加入随机的线条、背景等干扰元素，通过输出流刷新到前台。同时要将验证码结果和用户、商品的对应关系放入redis缓存中并设置60秒有效期，表示“验证码60秒内有效“，使用算式验证码能一定程度上防止一些验证码识别工具的作弊行为。

    ```java
    public BufferedImage createVerifyCode(User user, long goodsId) {
       if(user == null || goodsId <=0) {
          return null;
       }
       int width = 80;
       int height = 32;
       // 生成图片
       BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
       Graphics g = image.getGraphics();
       // 设置背景色
       g.setColor(new Color(0xDCDCDC));
       g.fillRect(0, 0, width, height);
       // 画边框
       g.setColor(Color.black);
       g.drawRect(0, 0, width - 1, height - 1);
       Random randGenerator = new Random();
       for (int i = 0; i < 50; i++) {
          int x = randGenerator.nextInt(width);
          int y = randGenerator.nextInt(height);
          g.drawOval(x, y, 0, 0);
       }
       // 随机生成验证码
       String verifyCode = generateVerifyCode(randGenerator);
       g.setColor(new Color(0, 100, 0));
       g.setFont(new Font("DejaVuSans", Font.BOLD, 24));
       g.drawString(verifyCode, 8, 24);
       g.dispose();   // 及时销毁Graphics对象，否则内存泄漏
       // 把验证码存到redis中
       redisService.set(SeckillPrefix.VERIFY_CODE_PREFIX, user.getId() + "_" + goodsId, String.valueOf(calc(verifyCode)));
       return image;
    }
    ```

  - **每秒请求限制**

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;为了防止单一用户的频繁请求进一步加大系统压力，对用户的访问频率进行限制。这部分的实现在limit包下。

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;首先定义Limit注解便于指定需要进行限制的接口对应的方法。

    ```java
    @Retention(RUNTIME)   // 注解要保留到运行时，通过拦截器检查是否需要进行限制
    @Target(METHOD)       // 注解应用于方法上
    public @interface Limit {
       int seconds();     	  // 指定时间内
       int maxCount();        // 最大请求次数
       boolean loginCheck() default true;   // 登录检查——默认需要检查
    }
    ```

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;然后通过spring的拦截器对请求的业务方法进行预处理，如果该方法有注解（即需要进行请求限制），且用户的请求次数超过限制则拒绝请求。这部分实现在limit包下的LimitInterceptor类。

    ```java
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
       if(handler instanceof HandlerMethod) {
          User user = getUser(request, response);
          HandlerMethod handlerMethod = (HandlerMethod) handler;
          Limit limit = handlerMethod.getMethodAnnotation(Limit.class);
          if(limit == null) {  // 没有limit注解直接返回true
             return true;
          }
          int seconds = limit.seconds();
          int maxCount = limit.maxCount();
          boolean loginCheck = limit.loginCheck();
          String key = request.getRequestURI();
          if(loginCheck) {        // 需要登录检查，进行登录检查，防止非法用户请求
             if(user == null) {
                render(response, ErrorMsg.SESSION_ERROR);
                return false;
             }
             key += "_" + user.getId();
          }
          LimitPrefix limitPrefix = LimitPrefix.expireLimit(seconds);
          String count = redisService.get(limitPrefix, key);
           if(count  == null) {
               redisService.set(limitPrefix, key, "1");
           }else if(Integer.parseInt(count) < maxCount) {
               redisService.incr(limitPrefix, key);
           }else {
              render(response, ErrorMsg.SECKILL_LIMIT);
              return false;
           }
       }
       return true;
    }
    ```

- **异常处理**

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;系统根据业务需求需要自定义异常，包括登录、商品、秒杀模块并指定错误码和异常提示信息。异常处理通过spring提供的异常处理器实现。

  ```java
  @ControllerAdvice
  @ResponseBody
  public class GlobalExceptionHandler {
     @ExceptionHandler(value=Exception.class)
     public ResultMsg<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();
        if(e instanceof GlobalException) {   // 是系统定义的全局异常则返回对应错误信息
           GlobalException globalException = (GlobalException)e;
           return ResultMsg.error(globalException.getErrorMsg());
        } else {   // 否则返回GENERAL_ERROR
           return ResultMsg.error(ErrorMsg.GENERAL_ERROR);   
        }
     }
  }
  ```

- **秒杀请求处理**

  &nbsp;&nbsp;秒杀请求的处理过程分为以下几个步骤：

  1. 检查用户是否登录
   	2. 检查用户的每秒请求次数是否达到上限
   	3. 检查验证码是否正确
   	4. 检查对应秒杀商品的内存标记，如果该商品秒杀已结束，直接返回
   	5. 检查是否重复秒杀（即redis中是否已有相关订单信息）
   	6. redis预减库存
    7. MQ入队异步处理秒杀订单

&nbsp;&nbsp;该部分业务处理逻辑的代码如下：

&nbsp;&nbsp;P.S.为避免篇幅过长，各方法的实现请参考源码

```java
    @Limit(seconds = 1, maxCount = 5)
    @PostMapping("/do")
    public ResultMsg<Integer> doSeckill(Model model, User user,
                  @RequestParam("goodsId")long goodsId,
                  @RequestParam(value="verifyCode", defaultValue="0")String verifyCode) {
      model.addAttribute("user", user);
      if (user == null) {
         return ResultMsg.error(ErrorMsg.SESSION_ERROR);
      }
      boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
      if(!check) {
         return ResultMsg.error(ErrorMsg.VERIFY_CODE_ERROR);
      }
      // 判断内存标记
      boolean isOver = localOverSet.contains(goodsId);
      if (isOver) {
         return ResultMsg.error(ErrorMsg.SECKILL_OVER);
      }
      // 判断是否重复秒杀
      Order preOrder = orderService.getOrderByUserIdAndGoodsId(user.getId(), goodsId);
      if (preOrder != null) {
         return ResultMsg.error(ErrorMsg.REPEATED_SECKILL);
      }

      // redis预减库存
      long stock = redisService.decr(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goodsId));
      if (stock < 0) {
         localOverSet.add(goodsId);
         return ResultMsg.error(ErrorMsg.SECKILL_OVER);
      }
        
      // RabbitMQ 入队
      MQMsg msg = new MQMsg();
      msg.setUser(user);
      msg.setGoodsId(goodsId);
      sender.sendMsg(msg);
      return ResultMsg.success(0);  //排队
   }
```

&nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; 用户发送秒杀请求后，前台进入轮询状态，通过接口检查是否秒杀成功。

&nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; 注意此处有个优化点：通过设置MQ中消息的TTL并通过死信交换机的方式防止用户等待时间过长。

```java
    /**
    * 返回处理结果
    * orderId：成功
    * -1：秒杀失败
    * 0： 排队中
    */
   @GetMapping("/result")
   public ResultMsg<Long> seckillResult(Model model,User user,
                       @RequestParam("goodsId")long goodsId) throws ExecutionException, InterruptedException {
      model.addAttribute("user", user);
      if(user == null) {
         return ResultMsg.error(ErrorMsg.SESSION_ERROR);
      }
      long result  = seckillService.getSeckillResult(user.getId(), goodsId);
      return ResultMsg.success(result);
   }
```

### 总结

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;做本项目的初衷是为了寻找一种高并发系统的解决方案，秒杀是常见的高并发场景，因此本项目以秒杀作为业务场景。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;秒杀场景首先要保证业务逻辑的正确性，然后尽可能提高系统QPS。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;超卖、少卖是秒杀场景常见的业务逻辑错误，出现这种情况的后果非常严重。仔细分析造成超卖、少卖的原因不难发现，秒杀的步骤：检查库存、减库存、生成秒杀订单并不是原子性操作。这个问题一般可以通过redis缓存的原子操作decr预减库存（redis底层单线程模型）、lua脚本或分布式锁来实现。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;大型秒杀系统的瞬间流量非常之大，又鉴于秒杀场景有特定时间点的特点，因此本项目就采用定时任务进行缓存预热，同时应在网关层就进行限流操作，将大部分流量阻挡在上游，这里有非常著名的漏桶和令牌桶算法，也可用随机快速失败、哈希或根据负载情况动态失败等策略。在处理秒杀请求时使用MQ做异步、削峰，应用MQ的消息确认机制异步的反馈结果，当然这里也要设置一个阈值，防止用户等待时间过久。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;充分利用缓存也十分重要，用户秒杀成功的订单可以缓存在redis中，并设置一个有效期，比如参考淘宝的做法可以设置15分钟，用户秒杀成功后须在15分钟内支付，否则订单失效；支付成功的订单则可以持久化到MySQL中。考虑到秒杀成功的用户量非常有限，并且用户的支付过程也是异步的，直接持久化到MySQL是可行的。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;另外为了保证系统的高可用性、容灾，缓存、MQ等关键中间件常使用集群部署，集群分为主、从和哨兵，通过主从复制、投票机制等方式保证单个机器的下线不会对整个系统造成影响，同时持久化机制也是系统关键数据不丢失的重要保证。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;此外，对于大型电商系统而言，服务的降级、熔断等机制也是保证系统高可用、提高QPS的重点，这里推荐一本书《大型网站技术架构：核心原理与案例分析》，作者是曾是阿里巴巴架构师，该书从大型网站的演变讲到技术的发展再到现如今应用广泛的分布式架构，也以淘宝为例，讲述怎样从零几年的单体应用一步步演变到如今复杂的架构，对理解大型网站的架构十分有帮助，也有助于针对性学习一门技术并深刻理解该技术的使用场景和价值。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本项目从简单完成功能到优化QPS断断续续进行了三个多月时间，一方面学习秒杀系统的设计架构，另一方面学习有关技术，虽然相较于大型秒杀系统，本项目实属简陋，但就现有技术栈而言，已经尽可能做到了最优化。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;我觉得书中有句话说的很好：技术是拿来解决问题的，而不是拿来秀的。我们学习技术时务必先了解技术的原理、使用场景、为什么要用、可以解决哪些问题等，而不是一股脑地把各种技术往项目中堆积，就比如一个公司内部使用的ERP系统，如果采用秒杀系统的架构，纯属浪费资源。

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本项目开源希望与大家学习交流，有什么问题、建议或意见都可及时反馈，路漫漫其修远兮，愿与大家共同进步！

