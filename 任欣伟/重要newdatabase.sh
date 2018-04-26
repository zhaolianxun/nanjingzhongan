#!/bin/bash
#$1 templatecode  $2 templatedb $3 databasecode
cp  /root/javaProjectResource/cm-plat/$1/$2.sql /root/javaProjectResource/cm-plat/$1/$3.sql &&
sed -i 's/CREATE DATABASE IF NOT EXISTS `.*`/CREATE DATABASE IF NOT EXISTS `'$3'`/g;s/USE `.*`/USE `'$3'`/g' /root/javaProjectResource/cm-plat/$1/$3.sql &&
mysql --defaults-extra-file=/etc/my.cnf -e "source /root/javaProjectResource/cm-plat/"$1"/"$3".sql" &&
rm -rf /root/javaProjectResource/cm-plat/$1/$3.sql


