
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter_html/flutter_html.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:intl/intl.dart';
import 'package:uni/model/news_entities/comment.dart';

import '../../controller/local_storage/app_news_database.dart';
import '../../model/app_state.dart';
import '../../model/news_entities/article.dart';
import 'general_page_view.dart';


class NewPage extends StatefulWidget {
  final Article article;
  NewPage(this.article, {Key key}) : super(key: key){
  }

  State<StatefulWidget> createState() => NewsContentState(article);
}

class NewsContentState extends GeneralPageViewState{
  final Article article;

  NewsContentState(this.article);
  List<Comment> comments = [];
  AppNewsDatabase databaseClient;

  createdb() async {
    comments = await databaseClient.getComments(article.id);

    setState((){});
  }


  @override
  void initState() {
    super.initState();
    databaseClient = AppNewsDatabase();
    comments = [];
    createdb();
  }

  List<Widget> buildComments(){

    List<Widget> listComments = [];
    listComments.add(Container(
        margin: const EdgeInsets.only(top: 16.0),
        child: const Text("Comentarios",
                style: TextStyle(fontSize: 25.0,
                    fontWeight: FontWeight.bold,
                    color: Colors.black)))
    );

    if (StoreProvider.of<AppState>(context).state.content['profile'] != null) {
      TextEditingController commentController = TextEditingController();


      final DateFormat formatter = DateFormat('yyyy-MM-dd HH:mm');

      listComments.add(Container(
        padding: const EdgeInsets.all(16),
        child:Row(
            children: [
              Expanded(
                child:Container(
                  color:Colors.white,
                child: TextField(
                    key:Key("inserirCometario"),
                    controller:commentController,
                    style: TextStyle(color: Colors.black),
                    decoration: const InputDecoration(
                        contentPadding: EdgeInsets.symmetric(horizontal:16.0),
                        hintText: 'Adiciona um comentario',
                        hintStyle: TextStyle(color:Colors.grey),
                        focusedBorder:OutlineInputBorder(
                          borderSide: const BorderSide(color:Colors.white,width:2.0),
                        )
                    )
                ),
              )
              ),
              Container(
                margin: const EdgeInsets.only(left:12.0),
                child: FloatingActionButton(
                    key:Key("plusButton"),
                    onPressed:(){
                        final Comment comment = Comment(0,
                            StoreProvider.of<AppState>(context).state.content['profile'].name,
                            commentController.text,
                            formatter.format(DateTime.now()).toString(), article.id);
                          databaseClient.addComment(comment).then((n){
                          createdb();

                        });
                    },
                    backgroundColor: Colors.white,
                    child: const Icon(Icons.add,
                                      color:Colors.black),
                )
              ),
            ],
          ),
        )
      );
    }

    for(var i = 0; i < comments.length; i++) {
      listComments.add( Container(
          margin: const EdgeInsets.only(top: 16.0, bottom: 2.0),
          alignment: Alignment.centerLeft,
          key:  Key("comment_" + comments[i].id.toString()),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            children:[

              Text(comments[i].name,
                style: const TextStyle(fontSize: 17.0,
                    fontWeight: FontWeight.normal,
                    color: Colors.black)),
              Text(comments[i].date.toString(),
                  style: const TextStyle(fontSize: 13.0,
                      fontWeight: FontWeight.normal,
                      color: Colors.grey)),
              Text(comments[i].text,style: const TextStyle(fontSize: 15.0,
                fontWeight: FontWeight.normal,
                color: Colors.black))],
          )));
    }
    return listComments;
  }

  Widget createImage(){

    if(article.image != null){
      return Container(
        margin: const EdgeInsets.all(20),
        child: Image.memory(article.image));
    }

    return Container();
  }

  List<Widget> buildNew(){
    if(article.subject == 'Anúncios'){
      return [Text(article.title
          , style: const TextStyle(fontSize: 25.0,
              fontWeight: FontWeight.normal,
              color: Colors.black)),
        Text(article.author
            , style: const TextStyle(fontSize: 20.0,
                fontWeight: FontWeight.normal,
                color: Colors.grey)),
        createImage(),
        Text(article.body, style: const TextStyle(fontSize: 15.0,
            fontWeight: FontWeight.normal,
            color: Colors.black))
      ];
    }

    return [Text(article.title
        , style: const TextStyle(fontSize: 25.0,
            fontWeight: FontWeight.normal,
            color: Colors.black)),
    Text(article.subtitle
    , style: const TextStyle(fontSize: 20.0,
    fontWeight: FontWeight.normal,
    color: Colors.grey)),

    createImage(),
    Html( data: article.body)
    ];

  }


  @override
  Widget build(BuildContext context) {
    List<Widget> listComments = buildComments();
    List<Widget> newWidgets = buildNew();

    if(listComments.isNotEmpty){
      newWidgets.addAll(listComments);
    }

    return MaterialApp(
        theme: ThemeData(
          scaffoldBackgroundColor: Colors.white,
        ),
        home: Scaffold(
          appBar: buildAppBar(context),
          body: ListView(
              padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 10),
              key:  Key("listaDeComentarios"),
              children: newWidgets
          ),

        )
    );
  }
}