import 'dart:async';
import 'dart:developer';
import 'dart:io';

import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';

class AtListaTopico extends Given1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String page) async {
    final ContinueWithoutLoggingIn = await find.byValueKey('ContinueWithoutLoggingIn');
    final Continue = await find.byValueKey('Continue');
    await world.driver.scroll(Continue,0, -3000,Duration(seconds: 3));
    await FlutterDriverUtils.tap(world.driver,ContinueWithoutLoggingIn);
    final pageLocator = find.byValueKey(page);
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);
  }

  @override
  RegExp get pattern => RegExp(r"User is in the page of {string}");
}

class TapTopic extends When1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String topic) async {
    final locator = find.byValueKey(topic);

    await FlutterDriverUtils.tap(world.driver,locator).timeout(Duration(minutes: 1));
  }
  @override
  RegExp get pattern => RegExp(r'User tap the {string} button');
}

class AtNewsListPage extends Then1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String topic) async {
    final locator = find.text(topic);
    await FlutterDriverUtils.isPresent(world.driver, locator);
  }

  @override
  RegExp get pattern => RegExp(r'The current page is expected to be a list of news of {string}');
}
