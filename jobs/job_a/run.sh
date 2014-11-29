#!/bin/bash

printf "start job a with arg: $1"
sleep 2
touch /var/lib/scheduler/job_a.done
printf "finish job a"
