# ProviderLoader

一个用于从SFTP服务器下载XML文件并进行处理的Java控制台应用程序。

## 功能特性

- **SFTP连接**: 使用JSch库连接到远程SFTP服务器
- **文件下载**: 自动下载匹配模式的XML文件到本地inbox目录
- **可扩展架构**: 为PGP解密、XML解析、数据库集成和审计日志提供了清晰的模块结构
- **配置管理**: 通过application.properties文件管理所有配置
- **审计日志**: 记录所有操作的详细审计信息

## 项目结构

```
src/main/java/com/quikcard/providerloader/
├── App.java                    # 主应用程序入口
├── config/
│   └── AppConfig.java          # 配置管理类
├── sftp/
│   ├── SftpClient.java         # SFTP客户端接口
│   ├── JschSftpClient.java     # JSch实现
│   └── SftpException.java      # SFTP异常类
├── pgp/
│   ├── PgpService.java         # PGP服务（存根实现）
│   └── PgpException.java       # PGP异常类
├── xml/
│   ├── ProviderXmlParser.java  # XML解析器（存根实现）
│   └── XmlParsingException.java # XML异常类
├── db/
│   ├── ProviderRepository.java # 数据库存储（存根实现）
│   └── DatabaseException.java  # 数据库异常类
└── audit/
    └── AuditLogger.java        # 审计日志服务
```

## 快速开始

### 前提条件

- Java 11 或更高版本
- Maven 3.6 或更高版本

### 配置

1. 编辑 `src/main/resources/application.properties` 文件，配置SFTP连接信息：

```properties
# SFTP配置
sftp.host=your-sftp-server.com
sftp.port=22
sftp.username=your_username
sftp.password=your_password
sftp.remote.directory=/path/to/xml/files
sftp.local.inbox.directory=./inbox
```

2. 确保目标SFTP服务器的主机密钥已添加到known_hosts文件中，或者在开发环境中可以使用StrictHostKeyChecking=no。

### 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行应用程序
mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App"

# 或者打包后运行
mvn clean package
java -jar target/provider-loader-1.0.0-SNAPSHOT.jar
```

### 开发模式

启用调试日志：
```bash
# 取消注释logback.xml中的DEBUG配置行
# 或者通过系统属性启用
mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App" -Dlogback.configurationFile=src/main/resources/logback.xml
```

## 依赖项

- **JSch**: SFTP连接和文件传输
- **SLF4J + Logback**: 日志记录
- **BouncyCastle**: PGP加密/解密（预留）
- **Oracle JDBC**: 数据库连接（预留）

## 未来开发计划

### PGP支持
- 实现使用BouncyCastle的PGP文件解密
- 添加数字签名验证功能

### XML处理
- 实现提供商XML文件的解析和验证
- 添加XSD模式验证

### 数据库集成
- 实现Oracle数据库连接和数据插入
- 添加事务管理和错误恢复

### 审计增强
- 将审计日志写入文件和数据库
- 添加性能监控和报告

## 配置选项

详细的配置选项请参考 `application.properties` 文件中的注释。主要配置包括：

- SFTP连接参数
- 文件路径和模式
- PGP密钥配置
- 数据库连接信息
- 应用程序设置（重试次数、超时等）

## 日志

应用程序使用分层日志记录：
- **控制台输出**: 实时状态和错误信息
- **应用程序日志**: 详细的操作日志 (`logs/provider-loader.log`)
- **审计日志**: 业务操作审计记录 (`logs/audit.log`)

## 许可证

本项目使用MIT许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。A Java-based framework for securely connecting to SFTP servers, downloading encrypted files, decrypting them with PGP, parsing XML data, and preparing for database integration.
