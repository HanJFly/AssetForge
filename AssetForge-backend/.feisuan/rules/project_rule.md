
# AssetForge-backend 开发规范指南
为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目基础信息
| 项 | 说明 |
|----|------|
| 用户工作目录 | `D:\Mystudy\AssetForge\AssetForge-backend` |
| 项目作者 | 32186 |
| 构建工具 | Maven |
| 操作系统 | Windows 11 |
| 项目版本 | 0.0.1-SNAPSHOT |

## 二、技术栈要求
- **主框架**：Spring Boot 4.1.0
- **语言版本**：JDK 21.0.2
- **持久层框架**：MyBatis-Plus 3.5.17 + Spring Data JPA（二选一或按需使用）
- **核心依赖**：
  - `mybatis-plus-spring-boot4-starter` / `mybatis-plus-generator`（MyBatis-Plus 核心及代码生成器）
  - `spring-boot-starter-data-jpa`（JPA 持久层支持）
  - `spring-boot-starter-webmvc`（Web 层支持）
  - `spring-boot-starter-validation`（参数校验支持）
  - `lombok`（实体类简化工具）
  - `hutool-all 5.8.46`（通用工具类库）
  - `jjwt-api/impl/jackson 0.12.6`（JWT 认证支持）
  - `pagehelper-spring-boot-starter 1.4.7`（MyBatis 分页插件）
  - `jbcrypt 0.4`（密码加密工具）
  - `mysql-connector-j`（MySQL 驱动）
  - `freemarker 2.3.32`（模板引擎，用于代码生成）
  - `spring-boot-devtools`（开发热部署支持）

## 三、目录结构规范
项目严格遵循以下目录结构，新增模块需对齐现有规范：
```
AssetForge-backend
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── hjf
        │   │           ├── common
        │   │           │   ├── exception  # 通用自定义异常、全局异常处理器
        │   │           │   └── result     # 统一接口返回结果封装
        │   │           ├── config         # 配置类（JWT、MyBatis-Plus、跨域、Swagger等）
        │   │           ├── controller     # 控制器层，处理HTTP请求
        │   │           ├── entity         # 数据库实体类（JPA Entity / MyBatis-Plus实体）
        │   │           ├── filter         # 过滤器（JWT认证、跨域、日志等）
        │   │           ├── mapper         # MyBatis-Plus 数据访问层
        │   │           ├── param          # 请求参数、查询条件、分页参数封装类
        │   │           ├── service        # 业务接口层
        │   │           │   └── impl       # 业务接口实现类
        │   │           ├── util           # 工具类（JWT工具、密码工具、日期工具等）
        │   │           └── vo             # 视图对象，返回给前端的封装数据
        │   └── resources
        │       ├── image                  # 静态图片资源
        │       ├── mapper                  # MyBatis XML映射文件
        │       ├── static                  # 前端静态资源
        │       ├── templates               # 模板文件
        │       └── application.yml         # 核心配置文件
        └── test
            └── java
                └── com
                    └── hjf               # 单元测试类
```

## 四、分层架构规范
| 层级 | 职责说明 | 开发约束与注意事项 |
|------|----------|--------------------|
| **Controller** | 处理HTTP请求与响应，定义API接口 | 1. 不得直接访问数据库，必须通过Service层调用<br>2. 入参必须加`@Valid`做校验，使用`jakarta.validation.constraints`下的校验注解<br>3. 返回值必须封装为`common.result.Result`统一格式，禁止直接返回实体/集合 |
| **Service** | 实现业务逻辑、事务管理、数据校验 | 1. 必须通过Repository/Mapper层访问数据库<br>2. 返回DTO/VO而非Entity（除非必要）<br>3. `@Transactional`注解仅用于Service层方法，禁止在循环中频繁提交事务<br>4. 接口与实现分离，实现类放在对应包下的`impl`子包中 |
| **Mapper/Repository** | 数据库访问与持久化操作 | 1. 使用MyBatis-Plus时：Mapper接口继承`BaseMapper`，XML放在`resources/mapper`目录，用`@TableName`等注解关联实体，复杂查询用`@TableField`或XML标签避免N+1问题<br>2. 使用JPA时：继承`JpaRepository`，用`@EntityGraph`避免N+1查询<br>3. 分页查询统一用PageHelper插件，禁止手动拼接limit |
| **Entity** | 映射数据库表结构 | 1. 用Lombok注解简化代码，包名统一为`entity`<br>2. 不得直接返回给前端，需转换为DTO/VO<br>3. 字段命名遵循数据库驼峰/下划线规范，开启MyBatis-Plus下划线转驼峰配置 |
| **Param** | 封装请求参数、查询条件、分页参数 | 不得包含业务逻辑，仅做参数传递 |
| **Vo** | 视图展示对象 | 仅包含前端需要的字段，禁止包含敏感信息（如密码、盐值） |
| **Common** | 通用组件 | 自定义异常、统一返回结果、常量类等公共组件统一放在该包下 |
| **Config** | 配置类 | 框架配置、业务配置统一放在该包下，禁止散落在Controller/Service中 |
| **Filter** | 过滤器 | JWT认证、跨域、请求日志等拦截逻辑统一放在该包下 |
| **Util** | 工具类 | 通用工具方法统一封装，禁止在业务层重复造轮子，优先使用Hutool工具类 |

## 五、安全与性能规范
### 输入校验
1. 所有接口入参必须加`@Valid`注解，使用`jakarta.validation.constraints`下的校验注解（如`@NotBlank`、`@Size`等）
2. 禁止手动拼接SQL字符串，防止SQL注入攻击，MyBatis-Plus/JPA的参数绑定必须用`#{}`/`?1`占位符

### 数据安全
1. 用户密码必须使用JBCrypt加密存储，禁止明文存储密码
2. JWT token签名密钥（`jwt.signKey`）禁止硬编码在代码中，生产环境需通过配置中心/环境变量注入
3. 禁止在接口返回值、日志中打印敏感信息（如密码、身份证号、token等）

### 性能优化
1. 禁止在循环中执行数据库查询操作，避免N+1查询问题
2. 大列表查询必须做分页，禁止一次性查询全量数据
3. 事务方法要尽量精简，避免在事务方法中执行RPC、IO等耗时操作

## 六、代码风格规范
### 命名规范
| 类型 | 命名方式 | 示例 |
|------|----------|------|
| 类名 | UpperCamelCase | `UserServiceImpl`、`JwtUtils` |
| 方法/变量 | lowerCamelCase | `saveUser()`、`pageNum` |
| 常量 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS`、`JWT_EXPIRE_TIME` |
| 包名 | 全小写，名词单数 | `entity`、`service`、`util` |

### 注释规范
1. 所有类、方法、字段必须添加**Javadoc注释**，注释使用中文
2. 复杂业务逻辑必须添加行内注释说明
3. 禁止出现无意义的注释（如`// get方法`、`// 修改用户`等）

### 实体类简化规范
统一使用Lombok注解替代手动编写getter/setter/构造方法，必须同时添加以下三个注解：
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
```

### 类型后缀规范（阿里巴巴风格）
| 后缀 | 用途说明 | 示例 |
|------|----------|------|
| DTO | 数据传输对象，层与层之间传递的参数 | `UserDTO` |
| VO | 视图对象，返回给前端的展示数据 | `UserVO` |
| Query | 查询参数封装对象 | `UserQuery` |
| BO | 业务逻辑封装对象 | `UserBO` |

## 七、通用开发规则
1. **接口优先原则**：所有业务逻辑通过接口定义，具体实现放在`impl`子包中，便于扩展和单元测试
2. **日志规范**：使用`@Slf4j`注解输出日志，禁止使用`System.out.println`，日志级别遵循：错误用`error`、 warn、业务关键节点用`info`、调试用`debug`
3. **依赖规范**：依赖版本统一在pom.xml的`properties`中管理，禁止随意升级依赖版本，新增依赖需评估必要性
4. **代码生成规范**：使用MyBatis-Plus代码生成器生成的代码需符合本规范，禁止随意修改生成代码的结构和包路径
5. **配置规范**：`application.yml`中的配置需按模块分类注释，敏感配置（如数据库密码、JWT密钥）生产环境禁止明文配置
6. **测试规范**：单元测试类放在`test`目录对应包下，测试覆盖率需达到核心业务80%以上

## 八、编码原则总结
| 原则 | 说明 |
|------|------|
| **SOLID** | 高内聚、低耦合，增强可维护性与可扩展性 |
| **DRY** | 避免重复代码，提高复用性，通用逻辑优先封装到工具类/公共组件 |
| **KISS** | 保持代码简洁易懂，避免过度设计 |
| **YAGNI** | 不实现当前不需要的功能，避免提前优化 |
| **OWASP** | 防范常见安全漏洞，如SQL注入、XSS、越权访问等 |
