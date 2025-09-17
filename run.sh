#!/bin/bash

# ProviderLoader运行脚本
# 此脚本演示如何编译和运行ProviderLoader应用程序

echo "ProviderLoader - SFTP XML文件下载器"
echo "=================================="

# 检查Java是否可用
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java。请安装Java 11或更高版本。"
    exit 1
fi

echo "Java版本:"
java -version
echo ""

# 创建必要的目录
echo "创建必要的目录..."
mkdir -p target/classes
mkdir -p logs
mkdir -p inbox

# 检查Maven是否可用
if command -v mvn &> /dev/null; then
    echo "使用Maven编译项目..."
    mvn clean compile
    
    if [ $? -eq 0 ]; then
        echo "编译成功！"
        echo ""
        echo "运行应用程序..."
        mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App"
    else
        echo "Maven编译失败。请检查依赖项和代码。"
        exit 1
    fi
else
    echo "警告: 未找到Maven。"
    echo "请安装Maven以便自动管理依赖项和编译项目。"
    echo ""
    echo "安装Maven的步骤："
    echo "1. 访问 https://maven.apache.org/download.cgi"
    echo "2. 下载并安装Maven"
    echo "3. 将Maven的bin目录添加到PATH环境变量"
    echo ""
    echo "或者使用包管理器安装："
    echo "  macOS: brew install maven"
    echo "  Ubuntu: sudo apt install maven"
    echo "  CentOS: sudo yum install maven"
    echo ""
    echo "安装Maven后，可以使用以下命令运行项目："
    echo "  mvn clean compile"
    echo "  mvn exec:java -Dexec.mainClass=\"com.quikcard.providerloader.App\""
fi

echo ""
echo "注意: 在运行应用程序之前，请确保："
echo "1. 编辑 src/main/resources/application.properties 文件"
echo "2. 配置正确的SFTP服务器连接信息"
echo "3. 确保网络连接正常"
