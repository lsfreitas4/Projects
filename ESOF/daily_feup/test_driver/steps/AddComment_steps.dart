// TODO Implement this library.import 'package:gherkin/gherkin.dart';
import 'dart:io';
import 'dart:math';
import 'package:uni/controller/local_storage/app_news_database.dart';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'dart:async';
import 'dart:developer';
import 'dart:io';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';

class CommentsAtAnnouncementPage extends Given1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String newId) async {
    final usernameInput = find.byValueKey("username");
    await FlutterDriverUtils.enterText(world.driver,usernameInput,"TestUser");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestUser"));
    final passwordInput = find.byValueKey("password");
    await FlutterDriverUtils.enterText(world.driver,passwordInput,"TestPassword");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestPassword"));
    final logInButton = find.byValueKey("LogInButton");
    await FlutterDriverUtils.tap(world.driver,logInButton);


    await world.driver.tap(find.byTooltip('Open navigation menu'));

    final NoticasButton = find.byValueKey("Noticias");
    await FlutterDriverUtils.tap(world.driver,NoticasButton);
    final pageLocator = find.byValueKey('TopicsList');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);
    final locator = find.byValueKey('Anúncios');
    await world.driver.scrollIntoView(locator);
    await FlutterDriverUtils.tap(world.driver,locator);
    final News = find.byValueKey('ArticleItem_'+newId);
    await FlutterDriverUtils.tap(world.driver,News);

    final lista_comentarios = await find.byValueKey("listaDeComentarios");
    world.driver.scroll(lista_comentarios,0,-3000,Duration(seconds: 3));
  }

  @override
  RegExp get pattern => RegExp(r"User is logged in and reaches the end of a news or an announcement page with id {string}");

}

class WriteAndPressButtonComments extends WhenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final commentInput = find.byValueKey("inserirCometario");
    await FlutterDriverUtils.enterText(world.driver,commentInput,"Teste comentario");
    final addCommentButton = find.byValueKey("plusButton");
    await FlutterDriverUtils.tap(world.driver,addCommentButton);
  }
  @override
  RegExp get pattern => RegExp(r'User enters the comment and selects the button');
}

class CommentSuccessful extends ThenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    //dar scroll até ao fim da lista de ccomentarios
    final lista_comentarios = await find.byValueKey('listaDeComentarios');
    world.driver.scroll(lista_comentarios,0,-3000,Duration(seconds: 3));

    await FlutterDriverUtils.isPresent(world.driver, find.text('Teste comentario'));


  }

  @override
  RegExp get pattern => RegExp(r"The new user's comment is added to the list of comments of that news/announcement page");
}