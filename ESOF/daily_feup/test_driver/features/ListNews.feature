Feature: Visualize the list of news of a topic.
  Scenario: When a user chooses the "Eventos Académicos", all the new related to the topic should be shown to him
    Given User is in the page of "TopicsList"
    When  User tap the "Eventos Académicos" button.
    Then The current page is expected to be a list of news of "Eventos Académicos"

  Scenario: When a user chooses the "Anúncios", all the new related to the topic should be shown to him
    Given User is in the page of "TopicsList"
    When  User tap the "Anúncios" button.
    Then The current page is expected to be a list of news of "Anúncios"