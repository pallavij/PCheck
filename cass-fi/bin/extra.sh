#!/bin/bash

#for mac os
sudo ifconfig lo0 alias 127.0.0.2 up
sudo ifconfig lo0 alias 127.0.0.3 up
sudo ifconfig lo0 alias 127.0.0.4 up
sudo ifconfig lo0 alias 127.0.0.5 up
sudo ifconfig lo0 alias 127.0.0.6 up
sudo ifconfig lo0 alias 127.0.0.7 up
sudo ifconfig lo0 alias 127.0.0.8 up


#for linux(ubuntu)
#sudo ifconfig lo:0 127.0.0.2 up
#sudo ifconfig lo:1 127.0.0.3 up
#sudo ifconfig lo:2 127.0.0.4 up
#sudo ifconfig lo:3 127.0.0.5 up
#sudo ifconfig lo:4 127.0.0.6 up
#sudo ifconfig lo:5 127.0.0.7 up
#sudo ifconfig lo:6 127.0.0.8 up
