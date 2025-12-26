#!/bin/bash

# 获取脚本真实路径并切换到项目目录
APP_HOME=$(cd "$(dirname "$0")"/..; pwd)
cd "$APP_HOME" || exit 1

# 记录调试信息
mkdir -p "$APP_HOME/logs"
echo "$(date) 启动脚本开始执行" >> "$APP_HOME/logs/startup.log"

JAR_FILE="$APP_HOME/lib/hx-mqtt-1.0-SNAPSHOT.jar"
CONFIG_DIR="$APP_HOME/config/"
PID_FILE="$APP_HOME/logs/app.pid"
LOG_FILE="$APP_HOME/logs/app.log"


# H2数据库配置
H2_PORT=8088                        # H2 TCP服务器端口(如果使用)
H2_LOCK_FILE="$APP_HOME/data/*.lock.db"  # H2锁文件路径
WAIT_TIMEOUT=30                     # 等待超时时间(秒)

# 创建必要目录
mkdir -p "$APP_HOME/logs"
mkdir -p "$APP_HOME/data"

echo "======================================"
echo "Spring Boot应用安全重启脚本"
echo "应用目录: $APP_HOME"
echo "======================================"

# 1. 停止现有应用
echo "[步骤1] 正在停止Spring Boot应用..."
if [ -f "$PID_FILE" ]; then
    APP_PID=$(cat "$PID_FILE")
    if ps -p $APP_PID > /dev/null; then
        echo "正在停止进程ID: $APP_PID"
        kill $APP_PID

        # 等待进程退出
        while ps -p $APP_PID > /dev/null; do
            sleep 1
            echo "等待应用进程退出..."
        done
        echo "应用已停止"
    else
        echo "PID文件存在但进程不存在，删除旧的PID文件"
        rm -f "$PID_FILE"
    fi
else
    echo "未找到PID文件，尝试查找进程..."
    APP_PID=$(pgrep -f "$(basename $JAR_FILE)")
    if [ ! -z "$APP_PID" ]; then
        echo "发现运行中的进程ID: $APP_PID，正在停止..."
        kill $APP_PID
        sleep 3
    else
        echo "未找到正在运行的应用"
    fi
fi

# 2. 等待H2数据库关闭
echo "[步骤2] 正在检查H2数据库状态..."
WAIT_COUNT=0
DB_CLOSED=false

while [ $WAIT_COUNT -lt $WAIT_TIMEOUT ]; do
    # 检查H2 TCP端口是否释放
    if ! nc -z localhost $H2_PORT &>/dev/null; then
        # 检查H2文件锁是否释放
        if [ ! -f $H2_LOCK_FILE ]; then
            DB_CLOSED=true
            break
        fi
    fi

    sleep 1
    WAIT_COUNT=$((WAIT_COUNT+1))
    echo "已等待 ${WAIT_COUNT}秒 (最多等待 ${WAIT_TIMEOUT}秒)..."
done

if [ "$DB_CLOSED" = false ]; then
    echo "[警告] 等待超时，H2数据库可能未正常关闭"
    echo "正在尝试强制清理H2锁文件..."
    rm -f $H2_LOCK_FILE
    if [ $? -eq 0 ]; then
        echo "H2锁文件已强制删除"
    else
        echo "无法删除H2锁文件，请手动检查"
    fi
else
    echo "H2数据库已正常关闭"
fi

# 3. 启动新应用
echo "[步骤3] 正在启动应用..."
nohup java -jar "$JAR_FILE" \
    --spring.config.location="$CONFIG_DIR/" \
    >> "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo $NEW_PID > "$PID_FILE"
echo "应用启动成功!"
echo "进程ID: $NEW_PID"
echo "日志文件: $LOG_FILE"

echo "======================================"
echo "应用重启流程完成!"
echo "======================================"