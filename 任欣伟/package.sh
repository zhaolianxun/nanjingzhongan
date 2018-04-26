#!/bin/bash
#$1 templatecode  $2 instancecode $3 dbcode $4 port $5 domain
mkdir -p /root/javaProjectResource/cm-plat/$1/instances/$2 && cp -r /root/javaProjectResource/cm-plat/$1/$1 /root/javaProjectResource/cm-plat/$1/instances/$2/$2 && sed -i 's/<display-name>.*<\/display-name>/<display-name>'$2'<\/display-name>/g' /root/javaProjectResource/cm-plat/$1/instances/$2/$2/WEB-INF/web.xml && 
sed -i 's/project.name=.*/project.name='$2'/g' /root/javaProjectResource/cm-plat/$1/instances/$2/$2/WEB-INF/classes/config/mainConfig.properties &&
sed -i 's/jdbc.dbname=.*/jdbc.dbname='$3'/g' /root/javaProjectResource/cm-plat/$1/instances/$2/$2/WEB-INF/classes/config/mainConfig.properties &&
cd /root/javaProjectResource/cm-plat/$1/instances/$2/$2 && jar cf ../$2.war ./* && rm -rf /root/javaProjectResource/cm-plat/$1/instances/$2/$2;
cp -r /root/javaProjectResource/cm-plat/$1/webroot /root/javaProjectResource/$2;
cp /root/javaProjectResource/cm-plat/yichaxun/nginx_server.conf /root/javaProjectResource/cm-plat/yichaxun/instances/$2/nginx_server.conf && 
sed -i 's/$$port/'$4'/g;s/server_name/server_name '$5'/g;s/$$instancecode/'$2'/g' /root/javaProjectResource/cm-plat/yichaxun/instances/$2/nginx_server.conf;






