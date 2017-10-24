#!/system/bin/sh
export PATH=/system/bin:$PATH

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

if [ ! -d /system/csc ]; then
mkdir /system/csc
fi

chmod 0777 cscfeature.xml
chmod 0777 /system/csc
chmod 0777 /system/csc/cscfeature.xml

cp -f cscfeature.xml /system/csc/cscfeature.xml

rm -r cscfeature.xml

chmod 0644 /system/csc/cscfeature.xml
chmod 0755 /system/csc
