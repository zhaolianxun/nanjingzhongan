mkdir -p /root/mnt/xvdb/data/database-backup/donate/$(date +%Y%m%d)
mkdir -p /root/mnt/xvdb/data/database-backup/medical/$(date +%Y%m%d)
mkdir -p /root/mnt/xvdb/data/database-backup/mlt-defence/$(date +%Y%m%d)
mkdir -p /root/mnt/xvdb/data/database-backup/mootop_platform/$(date +%Y%m%d)
mkdir -p /root/mnt/xvdb/data/database-backup/jobdb/$(date +%Y%m%d)
mkdir -p /root/mnt/xvdb/data/database-backup/sfdadataanalyst/$(date +%Y%m%d)

mysqldump -uroot -p579rencong_#sSDh mootop_platform | gzip > /root/mnt/xvdb/data/database-backup/mootop_platform/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz
mysqldump -uroot -p579rencong_#sSDh jobdb | gzip > /root/mnt/xvdb/data/database-backup/jobdb/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz
mysqldump -uroot -p579rencong_#sSDh donate | gzip > /root/mnt/xvdb/data/database-backup/donate/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz
mysqldump -uroot -p579rencong_#sSDh medical | gzip > /root/mnt/xvdb/data/database-backup/medical/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz
mysqldump -uroot -p579rencong_#sSDh mlt-defence | gzip > /root/mnt/xvdb/data/database-backup/mlt-defence/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz
mysqldump -uroot -p579rencong_#sSDh sfdadataanalyst | gzip > /root/mnt/xvdb/data/database-backup/sfdadataanalyst/$(date +%Y%m%d)/$(date +%Y%m%d_%H%M%S).sql.gz

rm -rf /root/mnt/xvdb/data/database-backup/mlt-defence/$(date -d "61 day ago" +"%Y%m%d")
rm -rf /root/mnt/xvdb/data/database-backup/medical/$(date -d "61 day ago" +"%Y%m%d")
rm -rf /root/mnt/xvdb/data/database-backup/donate/$(date -d "61 day ago" +"%Y%m%d")
rm -rf /root/mnt/xvdb/data/database-backup/mootop_platform/$(date -d "61 day ago" +"%Y%m%d")
rm -rf /root/mnt/xvdb/data/database-backup/jobdb/$(date -d "61 day ago" +"%Y%m%d")
rm -rf /root/mnt/xvdb/data/database-backup/sfdadataanalyst/$(date -d "61 day ago" +"%Y%m%d")

find /root/mnt/xvdb/data/database-backup/mlt-defence/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf
find /root/mnt/xvdb/data/database-backup/medical/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf
find /root/mnt/xvdb/data/database-backup/donate/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf
find /root/mnt/xvdb/data/database-backup/mootop_platform/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf
find /root/mnt/xvdb/data/database-backup/jobdb/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf
find /root/mnt/xvdb/data/database-backup/sfdadataanalyst/$(date -d "2 day ago" +"%Y%m%d")/  -type f ! -name $(date -d "2 day ago" +"%Y%m%d")_2357*.sql.gz | xargs rm -rf

