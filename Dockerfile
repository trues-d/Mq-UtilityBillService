# 使用基础镜像为 Ubuntu
FROM ubuntu:22.04
RUN sed -i 's@http://archive.ubuntu.com/ubuntu/@http://mirrors.aliyun.com/ubuntu/@g' /etc/apt/sources.list
# 设置工作目录
WORKDIR /utilityBillService

# 更新 apt 软件包索引并安装 curl
RUN apt-get update && apt-get install -y curl

# 下载并安装 Java 8 JDK
RUN apt-get install -y openjdk-8-jdk

# 复制本地的 jar 包到容器中的指定目录
COPY target/*.jar /utilityBillService/app.jar

ENV JAVA_OPTS="" JAVA_HEAP_OPTS=""
ENV LANG C.UTF-8
ENV TZ Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 指定容器启动时执行的命令
CMD ["java", "-jar", "app.jar"]