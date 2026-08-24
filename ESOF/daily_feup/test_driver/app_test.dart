import  'dart:async';
import 'package:flutter_gherkin/flutter_gherkin.dart';
import 'package:gherkin/gherkin.dart';
import 'package:glob/glob.dart';
import 'steps/ListNews_steps.dart';
import 'steps/NewPage_steps.dart';
import 'steps/ListComments_steps.dart';
import 'steps/AddComment_steps.dart';
import 'steps/AddAnnouncement_steps.dart';
import 'steps/Integrate_steps.dart';


Future<void> main() {
  final config = FlutterTestConfiguration()
    ..features = [Glob(r'test_driver/features/**.feature')]
    ..reporters = [
      ProgressReporter(),
      TestRunSummaryReporter(),
      JsonReporter(path: './report.json')
    ]
    ..stepDefinitions = [
      AtListaTopico(), TapTopic(), AtNewsListPage(),
      AtListaNews(), TapNew(), AtNewPage() ,
      CommentsAtNewPage(),ScrollNews(),FindComment(),
      AtLoginPage(), TapUnlogin(), TapNoticias(),
      AccessNoticias(),LogIn(),
       CommentsAtAnnouncementPage(),WriteAndPressButtonComments(),CommentSuccessful(),
       AtAddAnnouncementPage(),NotLoggedInAtAnnouncementsPage(),ScrollDown(),
       AddAnnouncementWithoutTitle(),AddAnnouncementWithTitle(),
       ErrorMessage(),AnnouncementSuccessful(),CantSeeAddAnnouncementButton()
      ]
    ..defaultTimeout=Duration(minutes: 2) // uma vez que vamos buscar dados a net as vezes é necessario espera um bocado mais de tempo
    ..restartAppBetweenScenarios = true
    ..customStepParameterDefinitions = []
    ..targetAppPath = "test_driver/app.dart";
  return GherkinRunner().execute(config);
}