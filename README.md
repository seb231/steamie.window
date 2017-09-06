# steamie.window

;;; TODO
;;; Add Usage

There are two avenues for future development, oulined below.

## 1. Data Science Analysis

It's now relatively easy to build up a database of users (and I can easily "get" a lot more users), each with a list of games and playtimes. This data could be mined for interesting characteristics, as per a usual data science investigation, so drawing out some summary statistics (playtimes, number of games owned, played, comparisons of number of games owned to how much time is spent per game or on specific games.

This analysis could also help inform the recommendation system!

## 2. Recommendation System

The latest version of this program returns the top _n_ most popular games as identified by matching playtimes between the profiled user and the database of users. Playtimes are matched based on a Poisson distribution constructed in the user profile. If a user in the datbase's play time falls within the play time distribution of the profiled user then that user and their games (not shared with the profiled user) are captured.

The results are collated and ranked in order of popularity, in terms of the number of users. This is too naive, as essentially just the most commonly owned games are returned, not really the most popular.

What is needed is a ranking of playtimes per game per user, so that a user in the database has their own games ranked in popularity in terms of time played. These top games can then be taken and those shared between the users who share a similar profile to the profiled user can be ranked in terms of overall time played per game.

# The Database

It should be noted that the database, which currently is newly built upon each run (for most up to data results), is biased as users are identified from the profiled users friend list and then from those users friends list. Larger sampling may mitigate this to some extent.

## License

Copyright © 2016 seb231
