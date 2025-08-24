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

首先引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```



