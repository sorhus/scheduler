#!/bin/bash

printf "start job b with arg: $1"
if [[ $1 = "bar" ]]; then
    sleep 5
elif [[ $1 = "foo" ]]; then
    if [[ $(($RANDOM % 2)) -eq 0 ]]; then
        printf "ERROR in job_b"
        exit 1
    fi
else
    printf "Illegal argument: $1"
fi
touch /var/lib/scheduler/job_b.done
printf "finish job b"
