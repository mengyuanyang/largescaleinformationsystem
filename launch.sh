#!/bin/bash

S3_BUCKET=cs5300hanliwei
AMI=ami-08111162
user_name=Liwei
key_pair=AWS_pro1b
securityGroup=launch-wizard-2
securityGroupId=sg-0c605b74


key_id=AKIAIZKOI22NAKVMF4MQ
secret_key=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

aws configure set aws_access_key_id $key_id
aws configure set aws_secret_access_key $secret_key
aws configure set default.region us-east-1
aws configure set preview.sdb true
aws sdb create-domain --domain-name SimpleDB


aws s3 mb s3://${S3_BUCKET} 
aws s3 cp /Users/$user_name/Desktop/first_servlet.war s3://${S3_BUCKET}/first_servlet.war --grants full=uri=http://acs.amazonaws.com/groups/global/AllUsers

aws ec2 run-instances --image-id ${AMI} --count 3 --instance-type t2.micro --key-name $key_pair --security-groups $securityGroup --security-group-ids $securityGroupId  --user-data file://InstallationScript.sh
