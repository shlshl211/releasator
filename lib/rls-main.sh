# cli generic stuff

##
# Prints usage
#
function printUsage() {
    cat <<EOF
Releasator
Synopsis:
    $0 [<option>...] <subcommand> [arg...]

Supported options:
    --help  -h  this help

Supported subcommands:
EOF
    sed -n '/^#CMD#/{s:^#CMD#:\n    :;s# : #\n\t#;p;}' $0
    echo
}

##
# Performs a subcommand, with all the decorations
#
# @param subcommand  one of supported subcommands, see `printUsage`
# @param *           argument list of the subcommand
#
function perform() {
    local subcommand=$1
    shift
    echo "------ $subcommand ------"
    echo "Executing: $0 $subcommand $@"
    CMD_"$subcommand" "$@"
    local RV=$?
    echo "------ $subcommand exits with $RV ------"
    return $RV
}

#### MAIN ####

CMD=""
while true; do
    arg="$1"
    shift
    case "$arg" in
    '--help'|'')
        printUsage
        exit 1;;
    '--'*)
        echo "ERROR: Invalid option: $arg" >&2
        exit 1;;
    *)
        CMD="$arg"
        break;;
    esac
done

if ! grep -q "^#CMD#$CMD\b" $0; then
    echo "ERROR: Unsupported command: $CMD" >&2
    echo "       Try $0 --help to see available commands." >&2
    exit 1
fi

perform "$CMD" "$@"
