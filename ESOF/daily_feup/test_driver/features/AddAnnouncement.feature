Feature: Create an announcement.Only Logged Users can create an announcement. For an announcement to be created it must have at least a title

Scenario: Logged User tries to create an announcement without a title
Given The user is logged in and at the add announcement page
When The user tries to create an announcement without a title
Then The user is shown an error message saying O título da notícia não pode estar vazio

Scenario: Logged User tries to create an announcement
Given The user is logged in and at the add announcement page
When The user tries to create an announcement with at least a title
Then The list of announcements containing the user new announcement is shown

Scenario: Non-Logged User tries to create an announcement
Given The user is in the page of announcements and isnt logged in
When The user scrolls down to the bottom of the page
Then The user cannot see the add announcement button