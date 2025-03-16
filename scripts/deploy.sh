#!/bin/bash

#chmod +x /home/ec2-user/pillyProject/scripts/deploy.sh

REPOSITORY=/home/ec2-user/pillyProject
cd $REPOSITORY

APP_NAME=pilly-0.0.1-SNAPSHOT.jar

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(ps -ef | grep java | grep "$APP_NAME" | awk '{print $2}')

if [ -z $CURRENT_PID ]
then
  echo "> 종료할것 없음."
else
  echo "> 실행 중인 애플리케이션 종료 중... (PID: $CURRENT_PID)"
  kill -15 "$CURRENT_PID"
  sleep 5
  if ps -p "$CURRENT_PID" > /dev/null; then
    echo "> 정상 종료되지 않아 강제 종료 실행 (PID: $CURRENT_PID)"
    kill -9 "$CURRENT_PID"
  fi
fi

echo "> 새로운 $JAR_PATH 배포"
nohup java -jar $JAR_PATH > /home/ec2-user/app.log 2>&1 &

echo "> 배포 완료!"