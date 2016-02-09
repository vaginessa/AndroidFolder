#!/bin/bash
TARGET_DIR=/media/welckron/linux
cd /home/welckron/buildroot/

cp buildroot-2015.08.1/output/images/zImage /media/welckron/boot/kernel.img

sudo tar xvfz buildroot-2015.08.1/output/images/rootfs.tar.gz -C $TARGET_DIR

#Set default's password
RPIPATHVAR=$(openssl passwd -1 -salt xyz esrg)
sed -i -e "s#^default:[^:]*:#default:$RPIPATHVAR:#" $TARGET_DIR/etc/shadow

#Set root's password
RPIPATHVAR=$(openssl passwd -1 -salt xyz root)
sed -i -e "s#^root:[^:]*:#root:$RPIPATHVAR:#" $TARGET_DIR/etc/shadow

#Tune inittab
sed -i -e '/# GENERIC_SERIAL$/s~^.*#~ttyAMA0::respawn:/sbin/getty -L ttyAMA0 115200 vt100 #~' $TARGET_DIR/etc/inittab

#Tune fstab
grep -q "^/dev/mmcblk0p1" $TARGET_DIR/etc/fstab || echo -e "\n/dev/mmcblk0p1\t/boot\t\tvfat\tdefaults\t0\t0" >> $TARGET_DIR/etc/fstab

#Replace interfaces
cd $TARGET_DIR/etc/network
rm interfaces
cp /home/welckron/Documents/scripts/interfaces ./

echo "done"


