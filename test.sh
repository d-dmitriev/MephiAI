#!/bin/sh
counter=1
#URL="http://minikube-ingress.local/api/sentiment"
URL="http://192.168.105.6/api/sentiment"
TEXT="%D0%A0%D0%B0%D0%B4%20%D1%87%D1%82%D0%BE%20%D0%BA%D1%83%D0%BF%D0%B8%D0%BB"
INTERVAL=0.1 # <- тут меняем нагрузку

echo "Starting load test for $URL"
echo "Press Ctrl+C to stop"

while true; do
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "${URL}?detailed=true&text=${TEXT}%20${counter}")
# wrk -t12 -c400 -d30s http://127.0.0.1:8080/index.html
    if [ "$http_code" -eq 200 ]; then
      if (( counter % 1000 == 0 )); then
        echo "Request #$counter - $(date '+%H:%M:%S'): ✅ HTTP $http_code"
#      fi
#      if (( counter % 5000 == 0 )); then
        if ((counter < 25000 )); then
          INTERVAL=$(echo "INTERVAL - 0.001" | awk '{ printf "%.1f\n", $1 }')
        else
          INTERVAL=$(echo "INTERVAL + 0.05" | awk '{ printf "%.1f\n", $1 }')
        fi
      fi
    else
      echo "Request #$counter - $(date '+%H:%M:%S'): ❌ HTTP $http_code"
    fi
    counter=$((counter + 1))

    sleep $INTERVAL
done