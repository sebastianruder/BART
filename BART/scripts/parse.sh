#! /bin/sh
## This is a modified version of parse.sh out of the reranking-parser
## directory.
## Move this into the reranking-parser directory (replacing parse.sh)
## and modify the charniakDir value in the configuration file to match your
## filesystem layout
# RERANKDATA=ec50-connll-ic-s5
# RERANKDATA=ec50-f050902-lics5
if [ x$PARSERDIR != x -a -d $PARSERDIR/first-stage ]; then
	# we're happy if PARSERDIR works
	echo Using $PARSERDIR 1>2
elif [ -d first-stage ] ; then
	PARSERDIR=.
else
	PARSERDIR=$(dirname $(which $0))
fi

MODELDIR=$PARSERDIR/second-stage/models/ec50spfinal
ESTIMATORNICKNAME=cvlm-l1c10P1
$PARSERDIR/first-stage/PARSE/parseIt -l399 -N50 \
	$PARSERDIR/first-stage/DATA/EN/ $* | \
$PARSERDIR/second-stage/programs/features/best-parses \
	-l $MODELDIR/features.gz $MODELDIR/$ESTIMATORNICKNAME-weights.gz
