# source this file after seting up appropriate vars

CWDIR=`dirname $0`

if [[ -n ${CP} ]]
then
    CP="${CP}:${CWDIR}/lib/*"
else
    CP="${CWDIR}/lib/*"
fi

java -cp "$CP" $@ "$MAIN_CLASS"
