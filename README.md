# video-web

一个仿 B 站风格的视频分享网站后端服务，基于 Spring Boot 3 构建，提供视频/剧集管理、UP 主投稿、评论互动、收藏夹、关注、搜索、大会员（VIP）支付、消息通知等完整的视频社区能力，并配套独立的管理端（RBAC 权限、审核、统计、日志）。

## 功能特性

### 前台（用户端）

| 模块 | 说明 |
| --- | --- |
| 用户认证 | 用户名密码 / 邮箱验证码双模式登录，JWT 无状态认证 |
| 视频浏览 | 视频列表（分类筛选、UP 主投稿分页）、视频详情、剧集播放（VIP 拦截、avid 剧集跳转） |
| 剧集互动 | 点赞、投币、收藏，实时同步视频聚合统计 |
| 评论系统 | 树形评论/回复、评论点赞、举报 |
| 关注体系 | 关注/取关、粉丝列表、回关状态 |
| 收藏夹 | 多文件夹管理（默认文件夹、私密属性、封面），收藏/取消收藏 |
| 搜索 | 聚合搜索（视频/用户/收藏夹统一入口），ES 双层开关 + 数据库降级 |
| 消息中心 | 站内信（分页、未读数、已读），WebSocket 实时推送 |
| 大会员 | 套餐购买（支付宝沙箱支付）、开通/续期、到期时间 |
| 订单 | 通用订单体系（预留多业务扩展），超时未支付自动关闭 |
| 投稿与举报 | 视频投稿、稿件举报、违规下架通知 |
| 观看历史 | 去重记录、最新剧集展示 |
| UP 主主页 | 公开信息、粉丝数同步、投稿列表、私信 |

### 管理端

- 视频管理：投稿审核（剧集级审核）、视频 CRUD
- 分类管理：分类增删改查、启用/禁用、名称唯一性校验
- 用户管理、角色/菜单管理（RBAC 权限体系）
- 字典管理：字典类型 + 字典数据双表结构
- 商品管理、订单管理、大会员（VIP）管理
- 举报管理：举报复核、下架通知
- 评论管理、消息管理
- 数据中心：多维度统计（按月分组、日环比）
- 日志管理：登录日志、操作日志（AOP 自动采集）
- 全局搜索、网站配置（WebConfig，Redis 缓存）

### 系统能力

- Spring Security + JWT 无状态认证，前后台接口隔离鉴权
- AOP 切面：登录日志、操作日志、消息通知自动组装
- Spring Event + 事务监听：异步通知解耦、失败静默降级
- Redisson 延迟队列 / 定时任务：订单超时自动关闭
- 文件分片上传（5MB 分片，最大 500MB），秒传与合并
- 邮件验证码、图形验证码（EasyCaptcha）
- Actuator 健康检查（ES 健康指示器跟随总开关）
- Knife4j 在线 API 文档

## 技术栈

| 分类 | 技术 | 版本 |
| --- | --- | --- |
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.3.13 |
| 构建 | Gradle（Wrapper） | - |
| 安全认证 | Spring Security + JWT（JJWT） | 0.12.6 |
| ORM | MyBatis-Plus | 3.5.10.1 |
| 数据库 | PostgreSQL | - |
| 缓存 | Redis（Lettuce）+ Redisson | 3.24.3 |
| 搜索引擎 | Elasticsearch（Java API Client） | 8.x |
| 配置/注册中心 | Nacos | 3.x |
| 微服务框架 | Spring Cloud Alibaba | 2023.0.3.4 |
| 实时推送 | WebSocket | - |
| API 文档 | Knife4j（OpenAPI 3） | 4.5.0 |
| 支付 | 支付宝开放平台 SDK（沙箱） | 4.38.157 |
| 验证码 | EasyCaptcha | 3.0.0 |
| 邮件 | Spring Boot Mail | - |
| 工具库 | Hutool | 5.8.40 |
| 部署 | Docker（eclipse-temurin:21-jre） | - |

## 项目结构

```
video-web
├── src/main/java/com/cjym/videoweb/
│   ├── annotation/          # 自定义注解（@LogLogin、@LogOperation、@NotifyMessage 等）
│   ├── aspect/              # AOP 切面（登录日志、操作日志、消息通知）
│   ├── config/              # Spring 配置（Security、MyBatis-Plus、Redis、ES、支付宝、WebSocket）
│   ├── constant/            # 业务常量
│   ├── controller/
│   │   ├── admin/           # 管理端接口
│   │   ├── client/          # 前台接口
│   │   └── FileController   # 文件上传/预览
│   ├── enums/               # 枚举（ResultEnum）
│   ├── exception/           # 全局异常（GlobeException）
│   ├── handler/             # 异常处理器、拦截器、WebSocket、字段自动填充
│   ├── mapper/              # MyBatis Mapper
│   ├── model/               # DTO / Entity / ES / Event / VO
│   ├── provider/            # 认证 Provider、线程池 Provider
│   ├── result/              # 统一响应结构 R<T>
│   ├── service/             # 业务服务（含 impl、pay 支付适配）
│   ├── task/                # 定时任务
│   └── utils/               # 工具类
└── src/main/resources/
    ├── db/                  # 数据库脚本（init_* 初始化 / migrate_* 增量迁移）
    ├── mapper/              # MyBatis XML
    ├── application.yml      # 主配置（端口、激活环境）
    ├── application-dev.yml  # 开发环境（Nacos 连接信息）
    └── application-prod.yml # 生产环境（Nacos 连接信息）
```

## 快速开始

### 1. 环境要求

- **JDK 21**（Oracle OpenJDK 21 或兼容版本）
- **PostgreSQL**（业务数据库）
- **Redis**（缓存、延迟队列）
- **Nacos 3.x**（配置中心 + 注册中心，必需）
- **Elasticsearch**（可选，搜索功能有 DB 降级方案）
- 邮件服务、支付宝沙箱（可选，影响验证码登录与支付功能）

### 2. 初始化数据库

创建业务数据库后，依次执行 `src/main/resources/db/` 下的 `init_*.sql` 脚本完成建表与基础数据初始化；增量变更脚本以 `migrate_*.sql` 命名，按时间顺序执行。

### 3. 配置

1. 修改 `application-dev.yml` 中的 Nacos 连接地址（服务端端口 8848）；
2. 在 Nacos 对应命名空间下发布配置，dataId 为 `video-web.yaml`、Group 为 `DEFAULT_GROUP`，内容参考项目根目录的 `nacos-config-dump.yaml`（包含数据源、Redis、ES、文件上传、MyBatis-Plus、邮件、支付宝等配置）。

### 4. 构建与运行

```bash
# 构建（跳过测试）
./gradlew bootJar -x test

# 本地运行（默认 dev 环境，端口 8899，context-path /api）
./gradlew bootRun

# Windows 下使用 gradlew.bat
gradlew.bat bootRun
```

启动后：

- 服务地址：`http://localhost:8899/api`
- API 文档（Knife4j）：`http://localhost:8899/api/doc.html`
- WebSocket 推送：`ws://localhost:8899/api/ws/message?token={access_token}`

## Docker 部署

```bash
# 1. 本地构建 jar
./gradlew bootJar -x test

# 2. 将主 jar 放入 target 目录（勿放入 -plain.jar）
mkdir -p target && cp build/libs/video-web-0.0.1-SNAPSHOT.jar target/

# 3. 构建镜像并运行
docker build -t video-web:latest .
docker run -d --name video-web \
  -p 8899:8899 \
  -v /data/uploads:/data/uploads \
  video-web:latest
```

说明：镜像基于 `eclipse-temurin:21-jre-jammy`，默认 JVM 参数 `-Xms256m -Xmx512m`，可通过 `docker run -e JAVA_OPTS=...` 覆盖；容器内文件上传目录为 `/data/uploads`，通过 `FILE_UPLOAD_PATH` 环境变量或挂载数据卷持久化。

## 接口规范

- 统一响应结构：`R<T>`，字段为 `code`（int）/ `message`（string）/ `data`（T）
- 认证方式：需登录接口使用请求头 `Authorization: Bearer {token}`
- 请求体格式：`application/json`
- 标识符约定：对外传输使用业务 UID（如视频 `episodeUid`/`avid`、收藏夹 `folderUid`、用户 `userUid`），禁止传递数据库主键 ID
- 日期时间格式：`yyyy-MM-dd HH:mm:ss`

## License

内部学习项目，暂未开源授权。
