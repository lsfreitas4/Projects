import 'package:flutter/material.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => LoginState();
}


class LoginState extends State<StatefulWidget>{
  @override
  Widget build(BuildContext context) {

    return MaterialApp(
        title: 'Daily Feup',
        theme: ThemeData(
          scaffoldBackgroundColor: const Color.fromRGBO(0xFF, 0xFF, 0xFF, 1),
        ),
        home: Scaffold(appBar: AppBar(
          title: const Text("DailyFeup"),
          backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),),
          body: Form(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                TextFormField(
                  decoration: const InputDecoration(
                    labelText: 'Student Number',
                    labelStyle: TextStyle(color: Color.fromRGBO(0x8C, 0x2D, 0x19, 1)) ,

                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Color.fromRGBO(0x8C, 0x2D, 0x19, 1), width: 2)),

                  ),
                  cursorColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),

                )
                ,TextFormField(
                  obscureText: true,

                  decoration: const InputDecoration(
                    labelText: 'Password',
                    labelStyle: TextStyle(color: Color.fromRGBO(0x8C, 0x2D, 0x19, 1)),
                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Color.fromRGBO(0x8C, 0x2D, 0x19, 1), width: 2)),

                  ),
                  cursorColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),

                ),
                TextButton(

                  onPressed: () {  },
                  child: const Text('Login'),
                  style: ButtonStyle(backgroundColor: MaterialStateProperty.all(const Color.fromRGBO(0x8C, 0x2D, 0x19, 1)),
                      foregroundColor: MaterialStateProperty.all(Colors.white)),
                ),
                TextButton(
                  onPressed: () {},
                  child: const Text('Continue without logging in'),
                  style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.white),
                      foregroundColor: MaterialStateProperty.all(const Color.fromRGBO(0x8C, 0x2D, 0x19, 1))),
                ),
              ],
            ),
          )
        )
    );


  }

  
}