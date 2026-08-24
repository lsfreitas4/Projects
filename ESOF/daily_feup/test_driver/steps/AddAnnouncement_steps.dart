import 'dart:async';
import 'dart:developer';
import 'dart:io';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';

class AtAddAnnouncementPage extends GivenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final usernameInput = find.byValueKey("username");
    await FlutterDriverUtils.enterText(world.driver,usernameInput,"TestUser");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestUser"));
    final passwordInput = await find.byValueKey("password");
    await FlutterDriverUtils.enterText(world.driver,passwordInput,"TestPassword");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestPassword"));
    final logInButton = await find.byValueKey("LogInButton");
    await FlutterDriverUtils.tap(world.driver,logInButton);



    await world.driver.tap(find.byTooltip('Open navigation menu'));
    final NoticasButton = await find.byValueKey("Noticias");
    await FlutterDriverUtils.tap(world.driver,NoticasButton);



    final pageLocator = await find.byValueKey('TopicsList');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);
    final locator = await find.byValueKey('Anúncios');
    await world.driver.scrollIntoView(locator);
    await FlutterDriverUtils.tap(world.driver,locator);


    final listNews = await find.byValueKey("listNews");
    await world.driver.scroll(listNews,0, -3000,Duration(seconds: 3));


    final addAnnouncementButton = await find.byValueKey("addAnnouncementButton");
    await FlutterDriverUtils.tap(world.driver,addAnnouncementButton);
  }

  @override
  RegExp get pattern => RegExp(r"The user is logged in and at the add announcement page");
}


class NotLoggedInAtAnnouncementsPage extends GivenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final ContinueWithoutLoggingIn = await find.byValueKey('ContinueWithoutLoggingIn');
    final Continue = await find.byValueKey('Continue');
    await world.driver.scroll(Continue,0, -3000,Duration(seconds: 3));
    await FlutterDriverUtils.tap(world.driver,ContinueWithoutLoggingIn);
    final pageLocator = find.byValueKey('TopicsList');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);
    final locator = find.byValueKey('Anúncios');
    await world.driver.scrollIntoView(locator);
    await FlutterDriverUtils.tap(world.driver,locator);
  }
  @override
  RegExp get pattern => RegExp(r'The user is in the page of announcements and isnt logged in');
}


class ScrollDown extends WhenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final listNews = find.byValueKey("listNews");
    await world.driver.scroll(listNews,0, -3000,Duration(seconds: 3));
  }
  @override
  RegExp get pattern => RegExp(r'The user scrolls down to the bottom of the page');
}



class AddAnnouncementWithoutTitle extends WhenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final contentInput = find.byValueKey("contentInput");
    await FlutterDriverUtils.enterText(world.driver,contentInput,"Conteúdo da Notícia");
    await FlutterDriverUtils.isPresent(world.driver, find.text("Conteúdo da Notícia"));
    final addAnnouncementButton = find.byValueKey("AddAnnouncement");
    await FlutterDriverUtils.tap(world.driver,addAnnouncementButton);
  }
  @override
  RegExp get pattern => RegExp(r'The user tries to create an announcement without a title');
}


class AddAnnouncementWithTitle extends WhenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final titleInput = find.byValueKey("titleInput");
    await FlutterDriverUtils.enterText(world.driver,titleInput,"Titulo da Noticia");
    await FlutterDriverUtils.isPresent(world.driver, find.text("Titulo da Noticia"));
    final contentInput = find.byValueKey("contentInput");
    await FlutterDriverUtils.enterText(world.driver,contentInput,"Conteúdo da Notícia");
    await FlutterDriverUtils.isPresent(world.driver, find.text("1234"));
    final addAnnouncementButton = find.byValueKey("AddAnnouncement");
    await FlutterDriverUtils.tap(world.driver,addAnnouncementButton);
  }
  @override
  RegExp get pattern => RegExp(r'The user tries to create an announcement with at least a title');
}


class ErrorMessage extends ThenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    await FlutterDriverUtils.isPresent(world.driver, find.text("O título da notícia não pode estar vazio"));
  }

  @override
  RegExp get pattern => RegExp(r'The user is shown an error message saying O título da notícia não pode estar vazio');
}


class AnnouncementSuccessful extends ThenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final listNews = find.byValueKey("listNews");
    await world.driver.scroll(listNews,0, -3000,Duration(seconds: 3));
    await FlutterDriverUtils.isPresent(world.driver, find.text("Titulo da Noticia"));
    await FlutterDriverUtils.isPresent(world.driver, find.text("Conteúdo da Noticia"));
  }

  @override
  RegExp get pattern => RegExp(r"The list of announcements containing the user new announcement is shown");
}


class CantSeeAddAnnouncementButton extends ThenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    await FlutterDriverUtils.isAbsent(world.driver,find.byValueKey("addAnnouncementButton"));
  }

  @override
  RegExp get pattern => RegExp(r"The user cannot see the add announcement button");
}
