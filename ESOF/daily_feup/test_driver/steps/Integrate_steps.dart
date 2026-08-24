import 'dart:async';
import 'dart:developer';
import 'dart:io';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:gherkin/gherkin.dart';

class AtLoginPage extends GivenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
  }

  @override
  RegExp get pattern => RegExp(r"The user is at the login page of the Uni app");
}

class TapUnlogin extends WhenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
     final ContinueWithoutLoggingIn = await find.byValueKey('ContinueWithoutLoggingIn');
     final Continue = await find.byValueKey('Continue');
     await world.driver.scroll(Continue,0, -3000,Duration(seconds: 3));
     await FlutterDriverUtils.tap(world.driver,ContinueWithoutLoggingIn);
  }
  @override
  RegExp get pattern => RegExp(r'When The user presses the Continue without logging-in button');
}

class TapNoticias extends Then1WithWorld<String, FlutterWorld> {
  @override
  Future<void> executeStep(String page) async {
    final pageLocator = find.byValueKey(page);
    await FlutterDriverUtils.isPresent(world.driver, pageLocator);
  }

  @override
  RegExp get pattern => RegExp(r'Then The user must see the page {string} of DailyFeup');
}


class LogIn extends GivenWithWorld<FlutterWorld> {
  @override
  Future<void> executeStep() async {
    final usernameInput = find.byValueKey("username");
    await FlutterDriverUtils.enterText(world.driver,usernameInput,"TestUser");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestUser"));
    final passwordInput = find.byValueKey("password");
    await FlutterDriverUtils.enterText(world.driver,passwordInput,"TestPassword");
    await FlutterDriverUtils.isPresent(world.driver, find.text("TestPassword"));
    final logInButton = find.byValueKey("LogInButton");
    await FlutterDriverUtils.tap(world.driver,logInButton);
  }

  @override
  RegExp get pattern => RegExp(r'The user succesfully logs in the Uni app, and is at their Personal Area page');
}

class AccessNoticias extends When1WithWorld<String,FlutterWorld> {
  @override
  Future<void> executeStep(String button) async {
    await world.driver.tap(find.byTooltip('Open navigation menu'));
    final noticasButton = find.byValueKey(button);
    await FlutterDriverUtils.tap(world.driver,noticasButton);
  }
  @override
  RegExp get pattern => RegExp(r'The user accesses the side bar and presses the {string} button');
}
