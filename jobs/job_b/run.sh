#!/bin/bash

printf "start job b with args: $1 and: $2"
sleep 5
touch /var/lib/scheduler/job_b.done
printf "finish job b"
