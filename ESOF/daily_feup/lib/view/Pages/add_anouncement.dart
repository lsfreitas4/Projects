import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:image_picker/image_picker.dart';
import 'package:uni/model/news_entities/article.dart';
import 'package:uni/view/Pages/general_page_view.dart';
import 'package:uni/view/Widgets/toast_message.dart';
import 'package:uni/model/app_state.dart';

import '../../controller/local_storage/app_news_database.dart';

class AddAnnouncement extends StatefulWidget{
  State<StatefulWidget> createState() => AddAnnouncementState();
}

class AddAnnouncementState extends GeneralPageViewState {

  // Create a global key that uniquely identifies the Form widget
  // and allows validation of the form.
  final _formKey = GlobalKey<FormState>();
  TextEditingController titleController = TextEditingController();
  TextEditingController contentController = TextEditingController();

  Uint8List imageBlob = null;
  final  AppNewsDatabase databaseClient = AppNewsDatabase();

  final ImagePicker _picker = ImagePicker();

  Future<Uint8List>saveImage() async{
      final XFile imageFile = await _picker.pickImage(source: ImageSource.gallery);
      File image = File(imageFile.path);
      return image.readAsBytes();
  }




  Future<String> getUsername(BuildContext context)async{
    String name;
    if (StoreProvider.of<AppState>(context).state.content['profile'] !=
        null) {
      name = StoreProvider.of<AppState>(context).state.content['profile'].name;
    }
    return name;
  }

  @override
  Widget buildForm(BuildContext context) {

    var listOfForm = [
      Text(
        'Pick a title',
        style: TextStyle(fontSize: 20),
      ),
      TextFormField(
        key:Key("titleInput"),
        controller:titleController,
        decoration: const InputDecoration(
          hintStyle: TextStyle(
          fontSize: 15,
          color:Colors.black
          ),
          hintText: 'Add your title',
        focusedBorder:OutlineInputBorder(
          borderSide: const BorderSide(color:Colors.black,width:2.0),
        )
        ),
      ),
      Text(
        'Write your announcement',
        style: TextStyle(fontSize: 20),
      ),
      TextField(
        key:Key("contentInput"),
        controller:contentController,
        decoration: const InputDecoration(
          hintStyle: TextStyle(
            fontSize: 15,
            color:Colors.black
            ),
          hintText: 'Add your content',
            focusedBorder:OutlineInputBorder(
              borderSide: const BorderSide(color:Colors.black,width:2.0),
            )

        ),
        maxLines:7
      ),

      Container(
        padding: const EdgeInsets.symmetric(vertical: 15.0),
        child: Text(
                'Upload Media',
                style: TextStyle(fontSize: 20),
              ),
      ),
      Container(
        alignment:Alignment.bottomLeft,
          child:FloatingActionButton(
            onPressed: ()async {
              imageBlob = await saveImage();
            },
            backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
            tooltip: 'Pick Image from gallery',
            child: Icon(Icons.photo_library),
          )
      ),
      Padding(
          padding: const EdgeInsets.symmetric(vertical: 16.0),
          child: Align(
            alignment: Alignment.center,
            child: ElevatedButton(
            key:Key('AddAnnouncement'),  
            onPressed: () async{
              if(titleController.text == '') {
                ToastMessage.display(context, 'O título da notícia não pode estar vazio');
              }
              else{
                  final Article article = await Article(0,titleController.text,'',
                      contentController.text, imageBlob, await getUsername(context),
                      DateTime.now().toString(), 'Anúncios');
                  await databaseClient.addArticle(article);
                  ToastMessage.display(context, 'Anúncio adicionado com sucesso');
                  Navigator.pop(context);
              }
            },
            child: const Text('Submit'),
            style:ElevatedButton.styleFrom(
              alignment: Alignment.center,
              primary: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
              padding: EdgeInsets.symmetric(vertical:30,horizontal:50),
              textStyle: TextStyle(
              fontSize: 30,
              fontWeight: FontWeight.bold),
              shape:RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(35.0),
              )
            )
            ),
        )
      )
    ];



      return Form(
        key: _formKey,
        child: Container(
          child: ListView(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
              children: listOfForm,
          ),
        decoration: BoxDecoration(
            color: Colors.white),


        )
      );
    }



  @override
  Widget build(BuildContext context) {



    return MaterialApp(
      title: 'Daily Feup',
      theme: ThemeData(
        scaffoldBackgroundColor: Colors.white,
      ),
      home: Scaffold(
        appBar: buildAppBar(context),
          body: buildForm(context)
      )
    );
  }
}

