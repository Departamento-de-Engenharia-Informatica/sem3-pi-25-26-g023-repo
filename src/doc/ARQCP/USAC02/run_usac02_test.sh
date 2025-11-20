#!/bin/bash
setxkbmap pt
cd /media/sf_partilha/tiago/utests/usac02
export REPO=user_stories
make -B
make run