#!/bin/bash
#$1 instancecode $2dbcode $3 templatecode
sed -i 's/jdbc.dbname=.*/jdbc.dbname='$2'/g' /root/javaProjectResource/cm-plat/$3/instances/$1/WEB-INF/classes/config/mainConfig.properties;