
import 'dart:async';
import 'dart:convert';
import 'package:uni/controller/local_storage/app_database.dart';
import 'package:sqflite/sqflite.dart';
import 'package:uni/model/news_entities/comment.dart';

import 'package:uni/model/news_entities/topic.dart';

import '../../model/news_entities/article.dart';


class AppNewsDatabase extends AppDatabase {
  AppNewsDatabase({Database db}) : super('noticias.db', [
        '''CREATE TABLE Subject(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE)
        ''',
    '''CREATE TABLE Article (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      subtitle TEXT NOT NULL,
      body TEXT NOT NULL,
      image BLOB,
      author TEXT,
      date DATE,
      subject string REFERENCES Subject('name') , UNIQUE(title, subject))
    ''',
    '''
      CREATE TABLE Comment (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        text TEXT NOT NULL,
        date DATE,
        id_article INTEGER REFERENCES Article(id),
        UNIQUE(name, text, id_article)
        )
    '''
  ], db: db) {
    if(db==null) {
      _init();
    }
  }

  void _init() async{
    await addTopic('Anúncios');
    await addArticle(Article(1, "Lançamento do dailyfeup", "", "App criada por génios faz sucesso no primeiro dia", null, "G4", DateTime.now().toString(), "Anúncios"));
    await addComment(Comment(1, "G4", "Very nice app|", DateTime.now().toString(), 1));
  }

  


  Future<Map<String, Topic>> getTopics() async {
    // Get a reference to the database
    final Database db = await this.getDatabase();

    // Query the table for all subjects
    final List<Map<String, dynamic>> subjectsData = await db.query('Subject');

    final Map<String, Topic> subjects =  Map();
    subjectsData.forEach((e) => subjects[e['name']] = Topic(e['id'], e['name']));

    return subjects;
  }

  /// Adds a topic to this database.
  Future<void> addTopic(String topicName) async {
    Map<String, Topic> topics = await getTopics();
    if(!topics.containsKey(topicName)){
      await insertInDatabase('Subject',
          {'name': topicName},
          conflictAlgorithm: ConflictAlgorithm.replace);
    }
  }

  Future<Map<String, Article>> getArticles(String subject) async {
    // Get a reference to the database
    final Database db = await this.getDatabase();

    // Query the table for all subjects
    final List<Map<String, dynamic>> articlesData =
      await db.query('Article', where: 'subject = ?', whereArgs: [subject]);
    final Map<String, Article> articles =  Map();
    articlesData.forEach((e) {
      articles[e['title']] = Article(e['id'], e['title'], e['subtitle'], e['body'],
          (e['image'] == null ? null : base64.decode(e['image'])),
          e['author'], e['date'], e['subject']);
    });

    return articles;
  }

  /// Adds a article to this database.
  Future<void> addArticle(Article article) async {
    final Map<String, Article> articles = await getArticles(article.subject);
    if(!articles.containsKey(article.title)){
      await insertInDatabase('Article', article.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace);
    }
  }


  Future<List<Comment>> getComments(int idArticle) async {
    // Get a reference to the database
    final Database db = await this.getDatabase();

    // Query the table for all bus stops
    final List<Map<String, dynamic>> comments = await db.query('Comment',
                                                where: 'id_article =?',
                                                whereArgs:[idArticle]);


    return List.generate(comments.length, (i) {
      return Comment(comments[i]['id'], comments[i]['name'], comments[i]['text']
          , comments[i]['date'], comments[i]['id_article']);
    });
  }

  Future<void> addComment(Comment comment) async {
      await insertInDatabase(
        'Comment',
        comment.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }

}

