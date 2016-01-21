#!/bin/bash

cmd_path=`dirname $(pwd)/${0}`
dst_file=$cmd_path/com.autonavi.plugin.jar
libs_path=$cmd_path/libs
tmp_path=$cmd_path/temp

## 创建新的temp目录
rm $dst_file
rm -rf $tmp_path
mkdir -p $tmp_path

## 进入temp目录
pushd $tmp_path > /dev/null

## 解压libs
for f in $libs_path/*.jar; do
    unzip -o $f > /dev/null
done

## 删除已重写的support v4代码
rm android/support/v4/app/Fragment.class
rm android/support/v4/app/Fragment\$*.class
rm android/support/v4/app/FragmentState.class
rm android/support/v4/app/FragmentState\$*.class
rm android/support/v4/app/FragmentContainer.class
rm android/support/v4/app/FragmentManager.class
rm android/support/v4/app/FragmentManager\$*.class
rm android/support/v4/app/FragmentManagerImpl.class
rm android/support/v4/app/FragmentManagerImpl\$*.class
rm android/support/v4/app/FragmentManagerState.class
rm android/support/v4/app/FragmentManagerState\$*.class

## 删除不使用的代码
rm -rf android/support/v4/print
rm -rf android/support/v4/provider
rm -rf android/support/v4/speech
rm -rf android/support/v4/text
rm -rf android/support/v4/net
rm -rf android/support/v4/media
rm -rf android/support/v4/hardware
rm -rf android/support/v4/database
rm -rf android/support/v4/accessibilityservice

## 查找并拷贝编译到temp目录
root_path=../
for i in {1..3} ; do
    cls_file=`find $root_path -wholename */com/autonavi/plugin/MsgCallback.class`
    count=`echo $cls_file | wc -c`
    if [ $count -gt 1 ] ; then
        cls_dir=`dirname $cls_file`/../../../
        ls $cls_dir
        cp -R $cls_dir ./
        break
    fi
    root_path=../$root_path
done

## 压缩temp目录里所有文件
zip -r $dst_file * > /dev/null

echo SUCCESS:$dst_file

## 退出temp目录
popd > /dev/null

