import 'dart:async';
import 'dart:developer';
import 'dart:typed_data';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite/sqflite.dart';
import 'package:uni/controller/locaL_storage/app_news_database.dart';
import 'package:uni/model/news_entities/article.dart';
import 'package:uni/model/news_entities/comment.dart';
import 'package:uni/model/news_entities/topic.dart';

class DatabaseMockGet implements Database{
  List<Map<String, Object>> listReturned; 
  String table;
  String where;
  List<Object> whereArgs;
  List<Map<String, Object>> insertValues;
  
  bool inserted = false;

  DatabaseMockGet(this.table, 
      {this.listReturned, this.where, this.whereArgs, this.insertValues}) :
        super();
  

  @override
  Future<List<Map<String, Object>>> query(String table,
      {bool distinct, List<String> columns, String where, List<Object> whereArgs, String groupBy, String having, String orderBy, int limit, int offset}) {
    
    expect(table, this.table);
    expect(where, this.where);
    expect(whereArgs, this.whereArgs);
    
    final completer = Completer<List<Map<String, Object>>>();
    completer.complete(listReturned);
    return completer.future;
  }

  @override
  Future<int> insert(String table, Map<String, Object> values,
      {String nullColumnHack, ConflictAlgorithm conflictAlgorithm}){

    expect(table, this.table);
    
    bool contain = false;
    for(var v in this.insertValues){
        if(mapEquals(v, values)){
          contain = true;
        }
    }
    
    expect(contain, true);
    
    return null;
  }
  
  
  @override
  noSuchMethod(Invocation invocation) => {};
}

void main() {


  test('inicialize news dababase test', () {
    WidgetsFlutterBinding.ensureInitialized();
    final DatabaseMockGet databaseMockGetTopics = DatabaseMockGet('');
    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);
    WidgetsFlutterBinding.ensureInitialized();


    final result = ['''CREATE TABLE Subject(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE)
        ''', '''CREATE TABLE Article (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      subtitle TEXT NOT NULL,
      body TEXT NOT NULL,
      image BLOB,
      author TEXT,
      date DATE,
      subject string REFERENCES Subject('name') , UNIQUE(title, subject))
    ''', '''
      CREATE TABLE Comment (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        text TEXT NOT NULL,
        date DATE,
        id_article INTEGER REFERENCES Article(id),
        UNIQUE(name, text, id_article)
        )
    '''];

    expect(result, database.commands);
    

  });
  
  test('get topics from dababase test', ()  async {
      WidgetsFlutterBinding.ensureInitialized();

      final List<Topic> topicListExpected = [Topic(1, 'Topic1'), Topic(2, 'Topic2'), 
                Topic(3, 'Topic3'), Topic(4, 'Topic4'),Topic(5, 'Topic5')];
      
      final List<Map<String, Object>> list = [];
      for(Topic topic in topicListExpected){
        list.add({'id': topic.id, 'name': topic.name});
      }
      
      final DatabaseMockGet databaseMockGetTopics = DatabaseMockGet('Subject', listReturned: list);
      final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);
      
      WidgetsFlutterBinding.ensureInitialized();
      
      final topicsList =  await database.getTopics();

      WidgetsFlutterBinding.ensureInitialized();
      
      for(var topic in topicListExpected){
        expect(topicsList.containsKey(topic.name), true);
        expect(topicsList[topic.name].name, topic.name);
        expect(topicsList[topic.name].id, topic.id);
      }


      WidgetsFlutterBinding.ensureInitialized();
    });
  
  test('get articles from dababase test', ()  async {
    WidgetsFlutterBinding.ensureInitialized();

    
    final List<Article> ArticleListExpected = [
      Article(1, 'title1', 'sutitle1', 'body1', Uint8List(1), 'author1', 'date1', 'subject1'),
      Article(3, 'title3', 'sutitle3', 'body3', Uint8List(3), 'author3', 'date3', 'subject2')];

    final List<Map<String, Object>> list = [];
    for(Article article in ArticleListExpected){
      list.add(article.toMap());
    }

    final DatabaseMockGet databaseMockGetTopics = DatabaseMockGet(
        'Article', where: 'subject = ?', whereArgs: ['subject1'], listReturned: list);
    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);

    WidgetsFlutterBinding.ensureInitialized();

    final articleList =  await database.getArticles('subject1');

    WidgetsFlutterBinding.ensureInitialized();

    for(var rename in ArticleListExpected){
      expect(articleList.containsKey(rename.title), true);
      expect(articleList[rename.title].toMap(), rename.toMap());
    }


  });
  
  test('get comments from dababase test', ()  async {
    WidgetsFlutterBinding.ensureInitialized();


    final List<Comment> CommentListExpected = [
      Comment(1, 'title1', 'sutitle1', 'author1', 1),
      Comment(3, 'title3', 'sutitle3', 'body3', 1)];

    final List<Map<String, Object>> list = [];
    for(Comment comment in CommentListExpected){
      list.add(comment.toMap());
    }
    
    int idArticle = 1;

    final DatabaseMockGet databaseMockGetTopics = 
    DatabaseMockGet('Comment', 
        where: 'id_article =?', whereArgs: [idArticle], listReturned: list);
    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);

    WidgetsFlutterBinding.ensureInitialized();

    final commentList =  await database.getComments(idArticle);

    WidgetsFlutterBinding.ensureInitialized();

    int i = 0;
    expect(commentList.length, CommentListExpected.length);
    for(var comment in CommentListExpected){
      expect(comment.toMap(), commentList[i++].toMap());
    }
    
  });

  test('add Comment in dababase test', ()  async {
    WidgetsFlutterBinding.ensureInitialized();

    Comment commentExpected = Comment(0, 'name1', 'text1', 'date1', 1);
    
    final DatabaseMockGet databaseMockGetTopics = 
    DatabaseMockGet('Comment', insertValues: [commentExpected.toMap()]);
    
    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);

    WidgetsFlutterBinding.ensureInitialized();
    
    database.addComment(commentExpected);
  });


  test('add topic in dababase test', ()  async {
    WidgetsFlutterBinding.ensureInitialized();

    String topic1 = 'TOPICO1';
    String topic2 = 'TOPICO2';

    final DatabaseMockGet databaseMockGetTopics =
    DatabaseMockGet('Subject', insertValues: [{'name':topic1}],
                    listReturned: [ 
                      {'id':2, 'name':topic2}
                    ]);

    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);

    WidgetsFlutterBinding.ensureInitialized();
    
    await database.addTopic(topic1);
    await database.addTopic(topic2);

  });

  test('add article in dababase test', ()  async {
    WidgetsFlutterBinding.ensureInitialized();

    final List<Article> ArticleListExpected = [
      Article(1, 'title1', 'sutitle1', 'body1', Uint8List(1), 'author1', 'date1', 'subject1'),
      Article(2, 'title2', 'sutitle2', 'body2', Uint8List(2), 'author2', 'date2', 'subject1')
    ];

    final List<Map<String, Object>> list = [ArticleListExpected[0].toMap()];

    final DatabaseMockGet databaseMockGetTopics = DatabaseMockGet(
        'Article', where: 'subject = ?', whereArgs: ['subject1'], 
        listReturned: list,
        insertValues: [ArticleListExpected[1].toMap()]);
    final AppNewsDatabase database = AppNewsDatabase(db: databaseMockGetTopics);

    WidgetsFlutterBinding.ensureInitialized();
    
    for(var article in ArticleListExpected){
      await database.addArticle(article);
    }

  });

}