#!/bin/bash

git reset --hard
git pull

source ./build.sh
source ./undeploy.sh
source ./deploy.sh
