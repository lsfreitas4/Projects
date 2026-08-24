import 'package:flutter_redux/flutter_redux.dart';
import 'package:uni/controller/load_news/webscraper_news.dart';
import 'package:uni/controller/local_storage/app_news_database.dart';
import 'package:uni/view/Pages/new_page_view.dart';
import 'package:uni/view/Pages/general_page_view.dart';
import 'package:flutter/material.dart';
import 'dart:developer';
import 'package:uni/view/Pages/add_anouncement.dart';
import 'package:uni/model/news_entities/article.dart';
import '../../model/app_state.dart';
import '../Widgets/page_title.dart';

class ListaAnuncios extends StatefulWidget {
  final String _topic;
  const ListaAnuncios(this._topic);
  String getAnuncios(){
    return _topic;
  }
  State<StatefulWidget> createState() => ListaAnunciosState(_topic);
}


class ListaAnunciosState extends GeneralPageViewState{
  List<Article> news = [];
  AppNewsDatabase databaseClient;
  final String topic;
  bool loading = true;
  ListaAnunciosState(this.topic);
  
  void createdb() async {

    await WebScraperNews.updateNews(databaseClient, topic);
    news = (await databaseClient.getArticles(topic)).values.toList();
    loading = false;
    setState(() {});
  }


  @override
  void initState() {
    super.initState();
    // abir a base de dados
    databaseClient = AppNewsDatabase();
    news = [];
    createdb();
  }

  List<Widget> buildNews(){
    List<Widget> listNews = [];

    if(!loading) {
      for (var i = 0; i < news.length; i++) {

        listNews.add(Container(
          decoration: const BoxDecoration(
            border: Border(
              top: BorderSide( //
                color: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
                width: 1.5,
              ),
            ),
          ),
          child: TextButton(
              key: Key('ArticleItem_' + news[i].id.toString()),
              onPressed: () {
                Navigator.push(context, MaterialPageRoute(
                    builder: (context) =>
                        NewPage(news[i],
                            key: Key("Article_" + news[i].id.toString()))));
              },
              child: Column(
                  children: [
                    Align(alignment: Alignment.centerLeft,
                        child: Text(news[i].title, textAlign: TextAlign.left,
                            style: TextStyle(fontSize: 17))),
                    Align(alignment: Alignment.centerLeft,
                        child: Text((topic == 'Anúncios'? 'Author: '+news[i].author : news[i].subtitle), textAlign: TextAlign.left,
                            style: TextStyle(color: Colors.grey),
                            overflow: TextOverflow.ellipsis,
                            maxLines: 4))
                  ]
              ),
              style: ButtonStyle(backgroundColor: MaterialStateProperty.all(
                  const Color.fromRGBO(
                      0xFF, 0xFF, 0xFF, 1.0)),
                  foregroundColor: MaterialStateProperty.all(Colors.black))),
        ));
      }
    }
    return listNews;
  }

  @override
  Widget build(BuildContext context) {
    Widget bodyContainer;
    if(loading) {
      bodyContainer =Column(
          children: [
            PageTitle(name: 'Noticias - ' + topic),
            Flexible(
                child: Center(child: CircularProgressIndicator())
            )
          ]
      );
    }else{
      List<Widget> list =[PageTitle(name: 'Noticias - ' + topic)];
      list.addAll(buildNews());
      bodyContainer = ListView(
        key:Key("listNews"),
        children: list
      );
    }


    buildAppBar(context);

    if (topic == 'Anúncios' && StoreProvider.of<AppState>(context).state.content['profile'] != null) {
      return MaterialApp(
          title: 'Daily Feup',
          theme: ThemeData(
            scaffoldBackgroundColor: const Color.fromRGBO(0xFF, 0xFF, 0xFF, 1),
          ),
          home: Scaffold(
            body: bodyContainer,
            appBar: buildAppBar(context),
            floatingActionButton: FloatingActionButton(
              key:Key("addAnnouncementButton"),
              onPressed: () {
                {
                  Navigator.push(context,
                      MaterialPageRoute(builder: (context) => AddAnnouncement())).then((_){
                    createdb();
                  });
                };
              },
              backgroundColor: Colors.white,
              child: const Icon(Icons.add,
                  color: Colors.black),
            )
          )
      );

    }

    return MaterialApp(
        title: 'Daily Feup',
        theme: ThemeData(
          scaffoldBackgroundColor: const Color.fromRGBO(0xFF, 0xFF, 0xFF, 1),
        ),
        home: Scaffold(
            body: bodyContainer,
            appBar: buildAppBar(context),
        )
    );
  }
}