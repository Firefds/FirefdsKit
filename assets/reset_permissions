#!/system/bin/sh
export PATH=/system/bin:$PATH

OMC_PATH=`getprop persist.sys.omc_path`

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

chmod 0644 $OMC_PATH/cscfeature.xml
chmod 0644 $OMC_PATH/cscfeature.xml.bak

chmod 0644 /system/csc/feature.xml
chmod 0644 /system/csc/feature.xml.bak
chmod 0644 /system/csc/cscfeature.xml
chmod 0644 /system/csc/cscfeature.xml.bak
chmod 0644 /system/csc/others.xml
chmod 0644 /system/csc/others.xml.bak

chmod 0644 /system/omc/sales_code.dat
chmod 0755 /system/omc
chmod 0644 /system/omc/CSCVersion.txt

chmod 0644 /system/csc/sales_code.dat
chmod 0755 /system/csc
chmod 0644 /system/CSCVersion.txt

mount -o ro,remount /system
mount -t rootfs -o remount,ro rootfs
