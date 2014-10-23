#!/usr/bin/perl
use strict;
use warnings;
use CGI;
use Fcntl qw(:flock :seek);


my $outfile = "/home/hgac/public_html/tracksamples.ini";
my $cgi = new CGI;

# output the content-type so the web server knows

print $cgi->header;

# output the start HTML sequence with a title

#print $cgi->start_html(-title=>'Basic CGI');
#print header;
print $cgi->start_html(-title=>'Track HGAC samples uploaded to Dmel');

print <<END_HTML;
<html>
<head>
</head>
<br></br>
<br></br>
<body>
<h1 style="text-align:center;color:#A8D000;font-family:Trebuchet MS, Arial, Helvetica, sans-serif;">Track Samples Uploaded To dmel.uchospitals.edu</h1>
<br></br>
<br></br>


END_HTML

# open the file for reading
open(IN, "$outfile") or &dienice("Couldn't open $outfile: $!");
# set a shared lock
flock(IN, LOCK_SH); 
# then seek the beginning of the file
seek(IN, 0, SEEK_SET);


print <<end1;

<style type="text/css">
#table1
{
font-family:"Trebuchet MS", Arial, Helvetica, sans-serif;
width:100%;
border-collapse:collapse;
}
#table2
{
font-family:"Trebuchet MS", Arial, Helvetica, sans-serif;
width:40%;
border-collapse:collapse;

}
.center
{
margin:auto;
width:100%;
}

#table1 td, #table1 th 
{
font-size:1em;
border:1px solid #98bf21;
padding:5px 20px 2px 3px;
}
#table1 th 
{
font-size:1.1em;
text-align:center;
padding-top:5px;
padding-bottom:4px;
background-color:#A7C942;
color:#ffffff;
}
#table1 tr.alt td 
{
color:#000000;
background-color:#EAF2D3;
}
</style>

end1

print <<end_html;

<table id="table2" class="center">
  <tr>
    <td>
       <table id="table1" >
         <tr>
            <th>Date Uploaded</th>
            <th> Bionimbus IDs</th>
         </tr>
       </table>
    </td>
  </tr>
  <tr>
    <td>
       <div style="width:100%; height:400px; overflow:auto;">
         <table id="table1" >
  </tr>
  </tr>
           
end_html

my $count=0;
while (my $rec = <IN>) {
   chomp($rec);
   if($count==0)
	{
   		my @values = split(' -- ', $rec);
   		print "<tr><td style=\"text-align:center\">$values[0]</td><td style=\"text-align:center\">$values[1]</td></tr>\n";
		$count=1;
	}
   else
	{
	 	my @values1 = split(' -- ', $rec);
         	print "<tr  class=\"alt\"><td style=\"text-align:center\">$values1[0]</td><td style=\"text-align:center\">$values1[1]</td></tr>\n";
		$count=0;
	}
}
close(IN);

print $cgi->end_table;
#print '</div>';
print '</div>';
print '</td>';
print '</tr>';
print '</table>';

print '</body>';
print $cgi->end_html;

sub dienice {
    my($msg) = @_;
    print h2("Error");
    print $msg;
    print end_html;
    exit;
}
