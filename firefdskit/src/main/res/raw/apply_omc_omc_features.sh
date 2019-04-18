#!/system/bin/sh
export PATH=/system/bin:$PATH

OMC_PATH=`getprop persist.sys.omc_path`

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

if [ ! -d $OMC_PATH ]; then
mkdir $OMC_PATH
fi

chmod 0777 /data/data/sb.firefds.pie.firefdskit/cache/cscfeature.xml
chmod 0777 $OMC_PATH
chmod 0777 $OMC_PATH/cscfeature.xml

cp -f /data/data/sb.firefds.pie.firefdskit/cache/cscfeature.xml $OMC_PATH/cscfeature.xml
rm -r /data/data/sb.firefds.pie.firefdskit/cache/cscfeature.xml

chmod 0644 $OMC_PATH/cscfeature.xml
chmod 0755 $OMC_PATH
