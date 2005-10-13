#!/usr/bin/perl

use strict;

my $mapFile = shift;
my $outFile = shift;

open(IN, $mapFile) || die "Cannot read input: $mapFile";
open(OUT, ">$outFile") || die "Cannot write output: $outFile"; 

my $cat;

while (<IN>) {
    if ( $_ =~ /\s*#\s*(\w[\w\s]+)\s*\n/ ) { $cat = $1; }
    if ( $_ =~ /\s*\*\s*(\w+)\/(\w+)\s*\n/ ) {
	my $schema = $1;
	my $table = $2;
	if ( $cat eq "" ) { die "Saw a table before a category"; } 
	print OUT $cat . "," . $schema . ",", $table . "\n";
    }
}
	 

close(IN);
close(OUT);

