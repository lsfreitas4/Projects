import 'package:flutter/material.dart';

class ListaAnuncios extends StatelessWidget {
  const ListaAnuncios({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Daily Feup',
      theme: ThemeData(
        scaffoldBackgroundColor: Colors.white,
      ),
      home: Scaffold(appBar: AppBar(
        title: const Text("DailyFeup"),
        backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),),
        body: ListView(

        children: <Widget>[
          Container(
            decoration: const BoxDecoration(
              border: Border(
                bottom: BorderSide( //                   <--- left side
                  color: Colors.white,
                  width: 1.0,
                ),
                top: BorderSide( //                    <--- top side
                  color: Colors.white,
                  width: 2.0,
                ),
              ),
            ),
            height: 50,
            child: TextButton(
                onPressed: () {  },
                child: const Text('Alugo Casa perto da universidade'),
                style: ButtonStyle(backgroundColor: MaterialStateProperty.all(const Color.fromRGBO(0x8C, 0x2D, 0x19, 1)),
                    foregroundColor: MaterialStateProperty.all(Colors.white))),
          ),

          Container(
            decoration: const BoxDecoration(
              border: Border(
                bottom: BorderSide( //                   <--- left side
                  color: Colors.white,
                  width: 1.0,
                ),
                top: BorderSide( //                    <--- top side
                  color: Colors.white,
                  width: 1.0,
                ),
              ),
            ),
            height: 50,
            child: TextButton(
                onPressed: () {  },
                child: const Text('Encontrei um cartao da universidade'),
                style: ButtonStyle(backgroundColor: MaterialStateProperty.all(const Color.fromRGBO(0x8C, 0x2D, 0x19, 1)),
                    foregroundColor: MaterialStateProperty.all(Colors.white))),
          )
        ]
        )
         ,
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            // Add your onPressed code here!
          },
            backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
            child: const Icon(Icons.add),
          )
      )
    );
  }
}