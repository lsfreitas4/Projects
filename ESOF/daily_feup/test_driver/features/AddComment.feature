Feature: Comment functionality for users
Scenario: When a logged in user reaches the end of a news or an announcement and tries  to comment it.
    Given User is logged in and reaches the end of a news or an announcement page with id "1"
    When User enters the comment and selects the button
    Then The new user's comment is added to the list of comments of that news/announcement page