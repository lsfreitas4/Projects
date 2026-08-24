import 'dart:convert';
import 'dart:core';

import 'dart:typed_data';

class Article{
  int id;
  String title;
  String subtitle;
  String body;
  Uint8List image;
  String author;
  String date;
  String subject;


  Article(this.id, this.title, this.subtitle, this.body,
      this.image, this.author, this.date, this.subject);

  Map<String, dynamic> toMap() {
    final map = {
      'title': title,
      'subtitle': subtitle,
      'body': body,
      'author': author,
      'date': date,
      'subject': subject,
    };
    if(image!=null){
      map['image'] = base64.encode(image);
    }

    return map;
  }

}