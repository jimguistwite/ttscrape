TitanTV.com TV Listing Scraper
==============================

For personal use, this tool scrapes the titantv.com web site in an
attempt to find shows of interest.

Usage
-----

Sign up with an account on titantv.com.  Configure your main channel
lineup filtering out unwanted channels, setting the default grid
duration to 6 hours, default titantv color scheme, checking "Allow Cell
Text to Wrap", checking "Use Fixed Height Grid Cells", and selecting
all items in "Programming Grid Fields".


Update the config/titlestoignore.txt and config/titlestolookfor.txt
files to taste and enter your titantv.com login and password in
src/main/resources/ttconfig.properties using ttconfig.sample as a
template.


Future Design Considerations
----------------------------

I currently run the tool weekly looking at the upcoming week of
television shows.  I've considered having this run as a deamon
continuously fetching the next day of programming when available.

I would like the ability to mark certain shows for detailed
information follow-up.  This design calls for a queue of shows and a
handler for that queue that retrieves the show detailed description from
the titantv.com site storing that data with the show.  This would
allow filtering of movies by actor or other such properties.

The webapp interface was a recent add-on allowing for easier viewing
of results and quick integration with imdb.  A nice feature would be
to automatically program a DVR device to record a show.  Being a Dish
Network customer, I have yet to find an API to set recordings on my
device and scraping the recording site appears difficult.

License
-------

Copyright 2012 Jim Guistwite

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
