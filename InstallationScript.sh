#!/bin/bash

S3_BUCKET=cs5300hanliwei
Reboot=0
instance_num=3
key_id=AKIAIZKOI22NAKVMF4MQ
secret_key=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

aws configure set aws_access_key_id $key_id
aws configure set aws_secret_access_key $secret_key
aws configure set default.region us-east-1
aws configure set preview.sdb true

sudo yum -y remove java-1.7.0-openjdk
sudo yum -y install java-1.8.0

sudo yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

sudo chmod 777 /usr/share/tomcat8/webapps/
aws s3 cp s3://${S3_BUCKET}/first_servlet.war /usr/share/tomcat8/webapps/



wget http://169.254.169.254/latest/meta-data/local-ipv4
sudo chomod 777 local-ipv4
sudo mv local-ipv4 /usr/share/tomcat8/webapps/
InternalIP=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)


wget http://169.254.169.254/latest/meta-data/ami-launch-index
sudo chmod 777 ami-launch-index
sudo mv ami-launch-index /usr/share/tomcat8/webapps/
ami=$(curl http://169.254.169.254/latest/meta-data/ami-launch-index)



echo $Reboot>Reboot_num.txt
sudo chmod 777 Reboot_num.txt
sudo mv Reboot_num.txt /usr/share/tomcat8/webapps/
echo $instance_num>instance_num.txt
sudo chmod 777 instance_num.txt
sudo mv instance_num.txt /usr/share/tomcat8/webapps/



aws sdb put-attributes --domain-name SimpleDB --item-name $InternalIP --attributes Name=ServID,Value=$ami

while true; do
    count=$(aws sdb select --select-expression "select count(*) from SimpleDB" --output text | grep -o '[0-9].*')
    if test $count -eq $instance_num
    then
    break
    fi
    sleep 10
done

sudo touch IP_info.txt
sudo chmod 777 IP_info.txt

let count=$count-1
while [  $count -ge 0 ]; do
IPaddr=$(aws sdb select --select-expression "select itemName() from SimpleDB where ServID = '$count'" --output text | grep -o '[0-9].*')
cat <<EOT >> IP_info.txt
$count $IPaddr
EOT
let count=count-1
done
sudo mv IP_info.txt /usr/share/tomcat8/webapps/

#Task6: Start tomcat server
service tomcat8 start
