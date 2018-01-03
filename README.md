# Forum web site

## Discord integration

Log the user with discord credentials. (https://discordapp.com/developers/docs/topics/oauth2)<br/>
Create account from discord data if the discord's mail do not already exist.<br/>
TODO: save Oauth2 related data for future

# Technical subject / Installation

## Project hierarchy 

Root module: Play application
Discord sub module: Discord api written in scala that use Play WS. 

## Sub module configuration

Sub module need to define the scala version to prevent unresolved dependency

## Basic configuration

### Configuration in application.conf

#### Data base

Create a postgres data base named "forum" (or name it as you want, then modify config) <br/>
Customize data base credential :

<pre>
db {
  # Postgres sql
  default.driver = org.postgresql.Driver
  default.url = "jdbc:postgresql://127.0.0.1/forum"
  default.user= "Your db user"
  default.password= "Your db password"
}
</pre>

Play will connect to this database and perform the required tables creations thanks to play DEV mode

### Configuration in reference.conf

Create a local file "reference.conf" and define the following keys <br/>
This file is local to your installation and ignored by git.

#### Recaptcha 

This application use recaptcha in several forms (like in the application form)

Can be disabled in main config file<br/>
<pre>
local.recaptcha.public = #Google recaptcha public key
local.recaptcha.private = #Google recaptcha private key
</pre>


#### Discord 

You have to create your own Discord app. See Discord documentation.<br/>
TODO link application and server + bot

<pre>
local.discord.user.agent.url = #$url in user agent see https://discordapp.com/developers/docs/reference#user-agent
local.discord.user.agent.version = #$versionNumber in user agent see https://discordapp.com/developers/docs/reference#user-agent
local.discord.client.id = #Discord application client id
local.discord.client.secret = #Discord application client secret
</pre>




