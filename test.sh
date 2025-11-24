#!/bin/sh
counter=1
#URL="http://minikube-ingress.local/api/sentiment"
URL="http://192.168.105.6/api/sentiment"
TEXT="%D0%A0%D0%B0%D0%B4%20%D1%87%D1%82%D0%BE%20%D0%BA%D1%83%D0%BF%D0%B8%D0%BB"
INTERVAL=0.01 # <- тут меняем нагрузку

while true; do
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "${URL}?detailed=true&text=${TEXT}%20${counter}")

    if [ "$http_code" -eq 200 ]; then
      echo "Request #$counter - $(date '+%H:%M:%S'): ✅ HTTP $http_code"
    else
      echo "Request #$counter - $(date '+%H:%M:%S'): ❌ HTTP $http_code"
    fi

    counter=$((counter + 1))

    sleep $INTERVAL
done