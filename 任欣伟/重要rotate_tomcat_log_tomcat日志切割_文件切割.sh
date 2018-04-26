#!/bin/sh
y=`date "+%Y"`

m=`date "+%m"`

d=`date "+%d"`

H=`date "+%H"`
M=`date "+%M"`
S=`date "+%S"`


maxsize=10
filesize=`ls -l /root/apache-tomcat-8.0.43/instance1/logs/catalina.out | awk '{print $5/1024/1024}'`
#c=`echo "$filesize > $maxsize" | bc`
#echo $maxsize
#echo $filesize
#echo $c
if [ `echo "$filesize > $maxsize" | bc` -eq 1 ]
then
  cp /root/apache-tomcat-8.0.43/instance1/logs/catalina.out /root/apache-tomcat-8.0.43/instance1/logs/catalina.out.$y$m$d$H$M$S
  echo > /root/apache-tomcat-8.0.43/instance1/logs/catalina.out
fi

filesize=`ls -l /root/apache-tomcat-8.0.43/instance2/logs/catalina.out | awk '{print $5/1024/1024}'`
if [ `echo "$filesize > $maxsize" | bc` -eq 1 ]  
then
  cp /root/apache-tomcat-8.0.43/instance2/logs/catalina.out /root/apache-tomcat-8.0.43/instance2/logs/catalina.out.$y$m$d$H$M$S
  echo > /root/apache-tomcat-8.0.43/instance2/logs/catalina.out
fi

#filesize=`ls -l /root/apache-tomcat-8.0.43/instance2_test/logs/catalina.out | awk '{print $5/1024/1024}'`
#if [ `echo "$filesize > $maxsize" | bc` -eq 1 ];  
#then
#  cp /root/apache-tomcat-8.0.43/instance2_test/logs/catalina.out /root/apache-tomcat-8.0.43/instance2_test/logs/catalina.out.$y$m$d$H$M$S
#  echo > /root/apache-tomcat-8.0.43/instance2_test/logs/catalina.out
#fi

exit
