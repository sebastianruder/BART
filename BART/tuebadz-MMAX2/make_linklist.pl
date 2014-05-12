#!/usr/bin/env/perl -w
use strict;

my $in_sent=0;
my $sent_no;
my $token_no;

while (<>)
{
    if (/^\#BOS[ \t]+([0-9]+)/) {
	$sent_no=$1;
	$token_no=0;
	$in_sent=1;
    } elsif (/^\#EOS/) {
	$in_sent=0;
    } elsif ($in_sent) {
	my $node_id;
	if (/^\#([0-9]+)/) {
	    $node_id=$1;
	} else {
	    $node_id=++$token_no;
	}
	if (/%% R=(anaphoric|coreferential|cataphoric)\.([0-9]+):([0-9]+)/) {
	    print "$sent_no:$node_id\t$2:$3\n";
	}
    }
}
