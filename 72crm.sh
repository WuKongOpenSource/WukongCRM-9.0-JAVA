#!/bin/bash

# 启动入口类，该脚本文件用于别的项目时要改这里
MAIN_CLASS=com.kakarote.crm9.Application

if [[ "$MAIN_CLASS" == "com.yourpackage.YourMainClass" ]]; then
    echo "请先修改 MAIN_CLASS 的值为你自己项目启动Class，然后再执行此脚本。"
	exit 0
fi

COMMAND="$1"

if [[ "$COMMAND" != "start" ]] && [[ "$COMMAND" != "stop" ]] && [[ "$COMMAND" != "restart" ]]; then
	echo "Usage: $0 start | stop | restart"
	exit 0
fi


# Java 命令行参数，根据需要开启下面的配置，改成自己需要的，注意等号前后不能有空格
# JAVA_OPTS="-Xms256m -Xmx1024m -Dundertow.port=80 -Dundertow.host=0.0.0.0"
# JAVA_OPTS="-Dundertow.port=80 -Dundertow.host=0.0.0.0"

# 生成 class path 值

APP_BASE_PATH=$(cd `dirname $0`; pwd)

CP=${APP_BASE_PATH}/config:${APP_BASE_PATH}/lib/*

function start()
{
    nohup java -Xverify:none ${JAVA_OPTS} -cp ${CP} ${MAIN_CLASS} > ./output.log &
}

function stop()
{
    kill `pgrep -f ${APP_BASE_PATH}` 2>/dev/null
}

if [[ "$COMMAND" == "start" ]]; then
	start
elif [[ "$COMMAND" == "stop" ]]; then
    stop
else
    stop
    start
fi
