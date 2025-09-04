# 项目说明

本项目基于苍穹外卖项目，将各种依赖项升级至较新的版本，包括使用Spring Boot 3, MyBatis Plus 3.5, OpenAPI 3等。

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

### Spring Boot 3整合Knife4j 4

参阅：

https://blog.csdn.net/zzm_0525/article/details/140256118

## 完善登录功能

目前存在的问题是，员工表 `employee` 中的 `password` 字段是明文存储的，应该使用加密算法进行加密存储。

## 常见问题的解决方案

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

一般来说，源代码和配置中需要修改和增加的地方有

- `javax.servlet` 改为 `jakarta.servlet`
- `@ApiModel` 和 `@ApiModelProperty` 改为 `@Schema`
- `@Api` 改为 `@Tag`
- `@ApiOperation` 改为 `@Operation`
- 添加新的 `Knife4jConfig.java` 进行符合OpenAPI 3规范的配置
- 在 `application.yml` 中添加 `springdoc`, `knife4j` 的配置
- 添加Spring Boot的 `banner-mode: off` 和Mybatis Plus的 `banner: false`
- JWT相关的依赖需要添加 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 三个依赖

### Spring MVC配置静态资源映射

不添加 `addResourceHandler` 方法的话，访问 `/doc.html` 时会出现404错误。

这是因为如果不进行静态资源配置的话，Spring Boot会默认将请求交给 `Controller` 进行处理，而 `/doc.html`
是一个静态资源文件，需要通过静态资源映射来访问。
添加以下代码到 `WebMvcConfiguration.java` 中：

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

### 关闭各种横幅logo并启用MyBatis日志

在本项目中，通过配置yaml，Spring和Mybatis Plus的横幅logo都可以关闭，也可以开启Mybatis的SQL日志。

```yaml
spring:
  main:
    banner-mode: "off"
mybatis-plus:
  global-config:
    banner: false
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

Pagehelper的横幅logo需要配置Spring Boot服务选项，在修改选项-添加虚拟机选项中添加 `-Dpagehelper.banner=false` 选项即可。

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

```text
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

### 注意事项

在删除分类前，需要先判断该分类下是否有菜品存在，如果有则不能删除。那么需要创建 `Dish` 和 `Setmeal` 对应的Mapper接口，然后再调用对应的
`selectCount` 方法查询对应分类id的菜品条数，如果不为0则不能删除。

```java

@Override
public Integer deleteCategory(Long id) {
    // 如果分类关联有菜品那么就不能删除
    Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
    if (count > 0) {
        throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
    }

    count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
    if (count > 0) {
        throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
    }

    return categoryMapper.deleteById(id);
}
```

# Day 3

## 代码重构 - 公共字段填充

### 使用Mybatis Plus实现

Mybatis Plus提供了公共字段填充的功能，可以通过实现 `MetaObjectHandler` 接口来实现自动填充公共字段。

首先创建一个公共字段实体 `BaseEntity.java`，包含 `create_time`、`create_user`、`update_time` 和 `update_user`
字段。针对不同的CRUD方法，需要在字段上标注 `@TableField(fill = FieldFill.XXX)` ， 然后让 `Employee` 和 `Category` 实体类继承
`BaseEntity` 类，同时删掉这两个实体类中对应的字段，防止配置覆盖，将来处理其他有这四个字段的数据表对应的实体类时，也按同样方式进行处理。

```java

@Data
public class BaseEntity implements Serializable {

    @TableField(fill = FieldFill.INSERT) // 插入时填充
    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) // 插入时填充
    @Schema(description = "创建人ID", example = "1")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
    @Schema(description = "修改人ID", example = "1")
    private Long updateUser;
}
```

然后创建一个公共字段填充器 `BaseEntityMetaObjectHandler.java` 实现 `MetaObjectHandler` 接口，重写 `insertFill` 和
`updateFill` 方法。

```java

@Component
@Slf4j
public class BaseEntityMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createUser", BaseContext::getCurrentId, Long.class);
        this.strictInsertFill(metaObject, "updateUser", BaseContext::getCurrentId, Long.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateUser", BaseContext::getCurrentId, Long.class);
    }
}
```

最后删除Service中的对应代码即可。

### 使用Spring Boot实现

首先创建一个自定义枚举类和注解，标识哪些方法需要被AOP切面拦截并处理。

```java
public enum OperationType {
    INSERT,
    UPDATE
}
```

```java

@Target(ElementType.METHOD) // 注解作用于方法
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperationType value(); // 通过 value 属性指定操作类型 (INSERT 或 UPDATE)
}
```

然后创建AOP切片类，用来拦截带有 `@AutoFill` 注解的方法，并在方法执行前后进行公共字段的填充。

```java

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // 定义切点，拦截所有 com.sky.mapper 包下二级目录和被 @AutoFill 注解标记的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    // 定义前置通知，在目标方法执行前执行
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        // 1. 获取注解的操作类型 (INSERT/UPDATE)
        // 方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法上的注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 获得注解的操作类型
        OperationType operationType = autoFill.value();

        // 2. 获取方法的参数 (实体对象)
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0]; // 约定第一个参数为实体对象

        // 3. 准备要填充的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 4. 根据操作类型，通过反射为对象属性赋值
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AOP自动填充[INSERT]异常", e);
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AOP自动填充[UPDATE]异常", e);
            }
        }
    }
}
```

最后在Service方法上添加 `@AutoFill` 注解，指定操作类型为 `INSERT` 或 `UPDATE`，删除原方法中的对应代码即可。

## 图片上传接口 `POST /admin/common/upload`

### 实现

与教程的实现有所区别，该项目的实现是将图片保存在本地资源路径中，并返回图片的访问路径。前端页面可以通过该路径来展示图片。首先需要配置保存到的路径和访问的路径，这两个配置在Controller中分别用
`@Value` 注解自动注入为 `basePath` 和 `urlPrefix` 两个对象。

```yaml
sky:
  upload:
    # 文件保存的绝对路径 (注意：即使在Windows下，也推荐使用正斜杠 /)
    path: .../sky-take-out/resources/images/
    # 对外暴露的访问路径前缀
    url-prefix: /images/
```

然后在 `CommonController.java` 中添加上传图片的接口方法 `uploadImage`，它接收一个 `MultipartFile` 类型的参数 `file`
。方法中将文件保存到指定路径，并返回图片的访问路径。部分代码可以进一步封装为工具类，再次不再赘述，只展示一个可用的方法。

```java

@PostMapping("/upload")
@Operation(summary = "上传图片", description = "提供图片上传功能")
public Result<String> uploadImage(MultipartFile file) {
    log.info("上传图片：{}", file);

    // 1. 生成唯一文件名，防止重名覆盖
    String originalFilename = file.getOriginalFilename();

    // 提取文件后缀，例如 .jpg
    String extension = null;
    if (originalFilename != null) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    // 使用UUID生成新的文件名
    String newFileName = UUID.randomUUID() + extension;
    log.info("新文件名为：{}", newFileName);

    // 2. 创建文件保存的目录
    File dir = new File(basePath);
    // 如果目录不存在，则创建它
    if (!dir.exists()) {
        dir.mkdirs();
    }

    try {
        // 3. 将临时文件转存到指定位置
        File destFile = new File(basePath + newFileName);
        file.transferTo(destFile);
        log.info("文件上传成功，保存路径：{}", destFile.getAbsolutePath());

        // 4. 拼接可供外部访问的URL并返回
        String accessUrl = urlPrefix + newFileName;
        log.info("文件访问URL：{}", accessUrl);
        return Result.success(accessUrl);

    } catch (IOException e) {
        log.error("文件上传失败", e);
        // throw new UploadException(MessageConstant.UPLOAD_FAILED); // 建议抛出自定义异常，由全局异常处理器捕获
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
```

接着在 `WebMvcConfiguration.java` 中添加静态资源映射
`registry.addResourceHandler(urlPrefix + "**").addResourceLocations("file:" + uploadPath);` ，将上传的图片目录映射到
`/images/` 路径下，这样前端就可以通过访问 `/images/xxx.jpg`来获取上传的图片，其中 `**` 代表扫描所有子目录和文件， `file:`
表示从本地文件系统中加载资源。

最后还需要在nginx配置中配置 `/images/` 的访问路径，本项目中配置到localhost的8080端口上即可。

## 菜品管理模块

### 新增菜品接口 `POST /admin/dish`

业务规则

1. 菜品名称必须唯一
2. 菜品分类必须存在
3. 新增菜品时可以选择口味
4. 每个菜品必须对应一张图片

### 分页查询菜品接口 `GET /admin/dish/page`

业务规则

1. 根据页码展示菜品信息
2. 每页展示10条数据
3. 分页查询时可以根据需要输入菜品名称、菜品分类、菜品状态进行查询

### 删除菜品接口 `DELETE /admin/dish`

业务规则

1. 一次可以删除一个菜品，也可以批量删除
2. 起售中的菜品不能删除
3. 被套餐关联的菜品不能删除
4. 删除后一并删掉菜品口味

### 实现

见源码，该模块依赖 `Category` 模块， `DishFlavor` 模块， `Setmeal` 模块的部分实现。注意多表联查需要添加 `@Transactional`
事务注解，确保单次请求的数据一致性。

# Day 4

## 套餐管理模块

与菜品管理模块类似，套餐管理模块也需要实现新增、分页查询、删除、修改和起售停售等功能。具体内容见源码。

# Day 5

## Redis入门

### 简介

Redis是一个**基于内存的**高性能**键值对（key-value）**数据库，相比之下，SQL是基于磁盘存储的关系型数据库，数据存储在二维表中。

项目地址：

https://github.com/redis/redis

Windows版：

https://github.com/tporadowski/redis

### 安装

安装方法基于Windows上运行的WSL2环境，总结自

https://developer.aliyun.com/article/1672659

https://blog.csdn.net/m0_47292890/article/details/148669149

#### 检查Docker源

如果出现如下问题，则考虑换源

```terminaloutput
Error response from daemon: Get "https://registry-1.docker.io/v2/": context deadline exceeded (Client.Timeout exceeded while awaiting headers)
```

换源方法

```bash
sudo mkdir -p /etc/docker
vim /etc/docker/daemon.json  
```

加入如下配置

```json
{
  "registry-mirrors": [
    "https://docker.registry.cyou",
    "https://docker-cf.registry.cyou",
    "https://dockercf.jsdelivr.fyi",
    "https://docker.jsdelivr.fyi",
    "https://dockertest.jsdelivr.fyi",
    "https://mirror.aliyuncs.com",
    "https://dockerproxy.com",
    "https://mirror.baidubce.com",
    "https://docker.m.daocloud.io",
    "https://docker.nju.edu.cn",
    "https://docker.mirrors.sjtug.sjtu.edu.cn",
    "https://docker.mirrors.ustc.edu.cn",
    "https://mirror.iscas.ac.cn",
    "https://docker.rainbond.cc",
    "https://do.nark.eu.org",
    "https://dc.j8.work",
    "https://dockerproxy.com",
    "https://gst6rzl9.mirror.aliyuncs.com",
    "https://registry.docker-cn.com",
    "http://hub-mirror.c.163.com",
    "http://mirrors.ustc.edu.cn/",
    "https://mirrors.tuna.tsinghua.edu.cn/",
    "http://mirrors.sohu.com/"
  ],
  "insecure-registries": [
    "registry.docker-cn.com",
    "docker.mirrors.ustc.edu.cn"
  ],
  "debug": true,
  "experimental": false
}
```

重新载入、重启Docker、验证生效

```bash
# 重新载入配置
sudo systemctl daemon-reload

# 重启Docker
sudo systemctl restart docker

# 验证配置是否生效
docker info
docker compose up -d
```

#### 拉取镜像

```bash
# 拉取指定版本镜像
docker pull redis:8.2.1
 
# 验证镜像
docker images -a
# 输出示例：
# REPOSITORY   TAG       IMAGE ID       CREATED      SIZE
# redis        8.2.1     9d1fe3a9a889   4 days ago   137MB
```

#### 添加配置

首先创建目录

```bash 
mkdir -p ~/data/dockerData/redis/{conf,data,logs}  
touch ~/data/dockerData/redis/conf/redis.config
```

创建配置

```text
# Redis服务器配置 

# 绑定IP地址
#解除本地限制 注释bind 127.0.0.1  
#bind 127.0.0.1  

# 服务器端口号  
port 6379 

# 配置密码，不要可以删掉
requirepass testpassword

# 关闭保护模式，允许外部网络访问
protected-mode no


# 这个配置不要和docker -d 命令 冲突
# 服务器运行模式，Redis以守护进程方式运行,默认为no，改为yes意为以守护进程方式启动，可后台运行，除非kill进程，改为yes会使配置文件方式启动redis失败，如果后面redis启动失败，就将这个注释掉
daemonize no

# 当Redis以守护进程方式运行时，Redis默认会把pid写入/var/run/redis.pid文件，可以通过pidfile指定(自定义)
#pidfile /data/dockerData/redis/run/redis6379.pid  

# 默认为no，redis持久化，可以改为yes
appendonly yes


# 当客户端闲置多长时间后关闭连接，如果指定为0，表示关闭该功能
timeout 60
# 服务器系统默认配置参数影响 Redis 的应用
maxclients 10000
tcp-keepalive 300

# 指定在多长时间内，有多少次更新操作，就将数据同步到数据文件，可以多个条件配合（分别表示900秒（15分钟）内有1个更改，300秒（5分钟）内有10个更改以及60秒内有10000个更改）
save 900 1
save 300 10
save 60 10000

# 按需求调整 Redis 线程数
tcp-backlog 511


# 设置数据库数量，这里设置为16个数据库  
databases 16


# 启用 AOF, AOF常规配置
appendonly yes
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb


# 慢查询阈值
slowlog-log-slower-than 10000
slowlog-max-len 128


# 是否记录系统日志，默认为yes  
syslog-enabled yes  

# 指定日志记录级别，Redis总共支持四个级别：debug、verbose、notice、warning，默认为verbose
loglevel notice

# 日志输出文件，默认为stdout，也可以指定文件路径  
logfile stdout

# 日志文件
#logfile /var/log/redis/redis-server.log


# 系统内存调优参数   
# 按需求设置
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-entries 512
list-max-ziplist-value 64
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
```

#### 启动容器

```bash
docker run \
  -p 6379:6379 \
  --name redis \
  -v ~/data/dockerData/redis/conf/redis.config:/etc/redis/redis.conf \
  -v ~/data/dockerData/redis/data:/data \
  -v ~/data/dockerData/redis/logs:/logs \
  -d \
  redis:8.2.1 \
  redis-server /etc/redis/redis.conf
```

| 参数	              | 作用             |
|:-----------------|----------------|
| -d	              | 后台运行           |
| --privileged	    | 赋予容器特权模式       |
| -p	              | 端口映射           |
| -v               | 	卷挂载（配置/数据/日志） |
| --restart=always | 	自动重启策略        |

#### 验证与调试

查看容器状态

```bash
# 查看运行状态
docker ps -a

# 输出示例
# CONTAINER ID   IMAGE          COMMAND                  CREATED        STATUS          PORTS                                         NAMES
# 911bdeb32b36   redis:latest   "docker-entrypoint.s…"   13 hours ago   Up 32 minutes   0.0.0.0:6379->6379/tcp, [::]:6379->6379/tcp   redis
```

WSL内测试客户端

```bash
# 启动容器
docker start redis

# 进入容器
docker exec -it redis bash
 
# 启动客户端
redis-cli
 
# 认证操作
127.0.0.1:6379> AUTH yourPassWord
OK
 
# 测试写入
127.0.0.1:6379> SET test_key "Hello Redis"
OK
 
# 验证读取
127.0.0.1:6379> GET test_key
"Hello Redis"

# 删除测试键
127.0.0.1:6379> DEL test_key
(integer) 1
```

#### Windows内测试客户端

可以使用Another Redis Desktop Manager图形化程序或命令行程序进行连接

Another Redis Desktop Manager下载地址：

https://github.com/qishibo/AnotherRedisDesktopManager

Redis Windows Version下载地址（>=Redis 6.0.20）：

https://github.com/redis-windows/redis-windows

Redis for Windows下载地址（Redis 4.0.14/5.0.14）：

https://github.com/redis-windows/redis-windows

将压缩包解压到任意目录下，然后将该目录添加到系统环境变量Path中，打开新的命令行窗口，输入 `redis-cli` 即可，使用方法与在Linux上相同。

### 常用数据类型

Redis存储的时key-value对，key是字符串类型，value可以是多种数据类型，常用的有5种。

#### 字符串 string

可以是字符串，也可以是数字（整数或浮点数），Redis可以将其作为字符串存储的同时，还能对它们执行原子性的增减操作。
类比 `String` ，或是 `Integer` 、 `AtomicLong` 等的包装类。

| 指令                               | 说明                                                  |
|:---------------------------------|-----------------------------------------------------|
| `SET key value`                  | 设置一个键值对。如果 `key` 已存在，则覆盖                            |
| `GET key`                        | 获取 `key` 对应的 `value`                                |
| `SETEX key seconds value`        | 设置一个带过期时间的键值对（EX = EXpire）                          |
| `SETNX key value`                | 设置一个键值对，仅当 `key` 不存在时才成功（NX = Not eXists）常用于实现分布式锁  |
| `INCR key`                       | 将 `key` 对应的数字 `value` 原子性加1。如果 `key` 不存在，则先初始化为0再加1 |
| `DECR key`                       | 将 `key` 对应的数字 `value` 原子性减1                         |
| `INCRBY key increment`           | 将 `key` 对应的数字 `value` 原子性地增加指定的整数                   |
| `MSET key value [key value ...]` | 一次性设置多个键值对                                          |
| `MGET key [key ...]`             | 一次性获取多个 `key` 的 `value`                             |

应用：

1. 缓存: 缓存用户信息、配置信息、页面片段、JSON字符串等。
2. 计数器: 网站访问量、文章点赞数、用户在线数等。
3. 分布式锁: 利用 `SETNX` 的特性，确保同一时间只有一个客户端能持有锁。
4. 共享Session: 存储Web应用的用户会话信息。

#### 散列 hash

字段（field）和值（value）的映射表，类似Java中的 `HashMap<String, String>` ，每个Redis的key对应一个Map实例。

| 指令                                       | 说明                       |
|:-----------------------------------------|--------------------------|
| `HSET key field value [field value ...]` | 将哈希表中一个或多个字段的值设为 `value` |
| `HGET key field`                         | 获取哈希表中指定字段的值             |
| `HMGET key field [field ...]`            | 获取哈希表中一个或多个字段的值          |
| `HGETALL key`                            | 获取哈希表中所有的字段和值            |
| `HDEL key field [field ...]`             | 删除哈希表中一个或多个字段            |
| `HKEYS key`                              | 获取哈希表中所有的字段              |
| `HVALS key`                              | 获取哈希表中所有的值               |
| `HINCRBY key field increment`            | 为哈希表中指定字段的整数值增加指定的增量     |

应用：

1. 对象缓存: 存储结构化数据，如用户信息、商品信息等。例如，一个 `user:123` 的key，其内部可以有 `name` , `age` , `email`
   等多个字段。相比于为每个字段都创建一个key，使用hash更节省内存和键空间。
2. 购物车: 用用户ID作为key，商品ID作为 `field` ，商品数量作为 `value` 。

#### 列表 list

字符串列表，按照插入顺序排序，可以在头部或尾部添加元素，类比Java中的 `LinkedList` 或者 `Deque` 。

| 指令                                  | 说明                                     |
|:------------------------------------|----------------------------------------|
| `LPUSH key element [element ...]`   | 从列表头部插入一个或多个元素                         |
| `RPUSH key element [element ...]`   | 从列表尾部插入一个或多个元素                         |
| `LPOP key [count]`                  | 从列表头部弹出一个或多个元素，如果是最后一个元素，则删除列表         |
| `RPOP key [count]`                  | 从列表尾部弹出一个或多个元素，如果是最后一个元素，则删除列表         |
| `BLPOP/BRPOP key [key ...] timeout` | LPOP/RPOP 的阻塞版本。如果列表为空，它会阻塞连接直到有新元素或超时 |
| `LRANGE key start stop`             | 获取列表中指定范围的元素（类似 `subList`）             |
| `LLEN key`                          | 获取列表的长度                                |

应用：

1. 消息队列/任务队列: 利用 `LPUSH` 生产消息，`BRPOP` 消费消息，实现简单高效的消息队列。
2. 时间线 (Timeline): 微博/朋友圈的关注列表，按时间顺序存储，`LPUSH` 发布新动态，`LRANGE` 分页查看。
3. 栈和队列: `LPUSH` + `LPOP` 实现栈 (FILO)，`LPUSH` + `RPOP` 实现队列 (FIFO)。

#### 集合 set

无序集合，其中每个元素在set中都是唯一的，类似Java中的 `HashSet<String>` 。

| 指令                             | 说明             |
|:-------------------------------|----------------|
| `SADD key member [member ...]` | 向集合添加一个或多个元素   |
| `SREM key member [member ...]` | 从集合中删除一个或多个元素  |
| `SPOP key [count]`             | 随机弹出一个或多个元素    |
| `SMEMBERS key`                 | 获取集合中的所有元素     |
| `SISMEMBER key member`         | 判断元素是否是集合的成员   |
| `SCARD key`                    | 获取集合的基数 (元素数量) |
| `SUNION key [key ...]`         | 获取多个集合的并集      |
| `SINTER key [key ...]`         | 获取多个集合的交集      |
| `SDIFF key [key ...]`          | 获取多个集合的差集      |

应用：

1. 标签系统: `SADD post:100 tag:java tag:redis`，为一篇文章添加标签。
2. 共同好友/关注: 使用 `SINTER` 计算两个用户的共同好友。
3. 抽奖系统: 存储所有参与抽奖的用户ID，使用 `SPOP` 或 `SRANDMEMBER` 随机抽取中奖用户。
4. 点赞/投票: 一个内容的点赞用户集合，天然去重。

#### 有序集合 sorted set (zset)

有序集合，同时每个元素会关联一个 `double` 类型的分数 (score)，集合中的元素按分数从小到大排序，类似Java中 `LinkedHashSet` 和
`TreeMap<Double, String>` 的结合，既保证的元素的唯一性，又能根据分数进行高效的排序和范围查找。

| 指令                                                              | 说明                          |
|:----------------------------------------------------------------|-----------------------------|
| `ZADD key [NX\|XX] [CH] [INCR] score member [score member ...]` | 向有序集合添加一个或多个成员，或者更新已存在成员的分数 |
| `ZREM key member [member ...]`                                  | 移除有序集合中的一个或多个成员             |
| `ZRANGE key start stop [WITHSCORES]`                            | 按分数从低到高返回指定排名范围的成员          |
| `ZREVRANGE key start stop [WITHSCORES]`                         | 按分数从高到低返回指定排名范围的成员          |
| `ZRANGEBYSCORE key min max [WITHSCORES]`                        | 按分数范围返回成员                   |
| `ZSCORE key member`                                             | 返回指定成员的分数                   |
| `ZCARD key`                                                     | 获取有序集合的成员数量                 |
| `ZINCRBY key increment member`                                  | 增加指定成员的分数                   |

应用：

1. 排行榜: 游戏积分榜、热搜榜、销售排行榜等。`ZREVRANGE` 可以轻松获取 Top-N 列表。
2. 带权重的任务队列: 分数作为优先级，分数越高的任务越先处理。
3. 范围查找: 例如查找某个价格区间或分数区间的商品/用户。

## 在Java中操作Redis

### 准备工作

引入依赖，其中 `jackson-datatype-jsr310` 是为了支持Java 8的时间类型序列化和反序列化的封装模块。

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.datatype</groupId>
<artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

配置数据源

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: password
```

编写配置类 `RedisConfiguration` ，创建 `RedisTemplate` 对象，然后将 `RedisTemplate` 注入到需要使用的类中即可。需要注意如果使用
`Jackson2JsonRedisSerializer` 来序列化和反序列化，还需要自定义一个 `ObjectMapper` 用来正确地识别Java 8的时间类型。具体见
`RedisConfiguration.java` 。

也可以直接注入Spring Data Redis已经封装好的 `StringRedisTemplate` ，它是 `RedisTemplate<String, String>`
的简化版，专门用于操作字符串类型的key和value，测试时会更加方便。

### 基本操作

假设已经注入了一个Redis模板对象 `redis` ，那么通过 `redis.opsForXXX()` 方法获取不同数据类型的操作对象，然后调用对应的方法进行操作。

注意如果自定义的Redis模板的值的序列化器为JSON的话，无法使用 `ops.increment()`
方法对数字类型的字符串进行自增操作，因为JSON序列化后的值是带引号的字符串，无法被识别为数字。推荐针对不同的业务需求使用不同的Redis模板对象，例如此时就可以使用封装好的
`StringRedisTemplate` 进行处理。

具体示例见测试类。

## 更改营业状态 `PUT /admin/shop/{status}`

通过修改营业状态为营业中或者打烊中，应当不仅能够影响前端页面的显示，还会影响小程序端用户下单的功能。而项目中约定了管理端发出的请求，统一使用
`/admin` 前缀，而小程序端的统一使用 `/user` 前缀。那么按照查询营业状态的功能来分类，就需要实现两个查询接口，一个是
`/admin/shop/status` ，另一个是 `/user/shop/status` 。

# Day 6

## HttpClient包

HttpClient是Apache提供的一个功能强大的HTTP客户端库，支持通过编码的方式发送HTTP请求和处理HTTP响应，封装了底层的网络通信细节，简化了HTTP操作。本项目使用当前最新的HttpClient5进行开发。

核心API包括：

1. `HttpClient`：用于发送HTTP请求和接收响应的主要接口。
2. `HttpClients`：用于创建 `HttpClient` 实例的工厂类。
3. `ClosableHttpClient`： `HttpClient` 的可关闭版本，使用后需要调用 `close()` 方法释放资源。
4. `HttpGet` 和 `HttpPost`：分别用于发送GET和POST请求
5. `HttpResponse`：表示HTTP响应，包含状态码、响应头和响应体等信息。

### 示例 - 发送一个GET请求

首先需要通过 `HttpClients.createDefault()` 创建一个 `HttpClient` 实例，然后创建一个请求对象（如 `HttpGet` 或 `HttpPost`
），最后调用 `execute()` 方法发送请求。

```java
public void testGET() {
    // 1. 创建httpclient对象
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        // 2. 创建get请求对象
        String url = "http://localhost:8080/user/shop/status";
        HttpGet httpGet = new HttpGet(url);
        System.out.println("执行请求: " + httpGet.getMethod() + " " + httpGet.getUri());

        // 3. 发送请求
        httpClient.execute(httpGet, response -> {
            // 4. 获取响应实体
            HttpEntity entity = response.getEntity();
            System.out.println("响应状态: " + response.getCode());
            System.out.println("响应内容长度: " + entity.getContentLength());
            System.out.println("响应内容: " + EntityUtils.toString(entity));
            return null;
        });
    } catch (Exception e) {
        log.error(e.getMessage());
    }
}
```

更简单地，也可以使用Hutool的HttpUtil工具类来发送请求

```java
public void testGETWithHutool() {
    String url = "http://localhost:8080/user/shop/status";
    String response = HttpUtil.get(url);
    System.out.println("响应内容: " + response);
}
```

### 示例 - 发送一个POST请求

考虑带有请求体的POST请求，与发送GET请求类似，但是需要多一步设置响应体的步骤。

```java
public void testPOST() {
    ObjectMapper mapper = new ObjectMapper();

    // 1. 创建httpclient对象
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        // 2.1 构造JSON响应体
        ObjectNode jsonNodes = mapper.createObjectNode();
        jsonNodes.put("username", "admin");
        jsonNodes.put("password", "123456");
        String json = new ObjectMapper().writeValueAsString(jsonNodes);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        // 2.2 设创建post请求对象，设置响应体
        String url = "http://localhost:8080/admin/employee/login";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        System.out.println("执行请求: " + httpPost.getMethod() + " " + httpPost.getUri());

        // 3. 发送请求
        httpClient.execute(httpPost, response -> {
            System.out.println(EntityUtils.toString(response.getEntity()));
            return null;
        });
    } catch (Exception e) {
        log.error(e.getMessage());
    }
}
```

类似地，Hutool也可以简化POST请求的发送：

```java
public void testPOSTWithHutool() {
    String url = "http://localhost:8080/admin/employee/login";
    String json = "{\"username\":\"admin\",\"password\":\"123456\"}";
    String response = HttpRequest.post(url)
            .body(json)  // 设置请求体
            .contentType(ContentType.APPLICATION_JSON.toString())  // 设置Content-Type
            .execute()
            .body();  // 获取响应体

    // String response = HttpUtil.post(url, json);  // 更简单的写法
    System.out.println("响应内容: " + response);
}
```

## 微信小程序开发入门

### 微信小程序的目录结构

小程序包含一个描述整体程序的 `app` 和多个描述各自页面的 `page` 组成。每个 `app` 和 `page` 都有自己的逻辑文件、配置文件和样式文件。

| 文件名      | 是否必需 | 作用                                |
|:---------|:-----|:----------------------------------|
| app.js   | 是    | 小程序逻辑文件，负责处理小程序的生命周期、全局数据等        |
| app.json | 是    | 小程序公共配置文件，定义小程序的页面路径、窗口表现、网络超时时间等 |
| app.wxss | 否    | 小程序公共样式表文件，定义小程序的全局样式，相当于CSS样式    |

对于每个 `page` ，都包含以下四个文件：

| 文件名       | 是否必需 | 作用                        |
|:----------|:-----|:--------------------------|
| page.js   | 是    | 页面逻辑文件，处理页面的生命周期、事件等      |
| page.wxml | 是    | 页面结构文件，定义页面的布局和内容，相当于HTML |
| page.json | 否    | 页面配置文件，定义页面的窗口表现、导航栏等     |
| page.wxss | 否    | 页面样式文件，定义页面的样式，相当于CSS样式   |

## 微信登录功能

### 登录流程

微信官方文档中提供了完整的登录流程说明：

https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html

在小程序端，调用 `wx.login()` 接口获取临时登录凭证（授权码） `code` ，然后将 `code` 通过 `wx.request()`
发送到后端开发者服务器，这是一个封装的异步请求，在uni-app中一般使用luch-request。后端服务器调用微信接口服务，传递 `appid` 、
`secret` 和 `code` 三个参数，获取用户的唯一标识 `openid` 和会话密钥 `session_key` 。后端服务器可以将这两个值存储到数据库、缓存中，生成
**自定义登录态**并产生一个关联两个值的token（如JWT）返回给小程序端，作为后续请求的身份凭证。小程序端收到token后，存储到本地（如
`localStorage` ），每次通过 `wx.request()` 发起后续请求时都携带这个token作为自定义登录态的凭证，后端服务器查询 `openid` 和
`session_key` 来验证token，以识别用户身份。如果用户身份被确认，那么就可以返回用户的个人信息等正常的业务数据。

### 手动获得标识

重新编译项目，弹出需要授权登录的信息后，打开控制台复制 `code` 的值，然后通过Postman发送如下GET请求，即可获取 `openid` 和
`session_key` 。注意请求返回的标识的有效期为5分钟，过期后需要重新获取。

```
https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code
```

### 代码实现

由于微信已经提供了完整的登录流程，后端只需要实现一个接口来接收小程序端传递过来的 `code` ，然后调用微信接口服务获取
`openid` 和 `session_key` 即可。如果是新用户，则需要注册并将用户身份信息保存到数据库中。具体实现可以使用HttpClient包。具体来说分为如下几步：

1. 使用HttpClient包调用微信接口服务获取 `openid`
2. `openid` 判空，如果为空则抛出异常
3. 根据 `openid` 查询用户信息，如果不存在则注册新用户
4. 创建JWT令牌并赋给新的 `UserLoginVO` 对象，返回结果
5. 创建拦截器，验证用户身份

## 商品浏览功能

见源码，涉及到查询分类、根据分类id查询菜品、根据分类id查询套餐以及根据套餐id查询菜品四个接口。

# Day 7

## Spring Cache

Spring Cache是Spring框架提供的一个缓存抽象层，简化了在Java应用中使用缓存的过程。它提供了一套统一的注解和接口，可以方便地将方法的结果缓存起来，从而提高应用的性能和响应速度。Spring
Cache支持多种缓存实现，如EhCache、Caffeine、Redis等。

### 核心注解

| 注解               | 说明                                                  |
|:-----------------|-----------------------------------------------------|
| `@EnableCaching` | 启用Spring Cache功能，通常添加在启动类上                          |
| `@Cacheable`     | 用于方法上，表示该方法的结果需要被缓存，如果缓存中存在则直接返回缓存结果，否则执行方法并将结果存入缓存 |
| `@CachePut`      | 用于方法上，表示该方法的结果需要被缓存，但无论缓存中是否存在，都会执行方法并更新缓存          |
| `@CacheEvict`    | 用于方法上，表示该方法执行后需要从缓存中移除指定的缓存项                        |
| `@Caching`       | 用于方法上，允许在一个方法上同时使用多个缓存注解                            |

## 缓存菜品信息

当很多用户同时浏览某个分类下的菜品时，如果每次都去数据库查询，势必会增加数据库的压力，影响系统的响应速度。为了解决这个问题，可以将热门分类下的菜品信息缓存到Redis中，当用户请求该分类的菜品时，后端服务先从Redis中获取，如果缓存命中则直接返回，否则再去数据库查询并将结果存入Redis。

### Redis缓存逻辑设计

#### 查询的业务逻辑

由于小程序前端的逻辑是根据分类id查询菜品，当点击某个菜品分类时，前端展示分类下的所有菜品，因此可以将缓存的key设计为
`dish_category_{categoryId}`
，value为该分类下的菜品列表的JSON字符串，最好分层存储，例如添加key前缀 `sky:cache:dish_category:`
。构造好key之后，先从Redis中查询分类数据，根据存入Redis的类型直接强转即可，Redis的序列化器可以自动序列化和反序列化。如果存在，那么直接返回查询到的数据，如果不存在，那么进入原始的SQL查询实现，最后将数据存入Redis，可以根据具体的业务现状设置过期时间。具体示例逻辑见
`DishServiceImpl.java` 中的 `getDishByCategoryUser` 方法。

```java

@Override
public List<DishVO> getDishByCategoryUser(Long categoryId) {
    // 1. 构造redis中的key，规则为 dish_category_{categoryId}
    String key = "dish_category_" + categoryId;

    // 2. 先从redis当中查询分类数据
    List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);

    // 3. 如果存在，则直接返回
    if (dishVOList != null && !dishVOList.isEmpty()) {
        log.info("从Redis缓存中查询到数据: {}", key);
        return dishVOList;
    }

    // 4. 如果不存在，那么查询数据库，并将数据存入redis

    /* ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓以下为原始实现↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */

    log.info("Redis缓存未命中，开始查询数据库: {}", key);
    List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
            .eq(Dish::getCategoryId, categoryId));
    Category category = categoryMapper.selectById(categoryId);
    List<DishVO> dishVOS = BeanUtil.copyToList(dishes, DishVO.class);

    // 获取菜品所属的口味信息和类别名称
    for (DishVO dishVO : dishVOS) {
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, dishVO.getId()));
        dishVO.setCategoryName(category.getName());
        dishVO.setFlavors(dishFlavors);
    }

    /* ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑以上为原始实现↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

    // 5. 将数据存入redis，设置过期时间为1小时
    redisTemplate.opsForValue().set(key, dishVOS, 1, TimeUnit.HOURS);
    return dishVOS;
}
```

#### 增删改的业务逻辑

由于用户端查询到的只有启售中的菜品，因此只需要

1. 在新增菜品时删除对应分类的缓存
2. 在修改菜品时删除全部缓存
3. 在修改菜品启售停售状态时删除全部缓存

删除全部缓存时最好使用 `scan` 命令配合 `match` 参数来模糊匹配需要删除的key，然后使用 `del` 命令删除。具体示例逻辑见
`RedisCacheUtil.java` 中的 `cleanCacheSafe` 方法。

### 通过Spring Cache缓存

#### 启用缓存以及配置类

通过Spring Cache的配置类可以配置 `RedisCacheManager` 作为缓存管理器，可以管理键值的序列化器，以及自定义键的前缀、过期时间等等。本项目将缓存配置整合进Redis配置类
`RedisConfiguration.java` 中。

首先在类上添加 `@EnableCaching` 注解启用缓存功能，然后创建并配置 `RedisCacheManager` 实例，最后将其作为 `CacheManager`
类型的Bean返回。

```java

@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
            .computePrefixWith(cacheName -> "sky:cache:" + cacheName + ":")
            .entryTtl(Duration.ofHours(1));

    return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build();
}
```

#### 在业务类中使用缓存

有了Spring Cache就不需要手动对 `redisTemplate` 进行操作了，只需要在需要缓存的方法上添加 `@Cacheable` 注解即可，Spring
Cache会自动帮你处理缓存的读写逻辑。该注解有以下几个属性；

| 属性             | 说明                                                         |
|:---------------|:-----------------------------------------------------------|
| `cacheNames`   | 指定缓存的名称，可以是一个或多个，作为缓存的命名空间，有一个别名 `value`                   |
| `key`          | 指定缓存的键，可以使用SpEL表达式来动态生成键，例如 `#id` 表示方法参数中的 `id`            |
| `keyGenerator` | 指定自定义的键生成器类的Bean名称，可以用来生成复杂的缓存键                            |
| `condition`    | 指定缓存的条件表达式，只有满足条件时才会缓存结果，例如 `#id > 0` 表示只有 `id` 大于0时才缓存结果  |
| `unless`       | 指定不缓存的条件表达式，满足条件时不会缓存结果，例如 `#result == null` 表示结果为null时不缓存 |
| `cacheManager` | 指定使用的缓存管理器的Bean名称，默认使用配置类中定义的缓存管理器                         |

对需要清除缓存的方法，需要在方法上添加 `@CacheEvict` 注解，属性与 `@Cacheable` 类似，另外还多了一个属性为 `allEntries`
，表示是否清除该缓存名称下的所有缓存项。

值得注意的是，当调用有注解的方法时，会优先检查缓存，如果缓存命中则直接返回缓存结果，不会执行方法体内的代码。如果缓存未命中，则会执行方法体内的代码，并将结果存入缓存。因此，如果方法体内有副作用（如修改数据库），需要谨慎使用
`@Cacheable` 注解。

## 购物车功能

需要注意，本项目中的 `ShoppingCart` 实体类实际上是一个购物车项，表示购物车中的一件商品，而不是整个购物车。每个用户对应一个购物车，购物车中可以包含多件商品，每件商品对应一个购物车项。因此命名为
`ShoppingCartItem` 会更合适一些。

### 添加商品至购物车 `POST /user/shoppingCart/add`

先使用SQL进行存储和查询，查询时使用 `user_id` 和 `dish_id` 或 `setmeal_id`
进行联合查询，确保同一用户的同一商品只会有一条记录。如果存在，则将数量加1并更新记录，否则插入新记录，数量初始化为1。具体实现见
`ShoppingCartServiceImpl.java` 中的 `addShoppingCartItem` 方法。

### 删除购物车商品 `POST /user/shoppingCart/sub`

先查询购物车项，如果存在且数量大于1，则将数量减1并更新记录，如果数量等于1，则删除该记录。具体实现见
`ShoppingCartServiceImpl.java` 中的 `subShoppingCartItem` 方法。

# Day 8

## 地址簿模块

单表增删改查，注意一些细微的逻辑即可，具体实现见 `AddressBookServiceImpl.java` 。

## 用户下单模块

在设计时，订单和订单内明细最好分开存储，分别使用 `Orders` 和 `OrderDetail` 两个实体类，对应两张表 `orders` 和
`order_detail` 。注意项目的原始设计中地址是用地址外键 `address_book_id` 来关联的，而**实际业务中，订单的地址信息应当是冗余存储的
**
，因为用户下单后，可能会修改地址簿中的地址信息，如果订单表中只存储地址外键，那么订单的地址信息就会随着地址簿的修改而变化，这显然是不合理的。因此在订单表中应当存储地址信息，要么存储完整地址，要么存储关键地址信息，而不是地址外键。订单明细中同理，如果有已经删除的菜品，那么订单明细中也应当保留菜品的名称、图片等信息，而不是菜品外键。

对于前后端校验各项数据的意义，在开发前后端分离的项目时，前端和后端都需要进行数据校验，以确保数据的完整性和正确性。前端校验主要是为了提升用户体验，及时反馈用户输入的错误，而后端校验则是为了保证数据的安全性和一致性，防止恶意攻击和数据污染。在实际开发过程中，后端必须要假设前端的校验不存在，因此后端的校验逻辑必须完整且严谨，不能依赖前端的校验结果。

## 用户支付模块

用户支付模块使用微信支付功能，正常实现需要具备商户资质，微信支付商户平台参见：

https://pay.weixin.qq.com/static/product/product_index.shtml

微信支付目前有以下几种形式：

| 支付方式     | 说明                         |
|:---------|:---------------------------|
| 付款码支付    | 用户打开微信，商户出示付款码，用户扫码完成支付    |
| JSAPI支付  | 适用于H5应用场景，页面可以调用微信支付       |
| 小程序支付    | 适用于微信小程序场景                 |
| Native支付 | 适用于扫码支付场景，商户出示二维码，用户扫码完成支付 |
| APP支付    | 适用于移动应用场景，APP内集成微信支付SDK    |
| 刷脸支付     | 适用于线下刷脸支付场景，用户通过人脸识别完成支付   |

项目使用微信小程序支付功能，大体分为用户下单、商户调起支付、用户支付、商户对账、订单退款五个环节，具体流程参见：

https://pay.weixin.qq.com/doc/v3/merchant/4012791911

### 数据安全保障

在一整个微信支付流程中，数据的安全主要体现在**商户系统调用微信下单接口、微信后台返回交易标识**以及**微信后台回调商户系统支付结果通知**这两个环节。为了保障数据的安全，微信支付使用了多种加密和签名技术，包括：
- HTTPS加密传输：所有的接口调用都必须使用HTTPS协议，确保数据在传输过程中不被窃取或篡改。
- 签名验证：商户系统在调用微信支付接口时，需要对请求参数进行签名，微信支付后台会验证签名的正确性，防止请求被篡改。
- 证书加密：商户系统需要使用微信支付提供的API证书来加密敏感数据，如退款请求中的银行卡号等，确保数据在传输过程中不被泄露。在微信商户平台上可以下载到两个文件，分别是 `apiclient_key.pem` 和 `wechatpay_XXXXXX.pem` ，分别是商户API证书和微信支付平台证书。
- 回调验证：微信支付后台在回调商户系统支付结果通知时，会附带签名，商户系统需要验证签名的正确性，确保回调数据的真实性。

### 具体实现

实现类主要封装在了 `WeChatPayUtil.java` 中，本质就是根据微信支付的API规范封装HTTP请求，使用HttpClient包调用微信支付的接口服务。

### 测试场景

由于微信支付需要商户资质，且涉及到真实的资金交易，因此在本项目中直接使用微信支付进行真实交易场景下的测试。对项目提供的代码，需要改动以下两点：

首先在提供的前端小程序代码中，找到 `pages/pay.index.js` 文件，搜索 `wx.requestPayment` 方法，注释掉该方法的调用，然后取消注释后面的 `uni.redirectTo` 方法，这样就可以跳过支付环节，直接模拟支付成功后的回调。

然后在 `OrderServiceImpl.java` 中，修改 `payment` 方法如下：

```java
public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
    // 当前登录用户id
    Long userId = BaseContext.getCurrentId();
    User user = userService.getById(userId);

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("code", "ORDERPAID");
    OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
    vo.setPackageStr(jsonObject.getString("package"));

    paySuccess(ordersPaymentDTO.getOrderNumber());

    return vo;
}
```

这样就可以直接模拟支付成功后的回调，完成订单状态的更新。

# Day 9

## 用户端历史订单模块

查询历史订单的接口是使用分页查询的方式实现的，但是前端的接口只传来了 `page` 和 `pageSize` 两个参数，而没有使用 `OrdersPageQueryDTO` ，这是因为前端没有实现根据根据订单号、电话、时间等条件查询的功能，因此后端暂时也就没有必要实现这个功能，直接使用 `page` 和 `pageSize` 两个参数进行分页查询即可。具体实现见
`OrderServiceImpl.java` 中的 `getHistoryOrders` 方法。

## 商户端订单管理模块
