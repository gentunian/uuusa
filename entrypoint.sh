#!/bin/sh

export IP=$(ip address show dev ${IFACE:-eth0} | awk '/inet/ { gsub("\/.*", "", $2); print $2 }')

# IP=$(hostname -i)
# java \
#     -Dmember=${IP%.*}.1-20 \
#     -Dhostname=${IP} \
#     -cp samulan-0.1.1.jar:./config \
#     org.springframework.boot.loader.PropertiesLauncher -springboot  
# java \
#     -Dhazelcast.local.publicAddress=${IP} \
#     -Dhazelcast.local.localAddress=${IP} \
#     -Dhazelcast.discovery.public.ip.enabled=true \
#     -Dhazelcast.socket.bind.any=false \
#     -cp samulan-0.1.1.jar:./config \
#     org.springframework.boot.loader.PropertiesLauncher -springboot  


java ${JAVA_OPTS} \
    -Dhazelcast.socket.bind.any=false \
    -Dhazelcast.discovery.public.ip.enabled=true \
    -Dhazelcast.local.localAddress=${IP} \
    -Dhazelcast.local.publicAddress=${IP} \
     -jar samulan-0.1.1.jar -springboot
    
