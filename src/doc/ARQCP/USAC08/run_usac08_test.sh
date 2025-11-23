#!/bin/bash
setxkbmap pt
cd /media/sf_partilha/tiago/utests/usac08
export REPO=user_stories
make -B
make run