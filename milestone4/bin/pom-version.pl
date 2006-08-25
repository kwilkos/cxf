#!/usr/bin/env perl

use strict;
use File::Find;
use XML::Parser;
use Getopt::Long;

my $master_version;
GetOptions('version=s' => \$master_version);

my $parser = new XML::Parser;
$parser->setHandlers(Start => \&findversions);

my ($top_ver_line, $parent_ver_line);

my $start_dir = shift || '.';
File::Find::find(\&fixpomfiles, $start_dir);

my $parent_version;

sub fixpomfiles {
    return unless (/^pom.xml$/);
    print "processing $File::Find::name\n";
    my $pom = $_;
    $top_ver_line = $parent_ver_line = 0;
    $parser->parsefile($pom);
    return unless ($top_ver_line != 0 || $parent_ver_line != 0);
    local *POM;
    open(POM, $pom) || die "cannot open $pom: $!\n";
    my $line = 0;
    my @new;
    while (my $val = <POM>) {
        ++$line;
        if ($line == $top_ver_line) {
            $val =~ m:^(\s*)<version>([^<]+)</version>:;
            my ($space, $vers) = ($1, $2);
            my $newvers = $vers;
            if ($vers =~ /-SNAPSHOT$/) {
                $newvers =~ s/-SNAPSHOT$//;
            } else {
                $newvers += 0.1;
                $newvers .= '-SNAPSHOT';
            }
            print "  existing version is $vers\n";
            my $default_vers = $master_version || $newvers;
            print "  please enter new version [$default_vers]: ";
            my $version = <STDIN>;
            chomp $version;
            $version = $default_vers unless $version;
            push @new, "$space<version>$version</version>\n";
            $parent_version = $version if ($File::Find::dir eq $start_dir);
        } elsif ($line == $parent_ver_line) {
            $val =~ m:^(\s*)<version>[^<]+</version>:;
            push @new, "$1<version>$parent_version</version>\n";
        } else {
            push @new, $val;
        }
    }
    close POM;
    rename $pom, "${pom}.bak";
    open(POM, ">$pom") || die "cannot open $pom: $!\n";
    print POM @new;
    close POM;
}

sub findversions {
    my ($p, $el) = @_;
    return unless ($el eq 'version');
    my $parent = $p->current_element;
    if (defined($parent)) {
        $parent_ver_line = $p->current_line if ($parent eq 'parent');
        $top_ver_line = $p->current_line if ($parent eq 'project');
    }
}
