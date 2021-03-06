#!/bin/bash

usage(){
cat << EOF 
usage: $0 host adminName

This script ask questions and install dependencies / configuration to a distant host

OPTIONS:
   host can be localhost or 192.168.0.13 ...
EOF
}

# Context: Where is this script.
cd "$(dirname $0)"
ROOT=$PWD
cd -
COMMONS=$ROOT/commons/
DEPLOYMENT_INSTALL_SCRIPT=install/install.sh
INSTALL_ADMIN=install/addAdminUser.sh

# Add the includes 
. $COMMONS/includes.sh

if [ -z "$1" ] ; then
    error "You forgot to specify a host where to install the dependencies"
    usage
    exit 1
fi
if [ -z "$2" ] ; then
    error "You forgot to specify the admin name"
    usage
    exit 1
fi

ADMIN_NAME=$2

read -p "Do we have to install a '$ADMIN_NAME' user and remove the root login ? (y/N" _reponse
if [ "$_reponse" = "Y" ] || [ "$_reponse" = "y" ] 
then
    remote_launch "root@$1" $INSTALL_ADMIN $ADMIN_NAME
fi
remote_launch "$ADMIN_NAME@$1" $DEPLOYMENT_INSTALL_SCRIPT 

