# 项目说明

本项目基于苍穹外卖项目，将各种依赖项升级至较新的版本，包括使用Spring Boot 3, MyBatis Plus 3.5, OpenAPI 3等。文档编写和注释均不详细，



# Day 1

## 前端环境

在资源管理器中打开 `nginx-1.20.1` 文件夹，双击 `nginx.exe` 文件运行（提前迁移到非中文目录），在浏览器中访问localhost:
80，即可查看前端页面。

## Git推送

在终端中输入以下命令生成密钥对，然后将生成的公钥添加到GitHub账户中

```bash
ssh-keygen -t rsa
```

然后在终端中输入以下命令连接到GitHub

```bash
ssh -T git@github.com
```

如果提示 `Hi username! You've successfully authenticated, but GitHub does not provide shell access.`
则表示连接成功。

无法推送时，向host中添加

```bash
140.82.113.4 github.com
```

然后在终端中清除DNS缓存

```bash
ipconfig /flushdns
```

## 导入依赖修改

项目从**Spring Boot 2.x**升级至**Spring Boot 3.x**，支持**OpenDoc 3**，需要修改部分依赖，注意子模块下的 `pom.xml`
文件也需要进行修改：

```xml

<properties>
    <java.version>17</java.version>
    <mybatis.spring>3.5.12</mybatis.spring>
    <lombok>1.18.38</lombok>
    <druid>1.2.27</druid>
    <pagehelper>2.1.1</pagehelper>
    <aliyun.sdk.oss>3.10.2</aliyun.sdk.oss>
    <knife4j>4.4.0</knife4j>
    <jjwt>0.12.7</jjwt>
</properties>
<dependencyManagement>
<dependencies>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>${mybatis.spring}</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-jsqlparser</artifactId>
        <version>${mybatis.spring}</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-generator</artifactId>
        <version>${mybatis.spring}</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-3-starter</artifactId>
        <version>${druid}</version>
    </dependency>
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>${knife4j}</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt}</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>${jjwt}</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>${jjwt}</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
</dependencyManagement>
```

## 查看接口

`/v3/api-docs`, `/swagger-ui.html`, `/doc.html` 等接口可以在浏览器中访问，查看接口文档。

## 完善登录功能

目前存在的问题是，员工表 `employee` 中的 `password` 字段是明文存储的，应该使用加密算法进行加密存储。

## 踩的一些坑

### 端口占用问题

项目实际上是先走的nginx的反向代理，将前端发送的动态请求由nginx转发到后端服务器。
前端访问的是 `http://localhost/api/**` ，然后转发到了后端服务器的 `http://localhost:8080/admin/**` 进行处理。

使用nginx反向代理的优点：

- 提高访问速度，可以在nginx中配置缓存，减少后端服务器的压力。
- 可以通过nginx配置负载均衡，将请求分发到多个后端服务器上，提高系统的可用性和扩展性。
- 可以通过nginx配置SSL证书，实现HTTPS访问，提高数据传输的安全性。

通过nginx启动前端页面时，可能会出现端口被占用的情况。可以通过以下命令查看端口占用情况：

```bash
netstat -ano | findstr :8080
```

如果发现端口被占用，可以通过以下命令结束占用该端口的进程：

```bash
taskkill /F /PID <PID>
```

如果无法结束进程，一般是本机上安装的Tomcat或其他服务占用了该端口，有两种解决方案：

- Win+R打开运行窗口，输入 `services.msc` ，停止Tomcat服务
- 在nginx配置文件中修改端口号，找到 `nginx.conf` 文件中下列的字段，将端口号修改为其他端口：

```nginx configuration
location /api/ {
    proxy_pass   http://localhost:8080/admin/;
    #proxy_pass   http://webservers/admin/;  # 如果配置了负载均衡，则使用此行
}
```

配置负载均衡的方法，默认配置是轮询，也可以配置权重 `weight` 、根据ip `ip_hash` 、根据最少连接 `least_conn` 、根据url
`url_hash` 、根据响应时间 `fair` 等方式分配转发到的后端服务器。

```nginx configuration
upstream webservers {
    # 如果有多个后端服务器，可以声明多台服务器，也可以配置权重策略
    server 127.0.0.1:8080 weight=90;
    #server 127.0.0.1:8081 weight=10;  
}
```

### 导入依赖和配置修改

一般来说，源代码中需要修改的地方有

- `javax.servlet` 改为 `jakarta.servlet`
- `@ApiModel` 和 `@ApiModelProperty` 改为 `@Schema`
- `@Api` 改为 `@Tag`
- `@ApiOperation` 改为 `@Operation`
- 添加新的 `Knife4jConfig.java` 进行符合OpenAPI 3规范的配置
- 在 `application.yml` 中添加 `springdoc`, `knife4j` 的配置
- 添加Spring Boot的 `banner-mode: off` 和Mybatis Plus的 `banner: false`
- JWT相关的依赖需要添加 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 三个依赖

### Knife4J配置静态资源映射

不添加 `addResourceHandler` 方法的话，访问 `/doc.html` 时会出现404错误。

这是因为如果不进行静态资源配置的话，Spring Boot会默认将请求交给 `Controller` 进行处理，而 `/doc.html`
是一个静态资源文件，需要通过静态资源映射来访问。
添加以下代码到 `Knife4jConfig.java` 中：

```java
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
}
```

### JWT的使用方法变化

jjwt迁移到0.12.x后，使用方法发生了变化

- 构建JWT的方法

```java
// 注意secretKey的长度应至少是32字节（256bit）
public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
    // 1. 生成密钥对象
    // 对于HS256算法，密钥的字节长度至少应该是 256位（32字节）
    // Keys.hmacShaKeyFor() 方法会确保密钥长度的安全性
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    // 2. 计算过期时间
    Instant now = Instant.now();
    Date exp = Date.from(now.plus(ttlMillis, ChronoUnit.MILLIS));

    // 3. 构建JWT
    return Jwts.builder()
            .claims(claims)  // 设置自定义声明
            .issuedAt(Date.from(now))  // 设置签发时间
            .expiration(exp)  // 设置过期时间
            .signWith(key)  // 使用生成的密钥和HS256算法进行签名
            .compact();  // 生成JWT字符串
}
```

- 解析JWT的方法

```java
public static Claims parseJWT(String secretKey, String token) {
    // 1. 根据密钥字符串生成 SecretKey 对象
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    // 2. 创建一个 JwtParser 实例
    // 使用 parserBuilder() 来构建解析器
    JwtParser parser = Jwts.parser()
            .verifyWith(key) // 使用 verifyWith 设置验证密钥
            .build();

    // 3. 解析JWT
    // parseSignedClaims(token) 会验证签名并返回包含Claims的Jws对象
    Jws<Claims> jws = parser.parseSignedClaims(token);

    // 4. 返回Claims
    return jws.getPayload();
}
```

### Logback/Log4j乱码问题

在 `src/main/resources` 目录下创建 `logback-spring.xml` 文件，添加以下内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!--注意一定不能换行-->
            <pattern>%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p})
                %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan}
                %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}
            </pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

# Day 2

## 使用前的注意

由于已经使用了JWT令牌进行身份验证，所以在使用前端页面时需要先调用登录接口 `/admin/employee/login`
获取JWT令牌，然后将token交给全局参数进行管理，每次请求时可以自动加到请求头中，如果不起作用则刷新页面。

## 新增员工接口 `POST /admin/employee`

### 实现

在 `EmployeeController.java` 中添加新增员工的接口方法 `saveEmployee` ，它接收一个前端传来的 `EmployeeDTO` 对象，使用
`@RequestBody` 注解来接收请求体中的JSON数据。方法中调用服务层的 `employeeService.saveEmployee(employeeDTO)` 来保存员工信息。

服务层使用hutool的 `BeanUtil.copyProperties` 方法将 `EmployeeDTO` 转换为 `Employee` 实体类，并设置其他字段，然后调用
`employeeMapper.insert(employee)` 方法将员工信息插入到数据库中，这个方法是通过继承 `BaseMapper<Employee>` 得来的。

### 创建者和更新者的处理

这个功能需要实现动态获取当前登录员工的id，首先需要明确JWT进行身份验证的方式，通常是将JWT令牌存储在浏览器的 `localStorage`
或 `sessionStorage` 中，然后在每次请求时将其添加到请求头中。

|      前端       |                     行为                     |        后端        |
|:-------------:|:------------------------------------------:|:----------------:|
|  首次发起用户认证请求   | -----------------提交用户名和密码----------------> |       认证通过       |
| 本地保存JWT Token |  <-----------生成JWT Token返回给前端------------  |   生成JWT Token    |
|  后续登录请求后端接口   |       ----每次请求都在请求头中携带JWT Token--->        | 拦截请求验证JWT Token  |
|     展示数据      |   <-------------------------------------   | 如果通过，执行业务逻辑，返回数据 |
| 展示错误信息，返回登录页面 |   <-------------------------------------   |   如果不通过，返回错误信息   |

那么想要获取 `create_user` 字段，就需要在首次发起用户认证请求，也就是创建用户时进行拦截。而获取 `update_user`
字段则需要在后续登录请求中在JWT拦截其中进行拦截，校验用户在请求头中携带的JWT Token，获取用户id。

我们已经在 `JwtTokenAdminInterceptor.java` 中实现了JWT的拦截器 `preHandle` 方法，获取到了一个 `Long empId`
的值，这个值就是当前登录员工的id，目前的问题是如何将这个值传递到服务层的 `saveEmployee` 方法中。

可以使用ThreadLocal来存储当前登录员工的id，这样在服务层就可以直接获取到这个值。ThreadLocal为每个线程提供了一个独立的变量副本，适合存储当前线程的上下文信息。只有在线程内才能获取到对应的值，线程外不能访问，有线程隔离的效果。而Spring
Boot的每个请求都是在独立的线程中处理的，所以可以使用ThreadLocal来存储当前登录员工的id。

为证明这一点，在拦截器、控制层和服务层的对应方法中都打印当前线程ID和方法名称，控制台的输出如下

```
70 - JwtTokenAdminInterceptor preHandle
70 - EmployeeController saveEmployee
70 - EmployeeServiceImpl saveEmployee
```

而**每次发起请求时，线程ID是不同的**。

由于此处实现的是创建用户的功能，所以两个字段 `create_user` 和 `update_user` 都是当前登录员工的id。

## 分页查询 `GET /admin/employee/page`

### 实现

关键思想：将前端页面传来的分页参数和最终返回的分页结果分别封装到两个实体对象 `EmployeePageQueryDTO` 和 `PageResult<V>`
中，其中用于接收前端页面的DTO实体保存了根据 `name` 字段模糊查询的条件、分页参数 `page` 和 `pageSize`，而返回的分页结果实体则保存了总记录数
`total` 、总页数 `pages` 和分页数据 `records`。

在 `EmployeeController.java` 中添加新增员工的接口方法 `queryEmployeesPage` ，它接收一个前端传来的 `EmployeePageQueryDTO`
对象，使用 `@ParameterObject` 注解使用Spring Boot的参数对象功能，自动将前端传来的分页参数封装到该对象中。方法中调用服务层的
`employeeService.queryEmployeesPage(employeePageQueryDTO)` 来查询员工信息。

通过Mybatis Plus的实现非常简单，最后的 `PageResult.of(queryPage)` 用来将查询到的分页结果通过 `getRecords()` 方法转换成
`List<Employee>` 的结果列表，然后封装进 `PageResult<Employee>` 对象中返回，注意判空判null。

```java
public PageResult<Employee> queryEmployeesPage(EmployeePageQueryDTO employeePageQueryDTO) {
    int page = employeePageQueryDTO.getPage();
    int pageSize = employeePageQueryDTO.getPageSize();
    String name = employeePageQueryDTO.getName();
    Page<Employee> p = Page.of(page, pageSize);
    Page<Employee> queryPage = lambdaQuery()
            .like(name != null && !name.isEmpty(), Employee::getName, name)
            .page(p);
    return PageResult.of(queryPage);
}
```

### 原理

MP的的分页查询通过注册一个 `PaginationInnerInterceptor` 拦截器来实现的，这个拦截器会监听所有MyBatis即将执行的SQL操作。

当调用一个传入 `IPage` 对象的Mapper方法时（在代码中为 `.page(p)` ），MP会通过 `ThreadLocal`
（底层实际就是个Map）将该分页对象存储起来，确保分页参数在同一个线程内进行传递，无需通过方法参数传递，发生在
`PaginationInnerInterceptor` 内。

当 `lambdaQuery()` 构建的查询即将被MyBatis的 `Executor` 执行时，分页拦截器会接入，检查当前线程中的 `ThreadLocal`
是否有分页对象。如果有，它会修改SQL语句，添加 `LIMIT` 和 `OFFSET` 子句来实现分页查询。
这意味着，MP的分页查询实际上是通过修改SQL语句来实现的，而不是通过在Java代码中手动处理分页逻辑。

### 时间转换

在前端页面中希望时间显示为 `yyyy-MM-dd HH:mm:ss` 的格式，可以在 `Employee.java` 实体类中添加 `@DateTimeFormat` 或
`@JsonFormat` 注解来指定时间格式。

但这种方法的问题是每个时间字段都需要添加注解，比较麻烦。可以在 `WebMvcConfiguration` 中配置一个Jackson的对象转换器，通过定义一个
`Jackson2ObjectMapperBuilderCustomizer` 的Bean来自定义全局的时间格式。

```java

@Bean
public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> {
        builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
    };
}
```

## 修改员工信息 `GET /admin/employee/{id}` `PUT /admin/employee`

### 实现

这两个接口的实现比较简单，直接调用 `BaseMapper` 的 `selectById` 和 `updateById` 方法即可。注意更新时需要将 `update_user` 和
`update_time` 字段设置为当前登录员工的id和当前时间。

```java

@Override
public Integer editEmployee(EmployeeDTO employeeDTO) {
    Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
    employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(BaseContext.getCurrentId());
    return employeeMapper.updateById(employee);
}
```

### 技术细节

建议在 `Employee` 实体类的 `password` 字段上添加 `@JsonIgnore`
注解，这样在序列化时会忽略该字段，避免将密码（无论是明文还是加密后的，加密也有机会使用哈希碰撞进行破解）暴露给前端，进一步增加安全性。

## 分类管理模块的各项功能

### 实现

创建、继承并实现以下几个类以使用MyBatis Plus的自带接口，其他的参考员工管理模块即可。一个新模块的创建也可以参考以下步骤。

|          类名           |                   继承                    |        实现         |       自动装配        |
|:---------------------:|:---------------------------------------:|:-----------------:|:-----------------:|
| `CategoryController`  |                    /                    |         /         | `CategoryService` |
|   `CategoryService`   |          `IService<Category>`           |         /         |         /         |
| `CategoryServiceImpl` | `ServiceImpl<CategoryMapper, Category>` | `CategoryService` | `CategoryMapper`  |
|   `CategoryMapper`    |         `BaseMapper<Category>`          |         /         |         /         |