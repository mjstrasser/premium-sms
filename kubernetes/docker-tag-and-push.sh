#!/usr/bin/env bash

CHARGING_SERVICE=premium-sms-charging-service
MAIN_SERVICE=premium-sms-main-service

REPOSITORY=asia.gcr.io/spheric-mission-238712

push_latest() {
    docker tag ${1}:latest ${REPOSITORY}/${1}:latest
    docker push ${REPOSITORY}/${1}:latest
}

push_latest ${CHARGING_SERVICE}
push_latest ${MAIN_SERVICE}
