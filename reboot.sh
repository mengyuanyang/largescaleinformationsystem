#!/bin/bash
cd ../../usr/share/tomcat8/webapps/
while read line
do
let R=$line+1
echo $R>Reboot_num.txt
done <Reboot_num.txt
sudo service tomcat8 start