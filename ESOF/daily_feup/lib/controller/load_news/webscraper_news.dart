
import 'dart:convert';
import 'dart:developer';
import 'dart:typed_data';

import 'package:flutter/widgets.dart';
import 'package:html/parser.dart' as parser;
import 'package:http/http.dart' as http;
import 'package:uni/controller/local_storage/app_news_database.dart';
import 'package:uni/model/news_entities/article.dart';

class WebScraperNews{
    static const String sigarraNewsURL = "https://sigarra.up.pt/feup/pt/";


    static Future<bool> updateTopicos(AppNewsDatabase database) async{


        final response =
        await http.Client().get(Uri.parse(sigarraNewsURL+ 'noticias_geral.lista_noticias'));
        //Status Code 200 means response has been received successfully

        if (response.statusCode == 200) {
            //Getting the html document from the response
            var document = parser.parse(response.body);

            var ulWithTopics = document.querySelector('#conteudoinner  ul');
            var topicsList = ulWithTopics.querySelectorAll('a');

            for(var element in topicsList) {
                await database.addTopic(element.text);
            }

            return true;
        }

        return false;
    }


    static Future<Article> _getNewFromLink(String link, String topic) async{
      final response = await http.Client().get(Uri.parse(link));

      if (response.statusCode == 200) {
        var document = parser.parse(response.body);
        var title_element = document.querySelectorAll('#conteudoinner h1')[1];
        var subtitle_element = document.querySelector('#conteudoinner h2');

        String text = document.querySelector('#conteudoinner ').innerHtml;
        text = text.substring(text.indexOf('<p'));


        Uint8List image = null;

        var img_element = document.querySelector('#conteudoinner img');
        if(img_element != null){
          var image_response = await http.Client().get(
              Uri.parse(sigarraNewsURL + img_element.attributes['src']));
          if(image_response.statusCode == 200) {
            image = image_response.bodyBytes;
          }
        }

        var author = '';
        var date = '';

        Article article = Article(0, title_element.text, subtitle_element.text,
            text, image, author, date, topic);

        return article;
      }

      return null;
    }

    static Future<bool> updateNews(AppNewsDatabase database, String topic) async{

      final response =
      await http.Client().get(Uri.parse(sigarraNewsURL+'noticias_geral.lista_noticias'));
      //Status Code 200 means response has been received successfully

      if (response.statusCode == 200) {
        //Getting the html document from the response
        var document = parser.parse(response.body);

        var h2TopicsTitles = document.querySelectorAll('#conteudoinner  h2');
        var topicsList =  await database.getTopics();
        for(var h2Element in h2TopicsTitles){
          String topicName = h2Element.text;
          if(topicName == topic && topicsList.keys.contains(topicName)){

            var listOfNewsWithTopic = h2Element.nextElementSibling.nextElementSibling;

            // store news from each topic
            for(var new_article in listOfNewsWithTopic.children){
              var newLink = new_article.querySelector('a').attributes['href'];

              Article article = await _getNewFromLink(sigarraNewsURL + newLink, topic);

              await database.addArticle(article);

            }
            break;
          }
        }

        return true;
      }


        return false;
    }
}