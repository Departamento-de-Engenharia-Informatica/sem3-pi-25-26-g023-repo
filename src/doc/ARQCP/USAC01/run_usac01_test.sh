#!/bin/bash
setxkbmap pt
cd /media/sf_partilha/tiago/utests/usac01
export REPO=user_stories
make -B
make run