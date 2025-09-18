# ProviderLoader 部署指南

## 环境要求
- Java 11 或更高版本
- 网络访问权限（用于SFTP连接）

## 部署选项

### 选项1：便携式部署（推荐）
1. 复制整个项目文件夹到目标机器
2. 确保Java已安装：`java -version`
3. 设置环境变量：
   ```powershell
   [Environment]::SetEnvironmentVariable("MAVEN_HOME", "完整路径\apache-maven-3.9.11", "User")
   $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
   [Environment]::SetEnvironmentVariable("PATH", "$currentPath;完整路径\apache-maven-3.9.11\bin", "User")
   ```
4. 重启终端，运行：`mvn -v`

### 选项2：使用系统Maven
1. 复制项目文件夹（不包含apache-maven-3.9.11目录）
2. 确保系统已安装Maven：`mvn -v`
3. 直接运行：`mvn clean compile`

## 配置
1. 修改 `src/main/resources/application.properties` 中的SFTP配置
2. 确保 `logs/` 和 `inbox/` 目录存在

## 运行
```bash
mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App"
```

## 注意事项
- 确保目标机器能访问SFTP服务器
- 检查防火墙和网络策略
- 验证SFTP凭据的有效性
