import 'dart:developer';

import 'package:uni/controller/load_news/webscraper_news.dart';
import 'package:uni/controller/local_storage/app_news_database.dart';
import 'package:uni/model/news_entities/topic.dart';

import 'package:uni/view/Pages/general_page_view.dart';
import 'package:uni/view/Pages/anouncements_list_page.dart';
import 'package:flutter/material.dart';

import '../Widgets/page_title.dart';

class ListaTopico extends StatefulWidget {
  const ListaTopico({Key key}) : super(key: key);


  State<StatefulWidget> createState() => ListaTopicoState();
}


class ListaTopicoState extends GeneralPageViewState{

  List<Topic> topics = [];
  AppNewsDatabase databaseClient;
  bool loading = true;

  createdb() async {
    WebScraperNews.updateTopicos(databaseClient).then((bool updated) async{
        Map<String, Topic> lista = await databaseClient.getTopics();


        for (var topic in lista.values) {
            topics.add(topic);
        }
        loading = false;
        setState((){});
    });


  }


  @override
  void initState() {
    super.initState();
    // abir a base de dados
    databaseClient = AppNewsDatabase();
    topics = [];
    createdb();
  }

  List<Widget> buildTopics(){

    List<Widget> listTopics = [];
    listTopics.add( PageTitle(name: 'Lista de Topicos'));

    for(var i = 0; i < topics.length; i++) {
      listTopics.add( Container(
        margin: const EdgeInsets.all(16.0),
        height: 50,
        child: TextButton(
            key: Key(topics[i].name),
            onPressed: () { Navigator.push(context,
                MaterialPageRoute(builder: (context) =>  ListaAnuncios(topics[i].name))); },
            child: Text(topics[i].name),
            style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                foregroundColor: MaterialStateProperty.all(Colors.black))),
      ));
    }
    return listTopics;
  }


  @override
  Widget getBody(BuildContext context) {
    if(loading) {
      return Column(
          children: [
            PageTitle(name: 'Lista de Topicos', key: Key('TopicsList')),
            Flexible(
                child: Center(child: CircularProgressIndicator())
            )
          ]
      );
    }


    return ListView(
        key: Key('TopicsList'),
        children: buildTopics());
  }


}