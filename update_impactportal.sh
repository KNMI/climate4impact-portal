#/bin/bash

""" First check if repo has changed """
stat=$(hg stat)

if [ -z ${stat+x} ]; then echo "stat is unset"; else echo "stat is set to '$stat'"; fi