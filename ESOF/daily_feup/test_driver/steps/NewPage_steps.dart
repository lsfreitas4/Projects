import 'dart:io';
import 'dart:math';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';


class AtListaNews extends Given1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String topic) async {
    final ContinueWithoutLoggingIn = await find.byValueKey('ContinueWithoutLoggingIn');
    final Continue = await find.byValueKey('Continue');
    await world.driver.scroll(Continue,0, -3000,Duration(seconds: 3));
    await FlutterDriverUtils.tap(world.driver,ContinueWithoutLoggingIn);
    final pageLocator = find.byValueKey('TopicsList');
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);

    final locator = find.byValueKey(topic);
    await world.driver.scrollIntoView(locator);
    await FlutterDriverUtils.tap(world.driver,locator);

  }

  @override
  RegExp get pattern => RegExp(r"User is viewing a list of news about {string}");
}



class TapNew extends When1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String newId) async {
    final News = find.byValueKey("ArticleItem_"+newId);
    await FlutterDriverUtils.tap(world.driver,News);

  }
  @override
  RegExp get pattern => RegExp(r'User presses a new with id {string} of that news list');
}

class AtNewPage extends Then1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String idArticle) async {
    final locator = find.byValueKey("Article_"+idArticle);
    await FlutterDriverUtils.isPresent(world.driver, locator);
  }

  @override
  RegExp get pattern => RegExp(r'User should be able to visualize the news page with id {string}.');
}
