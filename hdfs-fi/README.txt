


1. Search for "please modify" in these two files
    - bin/hadoop 
    - fi-build.xml
   and change those entries if needed.

2. bin/extra-ifconfig-up.sh

   (Just run this once, after machine reboots.  This is for just
   aliasing some IP addresses to localhost.  But need ROOT permission,
   unfortunately. Hopefully you have root permission. If not, ask your
   root to run the script).


3. export ANT_OPTS="-Xms1024m -Xmx1024m"

4. ant

5. ant firt

   (You should NOT get any warning!)


6. ant fi

   (It's okay to see lots of warnings!)


7. Then see cloudera-work-trunk-release/README.txt


