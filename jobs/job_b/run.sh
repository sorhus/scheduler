#!/bin/bash

printf "start job b with arg: $1"
sleep 5
touch /var/lib/scheduler/job_b.done
printf "finish job b"
