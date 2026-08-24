import 'dart:io';
import 'dart:math';
import 'package:uni/controller/local_storage/app_news_database.dart';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';


class CommentsAtNewPage extends Given1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String newId) async {
    final ContinueWithoutLoggingIn = await find.byValueKey('ContinueWithoutLoggingIn');
    final Continue = await find.byValueKey('Continue');
    await world.driver.scroll(Continue,0, -3000,Duration(seconds: 3));
    await FlutterDriverUtils.tap(world.driver,ContinueWithoutLoggingIn);

    final pageLocator = find.byValueKey('TopicsList');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);


    final ListNews = find.byValueKey("Anúncios");
    await world.driver.scrollIntoView(ListNews);
    await FlutterDriverUtils.tap(world.driver,ListNews);

    final pageLocator2 = find.text('Anúncios');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator2);



    final News = find.byValueKey("ArticleItem_"+newId);
    await FlutterDriverUtils.tap(world.driver,News);

  }

  @override
  RegExp get pattern => RegExp(r"User is on a single news/announcement page with id {string}");
}



class ScrollNews extends When1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String newId) async {
    final lista_comentarios = await find.byValueKey("listaDeComentarios");

    world.driver.scroll(lista_comentarios,0,-3000,Duration(seconds: 3));
  }
  @override
  RegExp get pattern => RegExp(r'User reaches the end of new with id {string}.');
}

class FindComment extends Then1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String idArticle) async {

    await FlutterDriverUtils.isPresent(world.driver, find.text("Comentarios"));

  }

  @override
  RegExp get pattern => RegExp(r'User should be able to visualize every comment of new with id {string}.');
}