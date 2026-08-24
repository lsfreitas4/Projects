Feature: Integrate DailyFeup within the Uni app.

Scenario: When Non-Logged User tries to access DailyFeup, all the news topics list are shown to him.
Given The user is at the login page of the Uni app
When The user presses the Continue without logging-in button
Then The user must see the page "TopicsList" of DailyFeup

Scenario: Logged User tries to access DailyFeup
Given The user succesfully logs in the Uni app, and is at their Personal Area page
When The user accesses the side bar and presses the "Noticias" button
Then The user must see the page "TopicsList" of DailyFeup
    