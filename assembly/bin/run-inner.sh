# source this file after seting up appropriate vars

CWDIR=`dirname $0`

if [[ -n ${CP} ]]
then
    CP="${CP}:${CWDIR}/lib/*"
else
    CP="${CWDIR}/lib/*"
fi

# on ec2, eth0 is always an internal interface
INTERNAL_IP=`ifconfig eth0 | grep 'inet addr' | awk '{print $2}' | cut -d: -f 2`

if [[ -z ${INTERNAL_IP} ]]
then
    echo "Could not find internal ip" >&2
    exit 1
else
    echo "Binding to internal ip ${INTERNAL_IP}"
fi

if [[ -z ${ZOOKEEPER_CONNECTION_STRING} ]]
then
    ZOOKEEPER_CONNECTION_STRING="localhost:2281"
fi

ZOOKEEPER_CONNECTION_STRING_KEY=benchpress.zookeeper.client.connection-string

CONFIG_LINES="-D${INTERNAL_IP_CONFIG_KEY}=${INTERNAL_IP} -D${ZOOKEEPER_CONNECTION_STRING_KEY}=${ZOOKEEPER_CONNECTION_STRING}"

java -cp "$CP" \
$CONFIG_LINES \
$@ \
"$MAIN_CLASS"
