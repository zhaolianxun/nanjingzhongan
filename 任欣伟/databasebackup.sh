year=$(date +%Y)
month=$(date +%m)
day=$(date +%d)
hour=$(date +%H)
minute=$(date +%M)
second=$(date +%S)

#sfdadataanalyst
sfdadataanalyst_path="/root/mnt/xvdb/data/database-backup/sfdadataanalyst/$year$month$day/$hour"
mkdir -p ${sfdadataanalyst_path}

minute_path=$minute
mkdir -p ${sfdadataanalyst_path}/${minute_path}

mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_balance_record | gzip > ${sfdadataanalyst_path}/${minute_path}/t_balance_record.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_bill | gzip > ${sfdadataanalyst_path}/${minute_path}/t_bill.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_carecard | gzip > ${sfdadataanalyst_path}/${minute_path}/t_carecard.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_carecard_hospital | gzip > ${sfdadataanalyst_path}/${minute_path}/t_carecard_hospital.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_corporation | gzip > ${sfdadataanalyst_path}/${minute_path}/t_corporation.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_corporation_user | gzip > ${sfdadataanalyst_path}/${minute_path}/t_corporation_user.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_favorite | gzip > ${sfdadataanalyst_path}/${minute_path}/t_favorite.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_good | gzip > ${sfdadataanalyst_path}/${minute_path}/t_good.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_healthyculture | gzip > ${sfdadataanalyst_path}/${minute_path}/t_healthyculture.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_healthyculture_mallgood | gzip > ${sfdadataanalyst_path}/${minute_path}/t_healthyculture_mallgood.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_home_ad | gzip > ${sfdadataanalyst_path}/${minute_path}/t_home_ad.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospital | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospital.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospitalbill | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospitalbill.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospital_bill | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospital_bill.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospital_coupon_box | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospital_coupon_box.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospital_office | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospital_office.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hospital_wxmassmsg | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hospital_wxmassmsg.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hotsearch | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hotsearch.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_hot_key | gzip > ${sfdadataanalyst_path}/${minute_path}/t_hot_key.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_land_promoter | gzip > ${sfdadataanalyst_path}/${minute_path}/t_land_promoter.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_maintain_module | gzip > ${sfdadataanalyst_path}/${minute_path}/t_maintain_module.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_maintain_user | gzip > ${sfdadataanalyst_path}/${minute_path}/t_maintain_user.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_maintain_user_action | gzip > ${sfdadataanalyst_path}/${minute_path}/t_maintain_user_action.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_area | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_area.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_cart | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_cart.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_good | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_good.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_order | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_order.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_order_detail | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_order_detail.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_mall_order_pay | gzip > ${sfdadataanalyst_path}/${minute_path}/t_mall_order_pay.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_medical_scheme | gzip > ${sfdadataanalyst_path}/${minute_path}/t_medical_scheme.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_medical_scheme_coin_rec | gzip > ${sfdadataanalyst_path}/${minute_path}/t_medical_scheme_coin_rec.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_medical_scheme_support_order | gzip > ${sfdadataanalyst_path}/${minute_path}/t_medical_scheme_support_order.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_nearbyrecommend_mallarea | gzip > ${sfdadataanalyst_path}/${minute_path}/t_nearbyrecommend_mallarea.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_order | gzip > ${sfdadataanalyst_path}/${minute_path}/t_order.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_order_detail | gzip > ${sfdadataanalyst_path}/${minute_path}/t_order_detail.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_order_pay | gzip > ${sfdadataanalyst_path}/${minute_path}/t_order_pay.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_order_refund | gzip > ${sfdadataanalyst_path}/${minute_path}/t_order_refund.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_page_yccgx_home | gzip > ${sfdadataanalyst_path}/${minute_path}/t_page_yccgx_home.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_promotion | gzip > ${sfdadataanalyst_path}/${minute_path}/t_promotion.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_search_record | gzip > ${sfdadataanalyst_path}/${minute_path}/t_search_record.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_shop | gzip > ${sfdadataanalyst_path}/${minute_path}/t_shop.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_sys_param | gzip > ${sfdadataanalyst_path}/${minute_path}/t_sys_param.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_user | gzip > ${sfdadataanalyst_path}/${minute_path}/t_user.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_user_address | gzip > ${sfdadataanalyst_path}/${minute_path}/t_user_address.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_user_carecard | gzip > ${sfdadataanalyst_path}/${minute_path}/t_user_carecard.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_user_carecard_hospital | gzip > ${sfdadataanalyst_path}/${minute_path}/t_user_carecard_hospital.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_user_hospital_coupon | gzip > ${sfdadataanalyst_path}/${minute_path}/t_user_hospital_coupon.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_withdraw | gzip > ${sfdadataanalyst_path}/${minute_path}/t_withdraw.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -n -t -d -R sfdadataanalyst | gzip > ${sfdadataanalyst_path}/${minute_path}/others.sql.gz
if [ $hour$minute$second == "000000" ]
then
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_qcc_cor | gzip > ${sfdadataanalyst_path}/${minute_path}/t_qcc_cor.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_data | gzip > ${sfdadataanalyst_path}/${minute_path}/t_data.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf sfdadataanalyst t_original_data | gzip > ${sfdadataanalyst_path}/${minute_path}/t_original_data.sql.gz
fi

rm -rf /root/mnt/xvdb/data/database-backup/sfdadataanalyst/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/sfdadataanalyst/* -mtime +16 -exec rm -rf {} \;

#cm-plat
if [ $hour$minute$second == "000000" ]
then
mkdir -p /root/mnt/xvdb/data/database-backup/cm-plat/$year$month$day
mysqldump --defaults-extra-file=/etc/my.cnf -R  cm-plat | gzip > /root/mnt/xvdb/data/database-backup/cm-plat/$year$month$day/$hour$minute$second.sql.gz
rm -rf /root/mnt/xvdb/data/database-backup/cm-plat/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/cm-plat/* -mtime +16 -exec rm -rf {} \;
fi

#other databases
mkdir -p /root/mnt/xvdb/data/database-backup/donate/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/medical/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/mlt-defence/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/jobdb/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/master/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/frinvt/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/salvation/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/wordshow/$year$month$day
mkdir -p /root/mnt/xvdb/data/database-backup/wxma/$year$month$day


mysqldump --defaults-extra-file=/etc/my.cnf -R  jobdb | gzip > /root/mnt/xvdb/data/database-backup/jobdb/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  donate | gzip > /root/mnt/xvdb/data/database-backup/donate/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  medical | gzip > /root/mnt/xvdb/data/database-backup/medical/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  mlt-defence | gzip > /root/mnt/xvdb/data/database-backup/mlt-defence/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  master | gzip > /root/mnt/xvdb/data/database-backup/master/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  frinvt | gzip > /root/mnt/xvdb/data/database-backup/frinvt/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  salvation | gzip > /root/mnt/xvdb/data/database-backup/salvation/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  wordshow | gzip > /root/mnt/xvdb/data/database-backup/wordshow/$year$month$day/$hour$minute$second.sql.gz
mysqldump --defaults-extra-file=/etc/my.cnf -R  wxma | gzip > /root/mnt/xvdb/data/database-backup/wxma/$year$month$day/$hour$minute$second.sql.gz

rm -rf /root/mnt/xvdb/data/database-backup/mlt-defence/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/mlt-defence/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/medical/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/medical/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/donate/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/donate/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/jobdb/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/jobdb/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/master/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/master/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/frinvt/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/frinvt/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/salvation/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/salvation/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/wordshow/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/wordshow/* -mtime +16 -exec rm -rf {} \;
rm -rf /root/mnt/xvdb/data/database-backup/wxma/$(date -d "16 day ago" +"%Y%m%d")
find /root/mnt/xvdb/data/database-backup/wxma/* -mtime +16 -exec rm -rf {} \;
