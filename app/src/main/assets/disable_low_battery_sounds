#!/system/bin/sh
export PATH=/system/bin:$PATH

mount -o rw,remount /system
mount -t rootfs -o remount,rw rootfs

cp -f /system/media/audio/ui/LowBattery.ogg /system/media/audio/ui/LowBattery.ogg.bak

rm -r /system/media/audio/ui/LowBattery.ogg
