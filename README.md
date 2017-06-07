# Forum web site

## Discord integration

Log the user with discord credentials. (https://discordapp.com/developers/docs/topics/oauth2)<br/>
Create account from discord data if the discord's mail do not already exist.<br/>
TODO: save Oauth2 related data for future

# Technical subject

## Project hierarchy 

Root module: Play application
Discord sub module: Discord api written in scala that use Play WS. 

## Sub module configuration

Sub module need to define the scala version to prevent unresolved dependency

## Basic configuration

Create a local file "reference.conf" and define the following keys <br/>

#### Recaptcha 
Can be disabled in main config file<br/>
<pre>
local.recaptcha.public = #Google recaptcha public key
local.recaptcha.private = #Google recaptcha private key
</pre>


#### Discord 
Create discord application<br/>
TODO link application and server + bot

<pre>
local.discord.user.agent.url = #$url in user agent see https://discordapp.com/developers/docs/reference#user-agent
local.discord.user.agent.version = #$versionNumber in user agent see https://discordapp.com/developers/docs/reference#user-agent
local.discord.client.id = #Discord application client id
local.discord.client.secret = #Discord application client secret
</pre>




