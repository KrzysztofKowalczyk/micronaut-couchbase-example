# Long script for setting up couchbase, it handle retries when there is error because cluster is not read yet

# Main settings
couchbaseAdminUser=Administrator
couchbaseAdminPassword=password
defaultBucket=sampleBucket

# Extra settings
couchbaseServices=data,index,query
clusterRamSizeMb=512
bucketSizeMb=128
indexRamSizeMb=256

# Script settings
retries=20

err() {
    echo "$1" >&2
}

# retry couchbase command until successful
cb(){
    cmd=$1
    for ((run=1; run<=retries; run++))
    do
    echo "Running $cmd"
        /opt/couchbase/bin/couchbase-cli "$@"
    if [ $? -eq 0 ]; then
        echo "Success: $cmd"
        return 0
    fi
    echo "Failed attempt: $cmd $run of $retries"
    sleep 2
    done
    err "Failed to run $cmd after $retries retries"
    exit 1
}

# retry n1ql query until successful
# cqb return 0 even if there is error, so we have to check stderr
n1ql(){
    cmd=$1
    shift
    for ((run=1; run<=retries; run++))
    do
    echo "Running $cmd"
    output=$( /opt/couchbase/bin/cbq --exit-on-error -u ${couchbaseAdminUser} -p ${couchbaseAdminPassword} --script "$@" 2>&1 )
    if [[ "$output" != *"error"* ]] || [[ "$output" == *"already exists"* ]]; then
        echo "Success: $cmd\n$output"
        return 0
    fi
    echo "FAILED: $cmd:\n$output\nretry attempt: $run of $retries"
    sleep 2
    done
    err "Failed to run $cmd after $retries"
    exit 1
}

# define a cluster
cb cluster-init \
    -c 127.0.0.1:8091 \
    --cluster-username=${couchbaseAdminUser} \
    --cluster-password=${couchbaseAdminPassword} \
    --cluster-port=8091 \
    --cluster-ramsize=$clusterRamSizeMb \
    --cluster-index-ramsize=$indexRamSizeMb \
    --services=${couchbaseServices} \
    --index-storage-setting=memopt

# create a bucket
cb bucket-create \
    -c 127.0.0.1:8091 -u ${couchbaseAdminUser} -p ${couchbaseAdminPassword} \
    --enable-flush=0 \
    --bucket=${defaultBucket} \
    --bucket-ramsize=$bucketSizeMb \
    --bucket-replica=0 \
    --bucket-eviction-policy=fullEviction \
    --bucket-type couchbase \
    --wait

# Example of using n1ql to wait for it being ready and setup an index
n1ql "wait for indexer to respond" 'SELECT * FROM system:indexes;'
n1ql "create primary index as example for n1ql usage" "CREATE PRIMARY INDEX \`primary_index\` ON \`${defaultBucket}\` USING GSI;"