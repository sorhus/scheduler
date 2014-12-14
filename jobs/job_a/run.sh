#!/bin/bash

printf "start job a with arg: $1"
sleep 5
touch /var/lib/scheduler/job_a.done || exit 1
printf "finish job a"
