#!/system/bin/sh
export PATH=/system/bin:$PATH

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

if [ ! -d /system/csc ]; then
mkdir /system/csc
fi

chmod 0777 feature.xml
chmod 0777 /system/csc
chmod 0777 /system/csc/feature.xml

cp -f feature.xml /system/csc/feature.xml

rm -r feature.xml

chmod 0644 /system/csc/feature.xml
chmod 0755 /system/csc
