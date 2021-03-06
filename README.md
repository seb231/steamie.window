# steamie.window

This project was influenced by this ![article](https://arstechnica.com/gaming/2014/04/introducing-steam-gauge-ars-reveals-steams-most-popular-games/) and an attempt to get to grips with Clojure and build my first app-like program.

Everyone has backlogs of things to get do, and perhaps none more so than gamers on ![Steam](www.store.steampowered.com), what with their regular crazy sales. Users of Steam have often complained about how deep the Steam library is and how hard it can be to discover new things. Using the data available via the Steam API, I aimed to compare a users playstyle (both games played and amount of time played) with a database of other users, with the aim of identifying users with similar tastes and making suggestion to the original user based on other users game library.

## Usage

Spin up a REPL and call:

`(-main <STEAM-KEY> <STEAM-USER-ID> n)`

Where STEAM-KEY is a Steam API key you can get ![here](http://steamcommunity.com/dev/apikey), STEAM-USER-ID is your Steam user ID usually found in your profile's URL, and _n_ is the number of game recommendations you'd like to return.

## Recommendation System

The latest version of this program returns _n_ games as identified by matching playtimes between the profiled user and the database of users. Playtimes are matched based on a Poisson distribution constructed in the user profile. If a user in the database's play time falls within the play time distribution of the profiled user then that user and their games (those not shared with the profiled user) are captured.

Games are ranked by playtimes per game per user, so that a user in the database has their own games ranked in popularity in terms of time played. These top games can then be taken and those not shared with the users who share a similar profile to the profiled user can be ranked in terms of overall time played per game.

Further details on how the model works can be found in the accompanying ![flowchart](doc/flowchart.pdf).

# The Database

It should be noted that the database, which currently is newly built upon each run (for most up to data results), is biased as users are identified from the profiled users friend list and then from those users friends list. Larger sampling may mitigate this to some extent.

## License

Copyright © 2016 seb231
