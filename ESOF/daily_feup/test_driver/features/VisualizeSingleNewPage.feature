Feature: Visualize a single news page.
  Scenario: When a user presses on a single news which is contained on the list of news of the same topic, the user should be able to visualize the page that contains that entirity of that news.
  Given User is viewing a list of news about "Anúncios".
  When User presses a new with id "1" of that news list.
  Then User should be able to visualize the news page with id "1".