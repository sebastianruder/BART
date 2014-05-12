use strict;

my %sets=();
my %elements=();
my $next_set_id=0;

while (<>) {
    split;
    my @ks=();
    foreach (@_) {
	push @ks, $sets{$_} if $sets{$_};
    }
    my $llen=scalar @ks;
    my $set_id;
    if ($llen==0) {
	$set_id=sprintf("set_%05d",++$next_set_id);
    } elsif ($llen==1) {
	$set_id=$ks[0];
    } else {
	my @a=();
	$set_id=$ks[0];
	foreach my $set2 (@ks) {
	    next unless $set2;
	    my @elm=@{$elements{$set2}};
	    $elements{$set2}=[];
	    @a=(@a,@elm);
	    foreach my $e (@elm) {
		$sets{$e}=$set_id;
	    }
	}
	$elements{$set_id}=\@a;
    }
    foreach my $e (@_) {
	my $old_setid=$sets{$e};
	$sets{$e}=$set_id;
	if ($old_setid ne $set_id) {
	    push @{$elements{$set_id}}, $e;
	}
    }
}

foreach (sort keys(%elements)) {
    foreach my $e (@{$elements{$_}}) {
	print "$e\t$_\n";
    }
}
