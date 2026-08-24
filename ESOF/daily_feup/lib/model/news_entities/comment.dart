import 'dart:core';

class Comment{
  int id;
  String name;
  String text;
  String date;
  int article;

  Comment(this.id, this.name, this.text, this.date,this.article);

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'text': text,
      'date': date,
      'id_article': article,
    };
  }
}