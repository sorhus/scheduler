#!/bin/bash

printf "start job c: $1, $2, $3"
sleep 5
touch /var/lib/scheduler/job_c.done
printf "finish job c"
