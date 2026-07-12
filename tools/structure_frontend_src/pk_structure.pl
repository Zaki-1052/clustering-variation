#!/usr/bin/perl

print "The source base directory (absolute path) :\n";
chomp($source = <STDIN>);
print "The target directory (not in use) :\n";
chomp($target = <STDIN>);
`mkdir $target`;
pack_dir($source, $target);



sub pack_dir {
    
    my($path, $mirror_path) = @_;
    my($file, @allfiles, $realfile, $mirror_dir);
    
    opendir(DIR, $path) || return;
    


    @allfiles = readdir DIR;
    close DIR;
   
    #process the directory
    foreach $file (@allfiles){
	next if($file eq '..' || $file eq '.') ;
	
	$realfile = $path."\/".$file if $path ne '/';
	
	if (-d $realfile){
	    $mirror_dir = $mirror_path."\/".$file if $mirror_path ne '/';
	    `mkdir $mirror_dir`;
	    pack_dir($realfile, $mirror_dir);
	}
	
	if ($realfile =~ /.class$/) {
	    print "$realfile\n";
	    `cp \'$realfile\' $mirror_path`;
	}
	
    }

}
    
