Feature: Visualize the list of comments on a news or announcement.
Scenario: User reaches the end of a single news page, all the comments of the users who commented on that news/announcement should be displayed.
Given User is on a single news/announcement page with id "1".
When User reaches the end of new with id "1".
Then User should be able to visualize every comment of new with id "1".