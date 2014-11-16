# Web Service Finder

Used to find webservice via OWL.

# Required OS

Linux

# Developing and installing

## Installing apache

Use the following command to install and run the apache server on Ubuntu
sudo apt-get install apache2
sudo service apache2 start

## Dataset

All the folders present in the folder named `dataset` have to be placed in the root directory of your web server.

## Ruby Installation

You need to install latest ruby(2.1.0) if you do not have it. You can use the following commands to do so.
$ gpg --keyserver hkp://keys.gnupg.net --recv-keys D39DC0E3
$ curl -sSL https://get.rvm.io | bash -s stable --ruby
$ PATH="~/.rvm/scripts/rvm:${PATH}"
$ export PATH
$ rvm install 2.1.0
$ rvm use 2.1.0
(If the above command fails, change the gnome terminal settings as: Edit->Profile PReferences->Title and Command-> Check the `Run command as a login shell` checkbox)
$ rvm rubygems latest

## Sinatra server

Install sinatra server using the command 
$ gem install sinatra

## Runnning the server

To run the server you need to do the following
$ cd /path/to/the/project/folder
$ rvm use 2.1.0
$ ruby server.rb -o 0.0.0.0

## Accessing the server

Open the browser and point it to the URL `127.0.0.1:4567` 

