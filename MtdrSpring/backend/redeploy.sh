#!/bin/bash

git reset --hard
git pull

source ./undeploy.sh
source ./deploy.sh
