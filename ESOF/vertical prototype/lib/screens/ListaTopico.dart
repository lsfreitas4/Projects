import 'package:flutter/material.dart';


class ListaTopico extends StatefulWidget {
  const ListaTopico({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => ListaTopicoState();
}


class ListaTopicoState extends State<StatefulWidget>{
  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      title: 'Daily Feup',
      theme: ThemeData(
        scaffoldBackgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
      ),
      home: Scaffold(appBar: AppBar(
        title: const Text("DailyFeup"),
        backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),),
        body: ListView(

      children: <Widget>[
        Container(
          margin: const EdgeInsets.all(16.0),
          height: 50,
          child: TextButton(
            onPressed: () {  },
            child: const Text('Eventos academicos'),
            style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                foregroundColor: MaterialStateProperty.all(Colors.black))),
        ),
        Container(
          margin: const EdgeInsets.all(16.0),
          height: 50,
          child: TextButton(
              onPressed: () {  },
              child: const Text('Eventos Cientificos'),
              style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                  foregroundColor: MaterialStateProperty.all(Colors.black))),
        ),
        Container(
          margin: const EdgeInsets.all(16.0),
          height: 50,
          child: TextButton(
              onPressed: () {  },
              child: const Text('Eventos Culturais'),
              style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                  foregroundColor: MaterialStateProperty.all(Colors.black))),
        ),

        Container(
          margin: const EdgeInsets.all(16.0),
          height: 50,
          child: TextButton(
              onPressed: () {  },
              child: const Text('Eventos Desportivos'),
              style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                  foregroundColor: MaterialStateProperty.all(Colors.black))),
        ),

        Container(
          margin: const EdgeInsets.all(16.0),
          height: 50,
          child: TextButton(
              onPressed: () {  },
              child: const Text('Informações Gerais'),
              style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                  foregroundColor: MaterialStateProperty.all(Colors.black))),
        ),

      ],
      )
      )
    );
  }


}