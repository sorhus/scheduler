{
    "name":"",
    "description":"",
    "parameters":[],
    "dependencies":[],
}

# $name/done.sh
# $name/run.sh
# $name/kill.sh
# $name/clean.sh


* config sourced in scripts, e.g. environment specific
* job specific params in json, e.g. country
* the job with a "main" property defines a pipe, value is pipe param, e.g. date
* pipe param prepended to job specific params
* a dependency lacks params, i.e. must be done for all params

