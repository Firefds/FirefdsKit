#!/system/bin/sh
export PATH=/system/bin:$PATH

OMC_PATH=`getprop persist.sys.omc_path`

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

if [ ! -d $OMC_PATH ]; then
mkdir $OMC_PATH
fi

chmod 0777 $OMC_PATH

if [ ! -f $OMC_PATH/cscfeature.xml.bak ]; then
cp -f $OMC_PATH/cscfeature.xml $OMC_PATH/cscfeature.xml.bak
fi

if [ ! -f $OMC_PATH/cscfeature.xml ]; then
touch $OMC_PATH/cscfeature.xml
fi

if [ ! -f /system/omc/sales_code.dat ]; then
  echo BTU > /system/omc/sales_code.dat
fi

chmod 0777 $OMC_PATH/cscfeature.xml
chmod 0777 $OMC_PATH/cscfeature.xml.bak
chmod 0777 /system/omc/sales_code.dat