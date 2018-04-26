#!/bin/bash
sed -i 's/sfdadataanalyst/'$2'/g' /root/mnt/xvdb/database.sql &&
mysql -uroot -p579rencong_#sSDh -e "source /root/mnt/xvdb/database.sql" &&
cp -r /root/apache-tomcat-8.0.43/instance2/webapps/yichaxun /root/mnt/xvdb/$1 && sed -i 's/<display-name>yichaxun<\/display-name>/<display-name>'$1'<\/display-name>/g' /root/mnt/xvdb/$1/WEB-INF/web.xml && sed -i 's/project.name=yichaxun/project.name='$1'/g;s/sfdadataanalyst/'$2'/g' /root/mnt/xvdb/$1/WEB-INF/classes/enterprisesearch/config/mainConfig.properties;
